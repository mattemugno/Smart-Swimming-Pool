package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class WaterLevelSample {
    private int nodeId;
    private int height;

    public WaterLevelSample(int nodeId, int height) {
        this.nodeId = nodeId;
        this.height = height;
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

    @Override
    public String toString() {
        return "WaterLevelSample{" +
                "nodeId=" + nodeId +
                ", height=" + height +
                '}';
    }
}
