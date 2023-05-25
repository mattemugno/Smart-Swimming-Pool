package it.unipi.aide.iot.bean.sensors;

public class PresenceSensor {
    public final String PRESENCE_TOPIC = "presence";

    public float checkPresence(){
        //method to check presence in the pool by service handlers
        return 0;
    }

    public void savePresenceSample(){
        //method to store in db the last sample read from broker
    }
}
