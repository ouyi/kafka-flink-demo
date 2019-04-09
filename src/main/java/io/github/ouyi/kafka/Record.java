package io.github.ouyi.kafka;

public class Record {
    private String timestamp;
    private long data;

    /**
     * Required for by {@link RecordDeserializer}
     */
    public Record() {

    }

    public Record(String timestamp, long data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Record{" +
            "timestamp=" + timestamp +
            ", data=" + data +
            '}';
    }
}
