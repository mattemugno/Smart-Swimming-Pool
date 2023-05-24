package it.unipi.aide.iot.mqtt;

import com.google.gson.Gson;
import it.unipi.aide.iot.mqtt.sensors.ChlorineSensor;
import it.unipi.aide.iot.mqtt.sensors.PresenceSensor;
import it.unipi.aide.iot.mqtt.sensors.TemperatureSensor;
import it.unipi.aide.iot.mqtt.sensors.WaterLevelSensor;
import org.eclipse.paho.client.mqttv3.*;

public class MQTTSubscriber implements MqttCallback {

    private MqttClient mqttClient = null;
    private final String BROKER = "tcp://127.0.0.1:1883";
    private final String CLIENT_ID = "RemoteControlApp";
    private Gson parser;
    private PresenceSensor presenceSensor;
    private ChlorineSensor chlorineSensor;
    private WaterLevelSensor waterLevelSensor;
    private TemperatureSensor temperatureSensor;

    //private Logger logger;

    public MQTTSubscriber()
    {
        parser = new Gson();
        //logger = Logger.getInstance();
        temperatureSensor = new TemperatureSensor();
        presenceSensor = new PresenceSensor();
        chlorineSensor = new ChlorineSensor();
        waterLevelSensor = new WaterLevelSensor();
        do {
            try {
                mqttClient = new MqttClient(BROKER, CLIENT_ID);
                System.out.println("Connecting to the broker: " + BROKER);
                mqttClient.setCallback( this );
                brokerSubscription();
            }
            catch(MqttException me)
            {
                System.out.println("Connection error! Retrying ...");
            }
        }while(!mqttClient.isConnected());
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    /**
     * This function is used to try to connect to the broker
     */
    private void brokerSubscription () throws MqttException {
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
