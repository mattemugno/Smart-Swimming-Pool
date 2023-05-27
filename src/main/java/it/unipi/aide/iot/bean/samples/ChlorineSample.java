package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class ChlorineSample {
    private int nodeId;
    private float chlorineLevel;
    private Timestamp timestamp;

    public ChlorineSample(int nodeId, float chlorineLevel, Timestamp timestamp) {
        this.nodeId = nodeId;
        this.chlorineLevel = chlorineLevel;
        this.timestamp = timestamp;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public float getChlorineLevel() {
        return chlorineLevel;
    }

    public void setChlorineLevel(float chlorineLevel) {
        this.chlorineLevel = chlorineLevel;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
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
