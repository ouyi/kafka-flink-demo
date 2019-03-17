package io.github.ouyi.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class RecordDeserializer implements Deserializer<Record> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public Record deserialize(String topic, byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        Record record = null;
        try {
            record = mapper.readValue(data, Record.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return record;
    }

    @Override
    public void close() {

    }
}
