#!/usr/bin/env python

import requests
import argparse
import os
import time
import json

################################################
### FUNCTIONS
################################################

def get_url(args, path):
    host = args.host or os.environ["FLINK_HOST"] or "localhost:8081"
    return "http://" + host + path

def get_job_state(jobId):
    r = requests.get(get_url(args, "/jobs/" + jobId))
    result = r.json()

    return result["state"]

def get_job_exceptions(jobId):
    r = requests.get(get_url(args, "/jobs/" + jobId + "/exceptions"))
    result = r.json()

    print(result)

    return result["all-exceptions"]

def wait_for_job_exceptions(jobId):
    while True:
        time.sleep(5)

        print("  Getting job status...")
        state = get_job_state(jobId)
        if state == "FAILED":
            return get_job_exceptions(jobId)

        if state != "RUNNING":
            return None

def get_stages_from_jar(programId):
    params = {"program-args": "--list --asException"}
    r = requests.post(get_url(args, "/jars/" + programId + "/run"), params=params)

    uniquenessPrefix = "Caused by: org.codefeedr.pipeline.StageIdsNotUniqueException: "
    prefix = "Caused by: org.codefeedr.pipeline.PipelineListException: "

    result = None
    badStage = None

    for line in r.text.split("\n"):
        if line.startswith(prefix):
            text = line[len(prefix):]
            result = json.loads(text)
        if line.startswith(uniquenessPrefix):
            text = line[len(uniquenessPrefix):]
            badStage = text

    if badStage is not None:
        print("ERROR: List of stages in program is not unique. Stage '" + badStage + "' appears more than once.")
        return None

    if result is None:
        print("ERROR: Could not find list of stages in output")
        return None

    return result

def upload_jar(jar):
    print("Uploading jar...")

    files = {"file": (os.path.basename(jar), open(jar, "rb"), "application/x-java-archive")}

    r = requests.post(get_url(args, "/jars/upload"), files=files)
    result = r.json()

    if result["status"] != "success":
        return None

    return result["filename"]

def start_stage(programId, stage):
    params = {"program-args": "--stage " + stage, "parallelism": 1}
    r = requests.post(get_url(args, "/jars/" + programId + "/run"), params=params)
    if r.status_code == 200:
        return r.json()["jobid"]

    print("ERROR: Failed to start job. Flink returned:")
    print(r.text)

    return None

def cancel_job(jobId):
    r = requests.delete(get_url(args, "/jobs/" + jobId + "/cancel"))
    if r.status_code == 200:
        return True

    return False

def get_job(jobId):
    r = requests.get(get_url(args, "/jobs/" + jobId))
    if r.status_code != 200:
        return None

    return r.json()

def get_jobs():
    r = requests.get(get_url(args, "/jobs"))
    if r.status_code is not 200:
        print("Could not connect to host")
        return

    data = r.json()

    # Switch to a job+info format
    jobs = []
    for type, list in data.iteritems():
        for job in list:
            jobs.append({"id": job, "status": type[5:]})

    return jobs

def get_stage_from_job_name(name):
    nameInfo = name.split(": ")
    if len(nameInfo) > 1:
        return nameInfo[1]
    return ""

def get_active_stages():
    # Get list of running jobs
    jobs = get_jobs()

    # For each, get name
    filtered = filter(lambda job: job["status"] == "running", jobs)

    for job in filtered:
        info = get_job(job["id"])
        job["name"] = info["name"]
        job["stage"] = get_stage_from_job_name(info["name"])

    return filter(lambda job: job["name"] is not "", filtered)

def delete_program(programId):
    r = requests.delete(get_url(args, "/jars/" + programId))

    return r.status_code == 200


################################################
### COMMANDS
################################################

# List jobs. Arguments: -a to show inactive jobs too
# cf jobs
def cmd_list_jobs(args):
    jobs = get_jobs()

    if args.q is False:
        print("JOBID\t\t\t\t\tSTATUS\t\tSTAGE")

    for job in jobs:
        if job["status"] == "running" or args.a is True:
            if args.q is True:
                print(job["id"])
            else:
                info = get_job(job["id"])
                stage = get_stage_from_job_name(info["name"])
                print(job["id"] + "\t" + job["status"] + "\t\t" + stage)

