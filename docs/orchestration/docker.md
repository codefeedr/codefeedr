# Docker



To run a whole set of docker containers for running most CodeFeedr jobs, use the docker-compose file
supplied in `/tools/docker`. It contains a mongo instance, Flink job manager and taskrunner, Zookeeper and Kafka.
You can disable any parts by commenting them out or removing them. Redis (for use as key manager)can be activated by
uncommenting the redis lines.

__Warning__: The provided docker compose file is 100% ___UNSECURED___. Ports are open and there is no authentication.
To provide better security, use a firewall to control who can reach each port or close the port completely by
removing the `port` part of the service.

### Scaling Flink Managers
With docker-compose it is easy to create more space for more jobs.

The following command will add an extra taskrunner. It will automatically configure itself and register with the job manager.

`docker-compose up -d --scale taskrunner=2`

To remove the second task runner, scale back to 1. __Note__ that any jobs running on that task manager will be lost.

### Running a CodeFeedr pipeline on Docker
To run a pipeline, an assembled JAR file of the pipeline needs to be uploaded to the Flink Job Manager. Then for each stage
a job needs to be started.

The `cf-flink` tool, in `tools/flink-cluster` makes this easier. See [the tool documentation](orchestration/tool.md).

### Running on a cluster
To run on a cluster of computers instead of a single computer, it is recommended to use Docker Swarm. Create a
swarm master and swarm nodes and run the docker compose file. You can adjust the compose to define where certain
services should reside within the swarm. For this we suggest a read of the [Docker Swarm documentation](https://docs.docker.com/engine/swarm/). 