package it.unipi.aide.iot.bean.mqtt_sensors;

import it.unipi.aide.iot.bean.samples.TemperatureSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

import java.sql.Timestamp;
import java.util.Arrays;

public class TemperatureSensor {
    public final String TEMPERATURE_TOPIC = "temperature";
    public static int[] lastTemperatureSamples;
    public static int currentIndex;
    public static int lowerBound;
    public static int upperBound;

    public TemperatureSensor(){
        lastTemperatureSamples = new int[10];
        currentIndex = 0;
        Arrays.fill(lastTemperatureSamples, 25);
        lowerBound = 20;
        upperBound = 30;
    }

    public static int getCurrentTemperature(){
        return lastTemperatureSamples[currentIndex];
    }

    public float getAvgTemperature()
    {
        int howMany = lastTemperatureSamples.length;
        float sum = 0;
        for (float sample : lastTemperatureSamples) {
            if (sample == 0)
                return 0;
            sum += sample;
        }
        return sum / howMany;
    }

    public void saveTemperatureSample(TemperatureSample temperatureSample){
        lastTemperatureSamples[currentIndex] = temperatureSample.getTemperature();
        currentIndex = (currentIndex + 1) % lastTemperatureSamples.length;

        MySqlDbHandler.getInstance().insertTemperatureSample(temperatureSample);
    }

}
