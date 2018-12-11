
https://github.com/wurstmeister/kafka-docker
https://github.com/rmoff/kafka-listeners

$KAFKA_HOME/bin/kafka-topics.sh --list --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:19092 --topic test --from-beginning
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:19092 --topic test
docker exec -u 0 -it docker_kafka_1 bash
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list kafka:9092 --topic test
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --from-beginning
