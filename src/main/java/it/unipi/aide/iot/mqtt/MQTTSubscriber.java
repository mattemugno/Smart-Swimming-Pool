package it.unipi.aide.iot.mqtt;

import com.google.gson.Gson;
import it.unipi.aide.iot.bean.sensors.ChlorineSensor;
import it.unipi.aide.iot.bean.sensors.PresenceSensor;
import it.unipi.aide.iot.bean.sensors.TemperatureSensor;
import it.unipi.aide.iot.bean.sensors.WaterLevelSensor;
import org.eclipse.paho.client.mqttv3.*;

public class MQTTSubscriber implements MqttCallback {

    private MqttClient mqttClient = null;
    private final String BROKER = "tcp://127.0.0.1:1883";
    private final String CLIENT_ID = "RemoteControlApp";
    private final int SECONDS_TO_WAIT_FOR_RECONNECTION = 5;
    private final int MAX_RECONNECTION_ATTEMPTS = 10;
    private Gson parser;
    private PresenceSensor presenceSensor;
    private ChlorineSensor chlorineSensor;
    private WaterLevelSensor waterLevelSensor;
    private TemperatureSensor temperatureSensor;

    //private Logger logger;

    public MQTTSubscriber()
    {
        parser = new Gson();
        temperatureSensor = new TemperatureSensor();
        presenceSensor = new PresenceSensor();
        chlorineSensor = new ChlorineSensor();
        waterLevelSensor = new WaterLevelSensor();
        do {
            try {
                mqttClient = new MqttClient(BROKER, CLIENT_ID);
                System.out.println("Connecting to the broker: " + BROKER);
                mqttClient.setCallback( this );
                brokerConnection();
            }
            catch(MqttException me)
            {
                System.out.println("Connection error! Retrying ...");
            }
        }while(!mqttClient.isConnected());
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection with the Broker lost!");

        int attempts = 0;
        do {
            attempts++; // first iteration iter=1
            if (attempts > MAX_RECONNECTION_ATTEMPTS)
            {
                System.err.println("Reconnection with the broker not possible!");
                System.exit(-1);
            }
            try
            {
                Thread.sleep((long) SECONDS_TO_WAIT_FOR_RECONNECTION * 1000 * attempts);
                System.out.println("New attempt to connect to the broker...");
                brokerConnection();
            }
            catch (MqttException | InterruptedException e)
            {
                e.printStackTrace();
            }
        } while (!this.mqttClient.isConnected());
        System.out.println("Connection with the Broker restored!");

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String payload = new String(mqttMessage.getPayload());
        if (topic.equals(temperatureSensor.TEMPERATURE_TOPIC)){
            temperatureSensor.saveTemperatureSample();
        }
        else if(topic.equals(waterLevelSensor.WATER_LEVEL_TOPIC)){
            waterLevelSensor.saveWaterLevelSample();
        }
        else if(topic.equals(chlorineSensor.CHLORINE_TOPIC)){
            chlorineSensor.saveChlorineSample();
        }
        else if (topic.equals(presenceSensor.PRESENCE_TOPIC)){
            presenceSensor.savePresenceSample();
        }
        else{
            System.out.println("You are not subscripted to the '" + topic + "' topic");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }

    /**
     * This function is used to try to connect to the broker
     */
    private void brokerConnection () throws MqttException {
        mqttClient.connect();
        mqttClient.subscribe(presenceSensor.PRESENCE_TOPIC);
        System.out.println("Subscribed to: " + presenceSensor.PRESENCE_TOPIC);
        mqttClient.subscribe(temperatureSensor.TEMPERATURE_TOPIC);
        System.out.println("Subscribed to: " + temperatureSensor.TEMPERATURE_TOPIC);
        mqttClient.subscribe(chlorineSensor.CHLORINE_TOPIC);
        System.out.println("Subscribed to: " + chlorineSensor.CHLORINE_TOPIC);
        mqttClient.subscribe(waterLevelSensor.WATER_LEVEL_TOPIC);
        System.out.println("Subscribed to: " + waterLevelSensor.WATER_LEVEL_TOPIC);
    }
}
