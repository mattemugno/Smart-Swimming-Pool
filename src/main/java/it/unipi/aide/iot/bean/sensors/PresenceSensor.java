package it.unipi.aide.iot.bean.sensors;

import it.unipi.aide.iot.bean.samples.PresenceSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

public class PresenceSensor {
    public final String PRESENCE_TOPIC = "presence";

    public float checkPresence(){
        //method to check presence in the pool by service handlers
        return 0;
    }

    public void savePresenceSample(PresenceSample presenceSample){
        //method to store in db the last sample read from broker
        MySqlDbHandler.getInstance().insertPresenceSample(presenceSample);
    }
}
