package it.unipi.aide.iot.bean.sensors;

import it.unipi.aide.iot.bean.samples.ChlorineSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

public class ChlorineSensor {
    public final String CHLORINE_TOPIC = "chlorine";

    public float getCurrentChlorineLevel(){
        //method to check current chlorine level by pool handlers
        return 0;
    }

    public void saveChlorineSample(ChlorineSample chlorineSample){
        //method to store in db the last sample read from broker
        MySqlDbHandler.getInstance().insertChlorineSample(chlorineSample);

    }
}
