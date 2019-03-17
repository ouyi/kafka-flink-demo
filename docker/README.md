
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
    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list kafka:9092 --topic test < ./test.txt
    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list kafka:9092 --topic test --property "parse.key=true" --property "key.separator=:" < ./test.txt
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --from-beginning

Use connector to watch a file and add new lines into a topic:

    $KAFKA_HOME/bin/connect-standalone.sh $KAFKA_HOME/config/connect-standalone.properties $KAFKA_HOME/config/connect-file-source.properties

    // Depending on the text file format, the following changes to the connect-standalone.properties file might be needed:
    -bootstrap.servers=localhost:9092
    +bootstrap.servers=kafka:9092

    -key.converter=org.apache.kafka.connect.json.JsonConverter
    -value.converter=org.apache.kafka.connect.json.JsonConverter
    +key.converter=org.apache.kafka.connect.storage.StringConverter
    +value.converter=org.apache.kafka.connect.storage.StringConverter

    -key.converter.schemas.enable=true
    -value.converter.schemas.enable=true
    +key.converter.schemas.enable=false
    +value.converter.schemas.enable=false

Delete messages from a Kafka topic:

    cat > offsets.json
    {"partitions": [{"topic": "test", "partition": 0, "offset": 2}], "version":1 }
    ctrl-d

    $KAFKA_HOME/bin/kafka-delete-records.sh --bootstrap-server kafka:9092 --offset-json-file ./offsets.json

Delete a Kafka topic:

    $KAFKA_HOME/bin/kafka-topics.sh --delete --zookeeper zookeeper:2181 --topic test

Indent the YAML file:

    yq r docker-compose.yml > docker-compose.yml.tmp && mv docker-compose.yml.tmp docker-compose.yml
