package io.github.ouyi.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ProducerSimple {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_BROKERS);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, Constants.CLIENT_ID);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RecordSerializer.class.getName());

        KafkaProducer<String, Record> kafkaProducer = new KafkaProducer(properties);
        Random random = new Random(4711);
        produce(kafkaProducer, random);
    }

    public static void produce(KafkaProducer<String, Record> kafkaProducer, Random random) throws ExecutionException, InterruptedException {
        Object value = new Record(System.currentTimeMillis(), random.nextInt(100));
        ProducerRecord<String, Record> producerRecord = new ProducerRecord(Constants.TOPIC_NAME, value);
        RecordMetadata metadata = kafkaProducer.send(producerRecord).get();
        System.out.println(metadata);
    }
}

