package io.github.ouyi.kafka;

public interface Constants {
    String KAFKA_BROKERS = "localhost:19092";
    String CLIENT_ID = "client1";
    String TOPIC_NAME = "test";
    String GROUP_ID = "consumerGroup1";
    Integer AUTO_COMMIT_INTERVAL_MS = 1000;
    Boolean ENABLE_AUTO_COMMIT = true;
    Long POLL_INTERVAL_MS = 100L;
    String AUTO_OFFSET_RESET = "earliest";
}
