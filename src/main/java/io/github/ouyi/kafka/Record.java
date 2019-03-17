package io.github.ouyi.kafka;

public class Record {
    private long timestamp;
    private long data;

    /**
     * Required for by {@link RecordDeserializer}
     */
    public Record() {

    }

    public Record(long timestamp, long data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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
