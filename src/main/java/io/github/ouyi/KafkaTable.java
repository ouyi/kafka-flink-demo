package io.github.ouyi;

import io.github.ouyi.kafka.Constants;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.*;
import org.apache.flink.table.sources.wmstrategies.PunctuatedWatermarkAssigner;
import org.apache.flink.types.Row;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

public class KafkaTable {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        System.out.println(env.getConfig().getAutoWatermarkInterval());
//        env.getConfig().setAutoWatermarkInterval(1L);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.getTableEnvironment(env);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.GROUP_ID);

        tableEnv
            .connect(
                new Kafka()
                    .topic("test_input")
//                    .topic(Constants.TOPIC_NAME)
                    .properties(props)
                    .version("universal") // needed to avoid Issue 3
                    .startFromEarliest()
            )
            .withSchema(
                new Schema()
                    .field("ts", Types.SQL_TIMESTAMP()) // use a different field name to avoid Issue 1
                    .rowtime(
                        new Rowtime()
                            .timestampsFromField("timestamp")
//                            .watermarksPeriodicBounded(60000)
//                            .watermarksPeriodicAscending()
                            .watermarksFromStrategy(new PunctuatedWatermarkAssigner() {
                                @Override
                                public Watermark getWatermark(Row row, long timestamp) {
                                    return new Watermark(timestamp - 60000);
                                }
                            })
                    )
                    .field("data", Types.LONG())
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
                    .field("ts", Types.SQL_TIMESTAMP()) // this needs to match the query result field type, to avoid Issue 2
                    .field("m", Types.LONG())
                    .field("c", Types.LONG())
            )
            .inAppendMode()
            .registerTableSink("table_output");

//        tableEnv.scan("table_input").insertInto("table_output");
//        tableEnv.sqlUpdate("insert into table_output select * from table_input");

//        Table table = tableEnv.sqlQuery("select TUMBLE_START(ts, INTERVAL '1' MINUTE) AS wstart, " +
//            "extract(MONTH FROM CAST(TUMBLE_START(ts, INTERVAL '1' MINUTE) as DATE)) as wmonth, " +
//            "extract(DAY FROM CAST(TUMBLE_START(ts, INTERVAL '1' MINUTE) as DATE)) as wday, " +
//            "count(1) as cnt " +
//            "from table_input " +
//            "group by tumble(ts, INTERVAL '1' MINUTE)");

        execute(env, tableEnv);
    }

    private static void execute(StreamExecutionEnvironment env, StreamTableEnvironment tableEnv) throws Exception {
        Table table = tableEnv.sqlQuery("select TUMBLE_START(ts, INTERVAL '1' SECOND) AS wstart, max(data) as m, count(1) as c from table_input group by tumble(ts, INTERVAL '1' SECOND)");
//        Table table = tableEnv.sqlQuery("select TUMBLE_START(ts, INTERVAL '1' MINUTE) AS wstart, max(data) as m, count(1) as c from table_input where ts <= '2019-03-06 16:02:00' group by tumble(ts, INTERVAL '1' MINUTE)");

        DataStream<Row> dataStream = tableEnv.toAppendStream(table, Row.class);
        dataStream.print();

        Table table2 = tableEnv.sqlQuery("select * from table_input");
//        Table table2 = tableEnv.sqlQuery("select * from table_input where ts <= '2019-03-06 16:02:00'");

        DataStream<Row> dataStream2 = tableEnv.toAppendStream(table2, Row.class);
        dataStream2.print();

        tableEnv.registerTable("table_tmp", table);
        tableEnv.scan("table_tmp").insertInto("table_output");
//        tableEnv.sqlUpdate("insert into table_output select * from table_input");

        env.execute(KafkaTable.class.getName());
    }
}

