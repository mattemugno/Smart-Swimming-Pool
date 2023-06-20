package it.unipi.aide.iot.bean.mqtt_sensors;

import it.unipi.aide.iot.bean.samples.WaterLevelSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

import java.sql.Timestamp;

public class WaterLevelSensor {
    public final String WATER_LEVEL_TOPIC = "water_level";
    public static int currentWaterLevel;
    public static int upperBound;
    public static int lowerBound;

    public WaterLevelSensor(){
        lowerBound = 50;
        upperBound = 90;
    }

    public void saveWaterLevelSample(WaterLevelSample waterLevelSample){
        //method to store in db the last sample read from broker
        waterLevelSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        MySqlDbHandler.getInstance().insertWaterLevelSample(waterLevelSample);
        currentWaterLevel = waterLevelSample.getHeight();
    }

    public static int getCurrentWaterLevel(){
        return currentWaterLevel;
    }

}
