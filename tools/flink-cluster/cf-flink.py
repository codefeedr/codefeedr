#!/usr/bin/env python

import requests
import argparse
import subprocess

def get_url(args, path):
    host = args.host or 'localhost:8081'
    return 'http://' + host + path

# List jobs. Arguments: -a to show inactive jobs too
# cf jobs
def list_jobs(args):
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

def list_programs(args):
    r = requests.get(get_url(args, '/jars'))
    if r.status_code is not 200:
        print("Could not connect to host")
        return

    data = r.json()

    print(data)

def upload_program(args):
    print('upload program')
    # http://docs.python-requests.org/en/master/user/quickstart/#post-a-multipart-encoded-file

def list_stages(args):
    if args.jar is None:
        print("No jar specified")
        return

    subprocess.call(['java', '-jar', args.jar, '--list'])

def start_pipeline(args):
    print('start pipeline')

def cancel_job(args):
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
    parser_jobs.set_defaults(func=list_jobs)

    # cf program
    parser_program = subparsers.add_parser('program', help='program (jar) commands')
    parser_program.add_argument('--jar', type=str, help='Jar file of the program')
    subparsers_program = parser_program.add_subparsers(title='sub-commands')

    # cf program list
    parser_program_list = subparsers_program.add_parser('list', help="list programs on server")
    parser_program_list.set_defaults(func=list_programs)

    # cf program stages
    parser_program_stages = subparsers_program.add_parser('stages', help="list stages in program")
    parser_program_stages.set_defaults(func=list_stages)

    # cf program upload
    parser_program_upload = subparsers_program.add_parser('upload', help="upload program")
    parser_program_upload.set_defaults(func=upload_program)

    # cf pipeline
    parser_pipeline = subparsers.add_parser('pipeline', help='pipeline commands')
    parser_pipeline.add_argument('bar', type=int, help='bar help')

    # cf pipeline run
    # cf pipeline stop
    # cf pipeline restart

    # cf stage
    parser_stage = subparsers.add_parser('stage', help='stage commands')
    # parser_stage.add_argument('list', help='List stages of a program')


    args = parser.parse_args()
    args.func(args)
