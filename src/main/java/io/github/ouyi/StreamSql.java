package io.github.ouyi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ouyi.kafka.Constants;
import io.github.ouyi.kafka.Record;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.sinks.CsvTableSink;
import org.apache.flink.table.sinks.TableSink;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.io.IOException;
import java.util.Properties;

public class StreamSql {
    public static void main(String[] args) throws Exception {

        // set up execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.getTableEnvironment(env);

        DeserializationSchema<Record> valueDeserializer = new AbstractDeserializationSchema<Record>() {
            @Override
            public Record deserialize(byte[] message) throws IOException {
                ObjectMapper mapper = new ObjectMapper();
                Record record = null;
                try {
                    record = mapper.readValue(message, Record.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                return record;
            }
        };
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.GROUP_ID);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Constants.ENABLE_AUTO_COMMIT);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Constants.AUTO_COMMIT_INTERVAL_MS);
        FlinkKafkaConsumer<Record> flinkKafkaConsumer = new FlinkKafkaConsumer(Constants.TOPIC_NAME, valueDeserializer, props);
        flinkKafkaConsumer.setStartFromEarliest();

        DataStream<Record> stream = env.addSource(flinkKafkaConsumer);
        String tableName = "test_input";
        tEnv.registerDataStream(tableName, stream);
//        stream.map(new MapFunction<Record, Record>() {
//            @Override
//            public Record map(Record value) throws Exception {
//                System.out.println(value);
//                return value;
//            }
//        });
//        env.execute();

        // create a TableSink
        TableSink sink = new CsvTableSink("./target/test_output", ",");
        // register the TableSink with a specific schema
        String[] fieldNames = {"timestamp", "data"};
        TypeInformation[] fieldTypes = {Types.LONG(), Types.LONG()};
        tEnv.registerTableSink("test_output", fieldNames, fieldTypes, sink);

        Table table = tEnv.scan(tableName);
        table.insertInto("test_output");

        tEnv.execEnv().execute();
    }
}
