package it.unipi.aide.iot.mqtt;

import com.google.gson.Gson;
import it.unipi.aide.iot.bean.coap_actuators.HeatingSystem;
import it.unipi.aide.iot.bean.coap_actuators.Light;
import it.unipi.aide.iot.bean.coap_actuators.WaterPump;
import it.unipi.aide.iot.bean.samples.ChlorineSample;
import it.unipi.aide.iot.bean.samples.PresenceSample;
import it.unipi.aide.iot.bean.samples.TemperatureSample;
import it.unipi.aide.iot.bean.samples.WaterLevelSample;
import it.unipi.aide.iot.bean.mqtt_sensors.ChlorineSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.PresenceSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.TemperatureSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.WaterLevelSensor;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;


public class MQTTSubscriber implements MqttCallback {
    public String BROKER = "tcp://127.0.0.1:1883";
    public String CLIENT_ID = "RemoteControlApp";
    public static int a = 0;
    public static int b = 0;

    private MqttClient mqttClient = null;
    private final PresenceSensor presenceSensor;
    private final ChlorineSensor chlorineSensor;
    private final WaterLevelSensor waterLevelSensor;
    private final TemperatureSensor temperatureSensor;
    Gson parser = new Gson();

    public MQTTSubscriber()
    {
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
    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {
        String payload = new String(mqttMessage.getPayload());

        if (topic.equals(temperatureSensor.TEMPERATURE_TOPIC)){
            TemperatureSample temperatureSample = parser.fromJson(payload, TemperatureSample.class);
            temperatureSensor.saveTemperatureSample(temperatureSample);
            float currentAvgTemperature = temperatureSensor.getAvgTemperature();

            if (currentAvgTemperature !=0 & currentAvgTemperature < TemperatureSensor.lowerBound) {
                HeatingSystem.switchHeatingSystem("HOT");
                mqttClient.publish(temperatureSensor.TEMPERATURE_TOPIC, new MqttMessage("HOT".getBytes(StandardCharsets.UTF_8)));
            }

            else if (currentAvgTemperature !=0 & currentAvgTemperature > TemperatureSensor.upperBound) {
                HeatingSystem.switchHeatingSystem("COLD");
                mqttClient.publish(temperatureSensor.TEMPERATURE_TOPIC, new MqttMessage("COLD".getBytes(StandardCharsets.UTF_8)));
            }

            else if (currentAvgTemperature == 0)
                System.out.println("Not enough samples collected");

            else if((HeatingSystem.isStatus()) & (currentAvgTemperature >= TemperatureSensor.lowerBound & currentAvgTemperature <= TemperatureSensor.upperBound)) {
                HeatingSystem.switchHeatingSystem("OFF");
                mqttClient.publish(temperatureSensor.TEMPERATURE_TOPIC, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }
        }
        else if(topic.equals(waterLevelSensor.WATER_LEVEL_TOPIC)){
            WaterLevelSample waterLevelSample = parser.fromJson(payload, WaterLevelSample.class);
            waterLevelSensor.saveWaterLevelSample(waterLevelSample);
            float currentWaterLevel = WaterLevelSensor.getCurrentWaterLevel();

            if (currentWaterLevel !=0 & currentWaterLevel < WaterLevelSensor.lowerBound)
                a += 1;

            else if (currentWaterLevel !=0 & currentWaterLevel > WaterLevelSensor.upperBound)
                b += 1;

            else {
                a = 0;
                b = 0;
            }

            if (a == 3){
                WaterPump.switchWaterPump("INC");
                mqttClient.publish(waterLevelSensor.WATER_LEVEL_TOPIC, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
            }
            else if (b == 3){
                WaterPump.switchWaterPump("DEC");
                mqttClient.publish(waterLevelSensor.WATER_LEVEL_TOPIC, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
            }
            else if (WaterPump.isStatus() & (currentWaterLevel >= WaterLevelSensor.lowerBound & currentWaterLevel <= WaterLevelSensor.upperBound)){
                WaterPump.switchWaterPump("OFF");
                mqttClient.publish(waterLevelSensor.WATER_LEVEL_TOPIC, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }

        }
        else if(topic.equals(chlorineSensor.CHLORINE_TOPIC)){
            ChlorineSample chlorineSample = parser.fromJson(payload, ChlorineSample.class);
            chlorineSensor.saveChlorineSample(chlorineSample);
        }
        else if (topic.equals(presenceSensor.PRESENCE_TOPIC)){
            PresenceSample presenceSample = parser.fromJson(payload, PresenceSample.class);
            presenceSensor.savePresenceSample(presenceSample);
            if((presenceSample.isPresence()) & (!Light.isLastStatus())) {
                Light.lightSwitch(true);
                mqttClient.publish(presenceSensor.PRESENCE_TOPIC, new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
                System.out.println("Light switched on");
            }
            else if((!presenceSample.isPresence()) & (Light.isLastStatus())) {
                Light.lightSwitch(false);
                mqttClient.publish(presenceSensor.PRESENCE_TOPIC, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
                System.out.println("Light switched off");
            }
        }
        else{
            System.out.println("You are not subscribed to the '" + topic + "' topic");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Message correctly delivered");
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection with the Broker lost!");

        int attempts = 0;
        do {
            attempts++; // first iteration iter=1
            int MAX_RECONNECTION_ATTEMPTS = 10;
            if (attempts > MAX_RECONNECTION_ATTEMPTS)
            {
                System.err.println("Reconnection with the broker not possible!");
                System.exit(-1);
            }
            try
            {
                int SECONDS_TO_WAIT_FOR_RECONNECTION = 5;
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
