package it.unipi.aide.iot.bean.sensors;

import it.unipi.aide.iot.bean.samples.TemperatureSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

import java.sql.Timestamp;

public class TemperatureSensor {
    public final String TEMPERATURE_TOPIC = "temperature";

    public float getCurrentTemperature(){
        //method to check current temperature by service handlers
        return 0;
    }

    public void saveTemperatureSample(TemperatureSample temperatureSample){
        //method to store in db the last sample read from broker
        temperatureSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        MySqlDbHandler.getInstance().insertTemperatureSample(temperatureSample);
    }

}
