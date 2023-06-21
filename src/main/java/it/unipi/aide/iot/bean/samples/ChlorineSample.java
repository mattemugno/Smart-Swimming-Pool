package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class ChlorineSample {
    private int nodeId;
    private int chlorineLevel;
    private Timestamp timestamp;

    public ChlorineSample(int nodeId, int chlorineLevel) {
        this.nodeId = nodeId;
        this.chlorineLevel = chlorineLevel;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChlorineLevel() {
        return chlorineLevel;
    }

    public void setChlorineLevel(int chlorineLevel) {
        this.chlorineLevel = chlorineLevel;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ChlorineSample{" +
                "nodeId=" + nodeId +
                ", chlorineLevel=" + chlorineLevel +
                ", timestamp=" + timestamp +
                '}';
    }
}