/*
Issue 1:

Exception in thread "main" org.apache.flink.table.api.ValidationException: Field 'timestamp' could not be resolved by the field mapping.
	at org.apache.flink.table.sources.TableSourceUtil$.resolveInputField(TableSourceUtil.scala:492)
	at org.apache.flink.table.sources.TableSourceUtil$.$anonfun$resolveInputFields$1(TableSourceUtil.scala:521)
	at scala.collection.TraversableLike.$anonfun$map$1(TraversableLike.scala:233)
	at scala.collection.IndexedSeqOptimized.foreach(IndexedSeqOptimized.scala:32)
	at scala.collection.IndexedSeqOptimized.foreach$(IndexedSeqOptimized.scala:29)
	at scala.collection.mutable.ArrayOps$ofRef.foreach(ArrayOps.scala:194)
	at scala.collection.TraversableLike.map(TraversableLike.scala:233)
	at scala.collection.TraversableLike.map$(TraversableLike.scala:226)
	at scala.collection.mutable.ArrayOps$ofRef.map(ArrayOps.scala:194)
	at org.apache.flink.table.sources.TableSourceUtil$.resolveInputFields(TableSourceUtil.scala:521)
	at org.apache.flink.table.sources.TableSourceUtil$.validateTableSource(TableSourceUtil.scala:127)
	at org.apache.flink.table.plan.schema.StreamTableSourceTable.<init>(StreamTableSourceTable.scala:33)
	at org.apache.flink.table.api.StreamTableEnvironment.registerTableSourceInternal(StreamTableEnvironment.scala:150)
	at org.apache.flink.table.api.TableEnvironment.registerTableSource(TableEnvironment.scala:541)
	at org.apache.flink.table.descriptors.ConnectTableDescriptor.registerTableSource(ConnectTableDescriptor.scala:47)
	at io.github.ouyi.KafkaTable.main(KafkaTable.java:49)

Process finished with exit code 1

http://mail-archives.apache.org/mod_mbox/flink-user/201811.mbox/<CAGkDawkR5n2kRdCgHPQ+9ce3gN30j+D_KsH6v2HyxWYcv2iNbw@mail.gmail.com>


Issue 2:

Exception in thread "main" org.apache.flink.table.api.ValidationException: Field types of query result and registered TableSink table_output do not match.
Query result schema: [data: Long, ts: Timestamp]
TableSink schema:    [data: Long, ts: Long]
	at org.apache.flink.table.api.TableEnvironment.insertInto(TableEnvironment.scala:876)
	at org.apache.flink.table.api.Table.insertInto(table.scala:918)
	at io.github.ouyi.KafkaTable.main(KafkaTable.java:71)

Process finished with exit code 1


Issue 3:

Exception in thread "main" org.apache.flink.table.api.NoMatchingTableFactoryException: Could not find a suitable table factory for 'org.apache.flink.table.factories.StreamTableSourceFactory' in
the classpath.

Reason: No context matches.

The following properties are requested:
connector.properties.0.key=group.id
connector.properties.0.value=consumerGroup1
connector.properties.1.key=bootstrap.servers
connector.properties.1.value=localhost:19092
connector.property-version=1
connector.startup-mode=earliest-offset
connector.topic=test
connector.type=kafka
format.derive-schema=true
format.property-version=1
format.type=json
schema.0.name=data
schema.0.type=BIGINT
schema.1.name=ts
schema.1.rowtime.timestamps.from=timestamp
schema.1.rowtime.timestamps.type=from-field
schema.1.rowtime.watermarks.delay=1000
schema.1.rowtime.watermarks.type=periodic-bounded
schema.1.type=TIMESTAMP
update-mode=append

The following factories have been considered:
org.apache.flink.table.sources.CsvBatchTableSourceFactory
org.apache.flink.table.sources.CsvAppendTableSourceFactory
org.apache.flink.table.sinks.CsvBatchTableSinkFactory
org.apache.flink.table.sinks.CsvAppendTableSinkFactory
org.apache.flink.formats.json.JsonRowFormatFactory
org.apache.flink.streaming.connectors.kafka.KafkaTableSourceSinkFactory

	at org.apache.flink.table.factories.TableFactoryService$.filterByContext(TableFactoryService.scala:218)
	at org.apache.flink.table.factories.TableFactoryService$.findInternal(TableFactoryService.scala:134)
	at org.apache.flink.table.factories.TableFactoryService$.find(TableFactoryService.scala:81)
	at org.apache.flink.table.factories.TableFactoryUtil$.findAndCreateTableSource(TableFactoryUtil.scala:49)
	at org.apache.flink.table.descriptors.ConnectTableDescriptor.registerTableSource(ConnectTableDescriptor.scala:46)
	at io.github.ouyi.KafkaTable.main(KafkaTable.java:49)

Process finished with exit code 1

https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kafka.html

 */
