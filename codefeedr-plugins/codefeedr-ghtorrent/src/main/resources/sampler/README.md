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