# kafka-flink-demo
A demo application based on kafka and flink

Project was initialized using the Maven archetype [flink-quickstart-java](https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/projectsetup/java_api_quickstart.html).

## Build

    mvn package


## Run

    cd docker
    docker-compose up
    docker exec -u 0 -it docker_jobmanager_1 bash
    java -jar /target/abc.jar
