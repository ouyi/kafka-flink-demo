
# The demo environment with everything in Docker containers

## Informative links

- https://github.com/wurstmeister/kafka-docker
- https://github.com/rmoff/kafka-listeners

## Useful commands

Interact with Kafka from the Docker host:

    $KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper localhost:2181
    $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:19092 --topic test --from-beginning
    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:19092 --topic test

Interact with Kafka from within the Docker container:

    docker exec -u 0 -it docker_kafka_1 bash
    $KAFKA_HOME/bin/kafka-topics.sh --describe --topic test --zookeeper zookeeper:2181
    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list kafka:9092 --topic test
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --from-beginning

Delete messages from a Kafka topic:

    cat > offsets.json
    {"partitions": [{"topic": "test", "partition": 0, "offset": 2}], "version":1 }
    ctrl-d

    $KAFKA_HOME/bin/kafka-delete-records.sh --bootstrap-server kafka:9092 --offset-json-file ./offsets.json

Auto indent YAML file:

    yq r docker-compose.yml > docker-compose.yml.tmp && mv docker-compose.yml.tmp docker-compose.yml
