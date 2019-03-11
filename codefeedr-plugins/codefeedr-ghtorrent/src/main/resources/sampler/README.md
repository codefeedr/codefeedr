# GHTorrent sampler
The GHTorrent samples is a Python scripts which binds to a queue with a specific routing key and samples some of its data.
This might be useful to either investigate some of the GHTorrent data as well as understanding its type.

# How to run
First of all make sure you have your public key [submitted](http://ghtorrent.org/services.html) to GHTorrent.
Afterwards create a local port to which you can connect your AMQP client:
```bash
ssh -L 5672:streamer.ghtorrent.org:5672 ghtorrent@streamer.ghtorrent.org
```

Besides, make sure you install the following libraries:
```bash
pip install pika
```

To run the sampler script:
```bash
usage: sampler [-h] -u <username> [-x <amount>] [-r <key>] [-f <name>]
               [-d <directory>]

optional arguments:
  -h, --help      show this help message and exit
  -u <username>   Your username, this needs to be declared in the queue-name.
  -x <amount>     The amount of messages to sample.
  -r <key>        The routing key to sample from.
  -f <name>       Filename to save to (default: ROUTING_KEY.json).
  -d <directory>  Directory to save to (default: samples/).
``