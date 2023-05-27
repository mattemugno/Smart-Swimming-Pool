package it.unipi.aide.iot.bean.samples;

public class PresenceSample {
    private int nodeId;
    private boolean presence;

    public PresenceSample(int nodeId, boolean presence) {
        this.nodeId = nodeId;
        this.presence = presence;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isPresence() {
        return presence;
    }

    public void setPresence(boolean presence) {
        this.presence = presence;
    }

    @Override
    public String toString() {
        return "PresenceSample{" +
                "nodeId=" + nodeId +
                ", presence=" + presence +
                '}';
    }
}
