package it.unipi.aide.iot.bean.mqtt_sensors;

import it.unipi.aide.iot.bean.samples.ChlorineSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

public class ChlorineSensor {
    public final String CHLORINE_TOPIC = "chlorine";
    public static float lastChlorineLevel;
    public static float lowerBound;
    public static float upperBound;

    public void saveChlorineSample(ChlorineSample chlorineSample){
        //method to store in db the last sample read from broker
        MySqlDbHandler.getInstance().insertChlorineSample(chlorineSample);

    }

    public static float getLastChlorineLevel(){
        return lastChlorineLevel;
    }
}
