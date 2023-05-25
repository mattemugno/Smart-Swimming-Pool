package it.unipi.aide.iot.bean.sensors;

public class TemperatureSensor {
    public final String TEMPERATURE_TOPIC = "temperature";

    public float getCurrentTemperature(){
        //method to check current temperature by service handlers
        return 0;
    }

    public void saveTemperatureSample(){
        //method to store in db the last sample read from broker
    }

}
