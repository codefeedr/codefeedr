#!/usr/bin/env python
import argparse
import pika
import json
import os

# Parse arguments.
parser = argparse.ArgumentParser("sampler")
parser.add_argument("-u", metavar="<username>", help="Your username, this needs to be declared in the queue-name.", required=True)
parser.add_argument("-x", metavar="<amount>", help="The amount of messages to sample.", default=10)
parser.add_argument("-r", metavar="<key>", help="The routing key to sample from.", default="#")
parser.add_argument("-f", metavar="<name>", help="Filename to save to (default: ROUTING_KEY.json).")
parser.add_argument("-d", metavar="<directory>", help="Directory to save to (default: samples/).", default="samples/")

args = parser.parse_args()

# Get the arguments.
route = args.r
username = args.u
amount = args.x
folder = args.d
filename = route + ".json"

if args.f:
    filename = args.f

# Start connection with RabbitMQ server.
cred = pika.credentials.PlainCredentials("streamer", "streamer")
connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost', credentials=cred))
channel = connection.channel()

# Declare exchange with name 'ght-streams' as specified in the documentation.
channel.exchange_declare(exchange="ght-streams", durable=True, exchange_type="topic")

# Declare a queue, with a username prefixed (this is a requirement).
q = channel.queue_declare(username + "_queue", auto_delete=True)
queue_name = q.method.queue

# Bind a routing key to the queue.
channel.queue_bind(exchange="ght-streams", queue=queue_name, routing_key=route)

# Keeps track of all records
processed = 0
records = []

# Callback which simply adds according to the route.
def callback(ch, method, properties, body):
    global processed

    processed += 1
    records.append(json.loads(body.decode("utf-8")))

    if processed >= amount:
        channel.stop_consuming()
        write_samples()



# Write to file.
def write_samples():
    if not os.path.exists(folder):
        os.mkdir(folder)
    with open(folder + filename, 'w+') as outfile:
        json.dump(records, outfile)


# Setup callback.
channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)

# Start consuming.
channel.start_consuming()