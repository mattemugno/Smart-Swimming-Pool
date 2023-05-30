package it.unipi.aide.iot.bean.mqtt_sensors;

import it.unipi.aide.iot.bean.samples.WaterLevelSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

import java.sql.Timestamp;

public class WaterLevelSensor {
    public final String WATER_LEVEL_TOPIC = "water_level";
    public static float currentWaterLevel;
    public static float upperBound;
    public static float lowerBound;

    public void saveWaterLevelSample(WaterLevelSample waterLevelSample){
        //method to store in db the last sample read from broker
        waterLevelSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        MySqlDbHandler.getInstance().insertWaterLevelSample(waterLevelSample);
    }

    public static float getCurrentWaterLevel(){
        return currentWaterLevel;
    }

}
