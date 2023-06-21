package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class TemperatureSample {
    private int nodeId;
    private int temperature;

    public TemperatureSample(int idNode, int temperature) {
        this.nodeId = idNode;
        this.temperature = temperature;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "TemperatureSample{" +
                "idNode=" + nodeId +
                ", temperature=" + temperature +
                '}';
    }
}
