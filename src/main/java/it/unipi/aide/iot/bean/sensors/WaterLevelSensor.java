package it.unipi.aide.iot.bean.sensors;

public class WaterLevelSensor {
    public final String WATER_LEVEL_TOPIC = "water_level";

    public float getCurrentWaterLevel(){
        //method to check current temperature by service handlers
        return 0;
    }

    public void saveWaterLevelSample(){
        //method to store in db the last sample read from broker
    }

}
