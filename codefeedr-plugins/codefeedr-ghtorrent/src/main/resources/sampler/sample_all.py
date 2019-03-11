#!/usr/bin/env python
import argparse
import json
import os

# Parse arguments.
parser = argparse.ArgumentParser("sampler")
parser.add_argument("-u", metavar="<username>", help="Your username, this needs to be declared in the queue-name.", required=True)
args = parser.parse_args()

# Get the arguments.
username = args.u
routes = "routing_keys.txt"
