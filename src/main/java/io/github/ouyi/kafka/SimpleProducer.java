package io.github.ouyi.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.Random;

public class SimpleProducer {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        String kafkaBrokers = args.length > 0 ? args[0] : Constants.KAFKA_BROKERS;
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, Constants.CLIENT_ID);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RecordSerializer.class.getName());

        KafkaProducer<String, Record> kafkaProducer = new KafkaProducer(properties);
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            Thread.sleep(random.nextInt(600));
            produce(kafkaProducer, random);
        }
    }

    public static void produce(KafkaProducer<String, Record> kafkaProducer, Random random) {
        Object record = new Record(ZonedDateTime.now( ZoneOffset.ofHours(2) ).truncatedTo(ChronoUnit.MILLIS).format( DateTimeFormatter.ISO_OFFSET_DATE_TIME ), random.nextInt(100));
        ProducerRecord<String, Record> producerRecord = new ProducerRecord(Constants.TOPIC_NAME_INPUT, record);
        kafkaProducer.send(producerRecord);
        System.out.println(producerRecord);
    }
}
