package io.github.ouyi.kafka;

public interface Constants {
    String KAFKA_BROKERS = "kafka:9092";
    String CLIENT_ID = "client1";
    String TOPIC_NAME = "test";
    String GROUP_ID = "consumerGroup1";
    long AUTO_COMMIT_INTERVAL_MS = 1000L;
    Boolean ENABLE_AUTO_COMMIT = true;
    Long POLL_INTERVAL_MS = 100L;
    String TOPIC_NAME_INPUT = "test_input";
    String TOPIC_NAME_OUTPUT = "test_output";
    long OUT_OF_ORDERNESS_MS = 1000L;
}