def cmd_list_programs(args):
    r = requests.get(get_url(args, "/jars"))
    if r.status_code is not 200:
        print("Could not connect to host")
        return

    data = r.json()

    for jar in data["files"]:
        print(jar["id"] + "\t\t" + jar["name"] + "\t" + str(jar["uploaded"]))

def cmd_list_stages(args):
    if args.jar is None:
        print("No jar specified")
        return

    programId = upload_jar(args.jar)
    if programId is None:
        print("Failed to upload jar to Flink")
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    stages = get_stages_from_jar(programId)
    if stages is None:
        delete_program(programId)
        return

    print("Found " + str(len(stages)) + " stages")

    delete_program(programId)

    if len(stages) == 0:
        return

    for stage in stages:
        print(stage)

def cmd_get_pipeline_info(args):
    programId = upload_jar(args.jar)
    if programId is None:
        print("Failed to upload jar to Flink")
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    stages = get_stages_from_jar(programId)
    if stages is None:
        delete_program(programId)
        return

    print("\nFound " + str(len(stages)) + " stages in JAR:")
    for stage in stages:
        print(stage)

    print("\nRemoving JAR...")
    delete_program(programId)


def cmd_start_pipeline(args):
    print("Starting pipeline in " + args.jar + "...")

    programId = upload_jar(args.jar)
    if programId is None:
        print("Failed to upload jar to Flink")
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    stages = get_stages_from_jar(programId)
    if stages is None:
        delete_program(programId)
        return
    print("Found " + str(len(stages)) + " stages")

    if len(stages) == 0:
        return

    # Find all stages that already run
    activeStages = map(lambda x: x["stage"], get_active_stages())

    # For each item in list, start a flink job
    totalNewJobs = 0
    for stage in stages:
        if stage in activeStages:
            print("Stage already running: " + stage)
        else:
            print("Starting stage: " + stage)
            jobId = start_stage(programId, stage)
            totalNewJobs = totalNewJobs + 1
            if jobId is None:
                return

            print("Started with jobId '" + jobId + "'")

    if totalNewJobs == 0:
        print("Removing JAR...")
        delete_program(programId)

    print("Done")

def cmd_stop_pipeline(args):
    print("Starting pipeline in " + args.jar + "...")

    programId = upload_jar(args.jar)
    if programId is None:
        print("Failed to upload jar to Flink")
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    # Get list of stages form jar
    stagesInJar = get_stages_from_jar(programId)
    if stages is None:
        delete_program(programId)
        return
    print("Found " + str(len(stagesInJar)) + " stages")

    delete_program(programId)

    # Get the jobs
    jobsInFlink = get_active_stages()
    toCancel = filter(lambda job: job["stage"] in stagesInJar, jobsInFlink)

    print("Found " + str(len(toCancel)) + " jobs to stop")

    for job in toCancel:
        print("Stopping job '" + job["id"] + "' for stage " + job["stage"])
        if not cancel_job(job["id"]):
            print("ERROR: Failed to stop job " + job["id"])

def cmd_cancel_job(args):
    jobId = args.jobId

    if cancel_job(jobId):
        print("Cancelled job '" + jobId + "'")
    else:
        print("Failed to cancel job '" + jobId + "'")

def cmd_start_stage(args):
    print("Starting stage '" + args.stage + "' from jar " + args.jar + "...")

    programId = upload_jar(args.jar)
    if programId is None:
        print("Failed to upload jar to Flink")
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    # Find all stages that already run
    activeStages = map(lambda x: x["stage"], get_active_stages())

    # For each item in list, start a flink job
    totalNewJobs = 0
    stage = args.stage
    if stage in activeStages:
        print("Stage already running: '" + stage + "'")
        print("Removing JAR...")
        delete_program(programId)
    else:
        print("Starting stage: " + stage)
        jobId = start_stage(programId, stage)
        totalNewJobs = totalNewJobs + 1
        if jobId is None:
            print("Removing JAR...")
            delete_program(programId)
            return

        print("Started with jobId '" + jobId + "'")

    print("Done")

