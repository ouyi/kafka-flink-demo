package io.github.ouyi;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

public class WikipediaAnalysis {

    public static class WikipediaEditAccumulator {
        long bytes;
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<WikipediaEditEvent> edits = see.addSource(new WikipediaEditsSource());

        KeyedStream<WikipediaEditEvent, String> keyedEdits = edits
            .keyBy((KeySelector<WikipediaEditEvent, String>) wikipediaEditEvent -> wikipediaEditEvent.getUser());

        DataStream<Tuple2<String, Long>> result = keyedEdits
            .timeWindow(Time.seconds(5))
            .fold(new Tuple2<>("", 0L), new FoldFunction<WikipediaEditEvent, Tuple2<String, Long>>() {

                @Override
                public Tuple2<String, Long> fold(Tuple2<String, Long> accumulator, WikipediaEditEvent value) throws Exception {
                    accumulator.f0 = value.getUser();
                    accumulator.f1 += value.getByteDiff();
                    return accumulator;
                }
            });

        result.map(new MapFunction<Tuple2<String, Long>, String>() {

            @Override
            public String map(Tuple2<String, Long> value) throws Exception {
                return value.toString();
            }
        }).addSink(new FlinkKafkaProducer<String>("kafka:9092", "wiki-results", new SimpleStringSchema()));
        // To run this class from IDE, use localhost:19092

        see.execute();
    }
}
