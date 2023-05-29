package it.unipi.aide.iot.bean.sensors;

import it.unipi.aide.iot.bean.samples.TemperatureSample;
import it.unipi.aide.iot.persistence.MySqlDbHandler;

import java.sql.Timestamp;
import java.util.Arrays;

public class TemperatureSensor {
    public final String TEMPERATURE_TOPIC = "temperature";
    public float[] lastTemperatureSamples;

    public TemperatureSensor(){
        lastTemperatureSamples = new float[10];
        Arrays.fill(lastTemperatureSamples, 0);
    }

    public float getCurrentTemperature(){
        //method to check current temperature by service handlers
        return 0;
    }

    public float getAvgTemperature()
    {
        for (float sample : lastTemperatureSamples)
            if (sample == 0)
                return 0;

        int howMany = lastTemperatureSamples.length;
        float sum = 0;
        for (float lastTemperatureSample : lastTemperatureSamples) sum += lastTemperatureSample;
        return sum / howMany;
    }

    public void saveTemperatureSample(TemperatureSample temperatureSample){
        for(int i=0 ; i<lastTemperatureSamples.length; i++) {
            if (lastTemperatureSamples[i] == 0)
                lastTemperatureSamples[i] = temperatureSample.getTemperature();
            else if (i == lastTemperatureSamples.length - 1)
                lastTemperatureSamples[lastTemperatureSamples.length - 1] = temperatureSample.getTemperature();
        }

        temperatureSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        MySqlDbHandler.getInstance().insertTemperatureSample(temperatureSample);
    }

}
