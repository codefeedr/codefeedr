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
    host = args.host or 'localhost:8081'
    return 'http://' + host + path

def get_job_state(jobId):
    r = requests.get(get_url(args, '/jobs/' + jobId))
    result = r.json()

    return result["state"]

def get_job_exceptions(jobId):
    r = requests.get(get_url(args, '/jobs/' + jobId + '/exceptions'))
    result = r.json()

    print(result)

    return result["all-exceptions"]

def wait_for_job_exception(jobId):
    while True:
        time.sleep(5)

        print("  Getting job status...")
        state = get_job_state(jobId)
        if state == "FAILED":
            return get_job_exception(jobId)

        if state != "RUNNING":
            return None

def get_stages_from_jar(programId):
    params = {"program-args": "--list --asException"}
    r = requests.post(get_url(args, '/jars/' + programId + '/run'), params=params)

    prefix = "Caused by: org.codefeedr.pipeline.PipelineListException: "
    result = None
    for line in r.text.split("\n"):
        if line.startswith(prefix):
            text = line[len(prefix):]
            result = json.loads(text)

    if result is None:
        print("ERROR: Could not find list of stages in output")
        return None

    return result

def upload_jar(jar):
    files = {'file': (os.path.basename(jar), open(jar, 'rb'), 'application/x-java-archive')}

    r = requests.post(get_url(args, '/jars/upload'), files=files)
    result = r.json()

    if result["status"] != "success":
        return None

    return result["filename"]

def start_stage(programId, stage):
    params = {"program-args": "--stage " + stage}
    r = requests.post(get_url(args, '/jars/' + programId + '/run'), params=params)
    if r.status_code == 200:
        return r.json()["jobid"]

    print("ERROR: Failed to start job. Flink returned:")
    print(r.text)

    return None

################################################
### COMMANDS
################################################

# List jobs. Arguments: -a to show inactive jobs too
# cf jobs
def cmd_list_jobs(args):
    r = requests.get(get_url(args, '/jobs'))
    if r.status_code is not 200:
        print("Could not connect to host")
        return

    data = r.json()

    print(args)

    # Switch to a job+info format
    jobs = []
    for type, list in data.iteritems():
        for job in list:
            jobs.append({"id": job, "status": type[5:]})

    for job in jobs:
        if job["status"] == "running" or args.a is True:
            if args.q is True:
                print(job["id"])
            else:
                print(job["id"] + "\t\t" + job["status"])

def cmd_list_programs(args):
    r = requests.get(get_url(args, '/jars'))
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

    print('Uploading jar...')
    programId = upload_jar(args.jar)
    if programId is None:
        print('Failed to upload jar to Flink')
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    stages = get_stages_from_jar(programId)
    print('Found ' + str(len(stages)) + ' stages')

    if len(stages) == 0:
        return

    for (stage in stages):
        print(stage)

def cmd_get_pipeline_info(args):
    print("get pipeline info")

def cmd_start_pipeline(args):
    print('Starting pipeline in ' + args.jar + '...')

    print('Uploading jar...')
    programId = upload_jar(args.jar)
    if programId is None:
        print('Failed to upload jar to Flink')
        return
    else:
        print("Uploaded program with id '" + programId + "'")

    stages = get_stages_from_jar(programId)
    print('Found ' + str(len(stages)) + ' stages')

    if len(stages) == 0:
        return

    # For each item in list, start a flink job
    for stage in stages:
        print('Starting stage ' + stage)
        jobId = start_stage(programId, stage)
        if jobId is None:
            return

        print("Started with jobId '" + jobId + "'")

    print('Done')

def cmd_stop_pipeline(args):
    print('stop pipeline')

    # Get list of stages form jar

    # for each stage, stop job


def cmd_cancel_job(args):
    print('Cancel job')

# cf stage list
# cf stage start
# cf stage restart
# cf stage stop


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Communicate with Flink.')
    parser.add_argument('--host', help="host of Flink")
    subparsers = parser.add_subparsers(title='commands')

    # cf jobs
    parser_jobs = subparsers.add_parser('jobs', help="list jobs on server")
    parser_jobs.add_argument('-a', help="Also show inactive jobs", action='store_true')
    parser_jobs.add_argument('-q', help="Only show job IDs", action='store_true')
    parser_jobs.set_defaults(func=cmd_list_jobs)

    # cf program
    parser_program = subparsers.add_parser('program', help='program (jar) commands')
    parser_program.add_argument('--jar', type=str, help='Jar file of the program')
    subparsers_program = parser_program.add_subparsers(title='sub-commands')

    # cf program list
    parser_program_list = subparsers_program.add_parser('list', help="list programs on server")
    parser_program_list.set_defaults(func=cmd_list_programs)

    # cf program stages
    # parser_program_stages = subparsers_program.add_parser('stages', help="list stages in program")
    # parser_program_stages.set_defaults(func=list_stages)

    # cf program upload
    # parser_program_upload = subparsers_program.add_parser('upload', help="upload program")
    # parser_program_upload.set_defaults(func=upload_program)

    # cf pipeline
    parser_pipeline = subparsers.add_parser('pipeline', help='pipeline commands')
    subparsers_pipeline = parser_pipeline.add_subparsers(title='sub-commands')

    # cf pipeline info
    parser_pipeline_info = subparsers_pipeline.add_parser('info', help="get pipeline info")
    parser_pipeline_info.add_argument('jar', type=str, help='path to JAR of the pipeline')
    parser_pipeline_info.set_defaults(func=cmd_get_pipeline_info)

    # cf pipeline start
    parser_pipeline_start = subparsers_pipeline.add_parser('start', help="run a pipeline")
    parser_pipeline_start.add_argument('jar', type=str, help='path to JAR of the pipeline')
    parser_pipeline_start.set_defaults(func=cmd_start_pipeline)

    # cf pipeline stop
    parser_pipeline_stop = subparsers_pipeline.add_parser('stop', help="stop a pipeline")
    parser_pipeline_stop.add_argument('jar', type=str, help='path to JAR of the pipeline')
    parser_pipeline_stop.set_defaults(func=cmd_stop_pipeline)

    # cf stage
    # parser_stage = subparsers.add_parser('stage', help='stage commands')
    # parser_stage.add_argument('list', help='List stages of a program')


    args = parser.parse_args()
    args.func(args)
