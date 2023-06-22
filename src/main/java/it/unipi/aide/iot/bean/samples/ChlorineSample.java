package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class ChlorineSample {
    private int nodeId;
    private int chlorine;

    public ChlorineSample(int nodeId, int chlorine) {
        this.nodeId = nodeId;
        this.chlorine = chlorine;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChlorine() {
        return chlorine;
    }

    public void setChlorine(int chlorine) {
        this.chlorine = chlorine;
    }

    @Override
    public String toString() {
        return "ChlorineSample{" +
                "nodeId=" + nodeId +
                ", chlorine=" + chlorine +
                '}';
    }
}
