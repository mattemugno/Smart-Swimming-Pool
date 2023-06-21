package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class ChlorineSample {
    private int nodeId;
    private int chlorineLevel;

    public ChlorineSample(int nodeId, int chlorineLevel) {
        this.nodeId = nodeId;
        this.chlorineLevel = chlorineLevel;
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

    @Override
    public String toString() {
        return "ChlorineSample{" +
                "nodeId=" + nodeId +
                ", chlorineLevel=" + chlorineLevel +
                '}';
    }
}
