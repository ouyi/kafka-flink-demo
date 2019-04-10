
# kafka-flink-demo

A demo application implementing the Kappa architecture, based on Kafka, Flink, and ELK.

Project was initialized using the Maven archetype [flink-quickstart-java](https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/projectsetup/java_api_quickstart.html).


## Build

    ./mvnw package


## Run

    cd docker
    docker-compose up


## Play

### The Wikipedia analysis demo

In one terminal:

    docker exec -u 0 -it docker_kafka_1 bash
    /opt/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic wiki-results
    /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic wiki-results --from-beginning

In another terminal:

    docker exec -u 0 -it docker_jobmanager_1 bash
    flink run -c io.github.ouyi.WikipediaAnalysis /code/target/kafka-flink-demo-1.0-SNAPSHOT.jar

Watch the updates in the first terminal, and visit http://localhost:8081 with a browser to play with the Flink UI.

To stop the execution, run in the second terminal:

    flink list
    flink cancel <jobId>

### The Kafka and Flink stream SQL demo

In one terminal, start the standalone local filesystem connector:

    docker exec -u 0 -it docker_kafka_1 bash
    /opt/kafka/bin/kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic test_input
    /opt/kafka/bin/connect-standalone.sh /opt/kafka/config/connect-standalone.properties /opt/kafka/config/connect-file-source.properties

In another terminal:

    docker exec -u 0 -it docker_kafka_1 bash
    cat >> /var/tmp/test_input.json
    // Paste in test records from demo/src/main/resources/test_input.json

Start `io.github.ouyi.KafkaStreamSqlDemo` in IDE.

Create some visualizations using the index pattern `kafka` with the Kibana Web UI available at http://localhost:5601

Start `io.github.ouyi.kafka.SimpleProducer` in IDE.

## Clean up

    cd docker
    docker-compose down -v
