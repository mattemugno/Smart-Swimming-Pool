package it.unipi.aide.iot.bean.mqtt_sensors;

import it.unipi.aide.iot.bean.samples.ChlorineSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

public class ChlorineSensor {
    public final String CHLORINE_TOPIC = "chlorine";
    public static int lastChlorineLevel;
    public static int lowerBound;
    public static int upperBound;

    public ChlorineSensor(){
        lowerBound = 40;
        upperBound = 80;
    }

    public void saveChlorineSample(ChlorineSample chlorineSample){
        //method to store in db the last sample read from broker
        lastChlorineLevel = chlorineSample.getChlorine();
        MySqlDbHandler.getInstance().insertChlorineSample(chlorineSample);

    }

    public static int getLastChlorineLevel(){
        return lastChlorineLevel;
    }
}
