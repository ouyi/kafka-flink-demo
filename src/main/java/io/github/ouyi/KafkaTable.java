package io.github.ouyi;

import io.github.ouyi.kafka.Constants;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Json;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Rowtime;
import org.apache.flink.table.descriptors.Schema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

public class KafkaTable {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.getTableEnvironment(env);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.GROUP_ID);

        tableEnv
            .connect(
                new Kafka()
                    .topic(Constants.TOPIC_NAME)
                    .properties(props)
                    .version("universal")
                    .startFromEarliest()
            )
            .withSchema(
                new Schema()
                    .field("data", Types.LONG())
                    .field("ts", Types.SQL_TIMESTAMP())
                    .rowtime(
                        new Rowtime()
                            .timestampsFromField("timestamp")
                            .watermarksPeriodicBounded(1000)
                    )
            )
            .withFormat(
                new Json()
                    .deriveSchema()
            )
            .inAppendMode()
            .registerTableSource("table_input");


        tableEnv
            .connect(
                new Kafka()
                    .topic("test_output")
                    .properties(props)
                    .version("universal")
            )
            .withFormat(
                new Json()
                    .deriveSchema()
            )
            .withSchema(
                new Schema()
                    .field("data", Types.LONG())
                    .field("ts", Types.SQL_TIMESTAMP())
            )
            .inAppendMode()
            .registerTableSink("table_output");

        tableEnv.scan("table_input").insertInto("table_output");

        env.execute(KafkaTable.class.getName());
    }
}
