package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class WaterLevelSample {
    private int nodeId;
    private int height;
    private Timestamp timestamp;

    public WaterLevelSample(int nodeId, int height, Timestamp timestamp) {
        this.nodeId = nodeId;
        this.height = height;
        this.timestamp = timestamp;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "WaterLevelSample{" +
                "nodeId=" + nodeId +
                ", height=" + height +
                ", timestamp=" + timestamp +
                '}';
    }
}
