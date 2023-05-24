package it.unipi.aide.iot.mqtt.sensors;

public class ChlorineSensor {
    public final String CHLORINE_TOPIC = "chlorine";

    public float getCurrentChlorineLevel(){
        //method to check current chlorine level by service handlers
        return 0;
    }

    public void saveChlorineSample(){
        //method to store in db the last sample read from broker
    }
}
