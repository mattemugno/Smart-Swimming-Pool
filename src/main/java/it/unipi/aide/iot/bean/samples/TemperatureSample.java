package it.unipi.aide.iot.bean.samples;

import java.sql.Timestamp;

public class TemperatureSample {
    private int nodeId;
    private float temperature;
    private Timestamp timestamp;

    public TemperatureSample(int idNode, float temperature, Timestamp timestamp) {
        this.nodeId = idNode;
        this.temperature = temperature;
        this.timestamp = timestamp;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TemperatureSample{" +
                "idNode=" + nodeId +
                ", temperature=" + temperature +
                ", timestamp=" + timestamp +
                '}';
    }
}
