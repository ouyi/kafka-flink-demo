package io.github.ouyi.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class SoundVolumeProducer {

    public static void main(String[] args) {
        Properties properties = new Properties();
        String kafkaBrokers = args.length > 0 ? args[0] : Constants.KAFKA_BROKERS;
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, Constants.CLIENT_ID);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RecordSerializer.class.getName());

        KafkaProducer<String, Record> kafkaProducer = new KafkaProducer(properties);
        run(kafkaProducer);
    }

    static void run(KafkaProducer kafkaProducer) {
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
        final int bufferByteSize = 2048 * 8;

        TargetDataLine line;
        try {
            line = AudioSystem.getTargetDataLine(fmt);
            line.open(fmt, bufferByteSize);
        } catch(LineUnavailableException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }

        byte[] buf = new byte[bufferByteSize];
        float[] samples = new float[bufferByteSize / 2];

        float lastPeak = 0f;

        line.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }

            float rms = 0f;
            float peak = 0f;
            for(float sample : samples) {

                float abs = Math.abs(sample);
                if(abs > peak) {
                    peak = abs;
                }

                rms += sample * sample;
            }

            rms = (float)Math.sqrt(rms / samples.length);

            if(lastPeak > peak) {
                peak = lastPeak * 0.875f;
            }

            lastPeak = peak;

//            System.out.println(rms);

            produce(kafkaProducer, (int)(rms * 100));
        }
    }

    private static void produce(KafkaProducer kafkaProducer, int data) {
        Object record = new Record(ZonedDateTime.now( ZoneOffset.ofHours(2) ).truncatedTo(ChronoUnit.MILLIS).format( DateTimeFormatter.ISO_OFFSET_DATE_TIME ), data);
        ProducerRecord<String, Record> producerRecord = new ProducerRecord(Constants.TOPIC_NAME_INPUT, record);
        kafkaProducer.send(producerRecord);
        System.out.println(producerRecord);
    }
}