def cmd_stop_stage(args):
    print("Stopping stage '" + args.stage + "' from JAR " + args.jar + "...")

    activeStages = get_active_stages()
    if len(activeStages) == 0:
        print("No active stages found")
        return

    for stage in activeStages:
        if stage["stage"] == args.stage:
            jobId = stage["id"]
            if cancel_job(jobId):
                print("Cancelled job '" + jobId + "'")
            else:
                print("Failed to cancel job '" + jobId + "'")

    print("Done")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Communicate with Flink.")
    parser.add_argument("--host", help="host of Flink")
    subparsers = parser.add_subparsers(title="commands")

    # cf jobs
    parser_jobs = subparsers.add_parser("jobs", help="list jobs on server")
    parser_jobs.add_argument("-a", help="Also show inactive jobs", action="store_true")
    parser_jobs.add_argument("-q", help="Only show job IDs", action="store_true")
    parser_jobs.set_defaults(func=cmd_list_jobs)

    # cf cancel
    parser_cancel_job = subparsers.add_parser("cancel", help="cancel job")
    parser_cancel_job.add_argument("jobId", help="JobID")
    parser_cancel_job.set_defaults(func=cmd_cancel_job)

    # cf program
    parser_program = subparsers.add_parser("program", help="program (jar) commands")
    parser_program.add_argument("--jar", type=str, help="Jar file of the program")
    subparsers_program = parser_program.add_subparsers(title="sub-commands")

    # cf program list
    parser_program_list = subparsers_program.add_parser("list", help="list programs on server")
    parser_program_list.set_defaults(func=cmd_list_programs)

    # cf program stages
    parser_program_stages = subparsers_program.add_parser("stages", help="list stages in program")
    parser_program_stages.set_defaults(func=cmd_list_stages)

    # cf pipeline
    parser_pipeline = subparsers.add_parser("pipeline", help="pipeline commands")
    subparsers_pipeline = parser_pipeline.add_subparsers(title="sub-commands")

    # cf pipeline info
    parser_pipeline_info = subparsers_pipeline.add_parser("info", help="get pipeline info")
    parser_pipeline_info.add_argument("jar", type=str, help="path to JAR of the pipeline")
    parser_pipeline_info.set_defaults(func=cmd_get_pipeline_info)

    # cf pipeline start
    parser_pipeline_start = subparsers_pipeline.add_parser("start", help="run a pipeline")
    parser_pipeline_start.add_argument("jar", type=str, help="path to JAR of the pipeline")
    parser_pipeline_start.set_defaults(func=cmd_start_pipeline)

    # cf pipeline stop
    parser_pipeline_stop = subparsers_pipeline.add_parser("stop", help="stop a pipeline")
    parser_pipeline_stop.add_argument("jar", type=str, help="path to JAR of the pipeline")
    parser_pipeline_stop.set_defaults(func=cmd_stop_pipeline)

    # cf stage
    parser_stage = subparsers.add_parser("stage", help="stage commands")
    subparsers_stage = parser_stage.add_subparsers(title="sub-commands")

    # cf stage start
    parser_stage_start = subparsers_stage.add_parser("start", help="start a single stage")
    parser_stage_start.add_argument("jar", type=str, help="path to JAR of the pipeline")
    parser_stage_start.add_argument("stage", type=str, help="stage ID")
    parser_stage_start.set_defaults(func=cmd_start_stage)

    # cf stage stop
    parser_stage_stop = subparsers_stage.add_parser("stop", help="stop a single stage")
    parser_stage_stop.add_argument("jar", type=str, help="path to JAR of the pipeline")
    parser_stage_stop.add_argument("stage", type=str, help="stage ID")
    parser_stage_stop.set_defaults(func=cmd_stop_stage)


    args = parser.parse_args()
    args.func(args)
