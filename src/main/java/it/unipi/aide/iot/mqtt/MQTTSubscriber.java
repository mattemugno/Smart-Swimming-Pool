package it.unipi.aide.iot.mqtt;

import com.google.gson.Gson;
import it.unipi.aide.iot.bean.coap_actuators.ChlorineDispenser;
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
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;


public class MQTTSubscriber implements MqttCallback {
    public String BROKER = "tcp://127.0.0.1:1883";
    public String CLIENT_ID = "RemoteControlApp";
    public static int sampleUnderWaterTh = 0;
    public static int sampleOverWaterTh = 0;
    public static int sampleOutChlorineRange = 0;

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

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {
        String payload = new String(mqttMessage.getPayload());
        String command_topic = "temperature-command";

        if (topic.equals(temperatureSensor.TEMPERATURE_TOPIC)){
            TemperatureSample temperatureSample = parser.fromJson(payload, TemperatureSample.class);
            temperatureSensor.saveTemperatureSample(temperatureSample);
            float currentAvgTemperature = temperatureSensor.getAvgTemperature();

            if (currentAvgTemperature !=0 & currentAvgTemperature < TemperatureSensor.lowerBound) {
                HeatingSystem.switchHeatingSystem("INC");
                mqttClient.publish(command_topic, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
            }

            else if (currentAvgTemperature !=0 & currentAvgTemperature > TemperatureSensor.upperBound) {
                HeatingSystem.switchHeatingSystem("DEC");
                mqttClient.publish(command_topic, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
            }

            //else if (currentAvgTemperature == 0)
                //System.out.println("Not enough samples collected");

            else if((HeatingSystem.isStatus()) & (currentAvgTemperature >= TemperatureSensor.lowerBound & currentAvgTemperature <= TemperatureSensor.upperBound)) {
                HeatingSystem.switchHeatingSystem("OFF");
                mqttClient.publish(command_topic, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }
        }
        else if(topic.equals(waterLevelSensor.WATER_LEVEL_TOPIC)){
            String water_lev_command = "water-level-command";
            WaterLevelSample waterLevelSample = parser.fromJson(payload, WaterLevelSample.class);
            waterLevelSensor.saveWaterLevelSample(waterLevelSample);
            int currentWaterLevel = WaterLevelSensor.getCurrentWaterLevel();

            if (currentWaterLevel < WaterLevelSensor.lowerBound)
                sampleUnderWaterTh += 1;

            else if (currentWaterLevel > WaterLevelSensor.upperBound)
                sampleOverWaterTh += 1;

            else {
                sampleUnderWaterTh = 0;
                sampleOverWaterTh = 0;
            }

            if (sampleUnderWaterTh == 3){
                System.out.println("Switch on water pump in INC mode");
                WaterPump.switchWaterPump("INC");
                mqttClient.publish(water_lev_command, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
            }
            else if (sampleOverWaterTh == 3){
                System.out.println("Switch on water pump in DEC mode");
                WaterPump.switchWaterPump("DEC");
                mqttClient.publish(water_lev_command, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
            }
            else if (WaterPump.isStatus() & (currentWaterLevel >= WaterLevelSensor.lowerBound & currentWaterLevel <= WaterLevelSensor.upperBound)){
                System.out.println("Switch off water pump");
                WaterPump.switchWaterPump("OFF");
                mqttClient.publish(water_lev_command, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }

        }
        else if(topic.equals(chlorineSensor.CHLORINE_TOPIC)){
            ChlorineSample chlorineSample = parser.fromJson(payload, ChlorineSample.class);
            chlorineSensor.saveChlorineSample(chlorineSample);
            int currentChlorineLevel = ChlorineSensor.getLastChlorineLevel();

            if (currentChlorineLevel < ChlorineSensor.lowerBound)
                 sampleOutChlorineRange += 1;

            else if (currentChlorineLevel > ChlorineSensor.upperBound)
                sampleOutChlorineRange += 1;

            else
                sampleOutChlorineRange = 0;

            if (sampleOutChlorineRange == 3){
                System.out.println("Accendi dispenser");
                ChlorineDispenser.switchChlorineDispenser();
                mqttClient.publish(chlorineSensor.CHLORINE_TOPIC, new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
            }

            else if(ChlorineDispenser.lastStatus & currentChlorineLevel >= ChlorineSensor.lowerBound & currentChlorineLevel <= ChlorineSensor.upperBound){
                System.out.println("Spegni dispenser");
                ChlorineDispenser.switchChlorineDispenser();
                mqttClient.publish(chlorineSensor.CHLORINE_TOPIC, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }
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
    public void deliveryComplete(IMqttDeliveryToken token) {
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
        System.out.println("entrato");
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
