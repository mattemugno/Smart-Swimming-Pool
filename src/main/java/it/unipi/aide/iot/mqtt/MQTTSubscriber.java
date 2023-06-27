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
import it.unipi.aide.iot.utility.Logger;
import it.unipi.aide.iot.utility.SimulationParameters;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


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
    private Logger logger;
    Gson parser = new Gson();

    public MQTTSubscriber()
    {
        temperatureSensor = new TemperatureSensor();
        presenceSensor = new PresenceSensor();
        chlorineSensor = new ChlorineSensor();
        waterLevelSensor = new WaterLevelSensor();
        logger = Logger.getInstance();

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

            if ((currentAvgTemperature < TemperatureSensor.lowerBound) & (Objects.equals(HeatingSystem.isStatus(), "OFF"))) {
                HeatingSystem.switchHeatingSystem("INC");
                mqttClient.publish(command_topic, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
                logger.logTemperature("Average level of Temperature too low: " + currentAvgTemperature + "°C, increase it");
            }

            else if ((currentAvgTemperature > TemperatureSensor.upperBound) & (Objects.equals(HeatingSystem.isStatus(), "OFF"))) {
                HeatingSystem.switchHeatingSystem("DEC");
                mqttClient.publish(command_topic, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
                logger.logTemperature("Average level of Temperature too high: " + currentAvgTemperature + "°C, decrease it");
            }

            else if((SimulationParameters.isManualCommandHeating() & (Objects.equals(HeatingSystem.isStatus(), "INC")) & currentAvgTemperature > TemperatureSensor.upperBound) ||
                   (SimulationParameters.isManualCommandHeating() & (Objects.equals(HeatingSystem.isStatus(), "DEC")) & currentAvgTemperature < TemperatureSensor.lowerBound)) {
                logger.logTemperature("Heating system have done it's work, temperature come back in the range");
                HeatingSystem.switchHeatingSystem("OFF");
                mqttClient.publish(command_topic, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
                SimulationParameters.setManualCommandHeating(false);
            }

            else if(((Objects.equals(HeatingSystem.isStatus(), "INC")) & (currentAvgTemperature > (float)(TemperatureSensor.upperBound + TemperatureSensor.lowerBound)/2)) ||
                    ((Objects.equals(HeatingSystem.isStatus(), "DEC")) & (currentAvgTemperature < (float)(TemperatureSensor.upperBound + TemperatureSensor.lowerBound)/2))){
                logger.logTemperature("Heating system have done it's work, temperature come back in the range");
                HeatingSystem.switchHeatingSystem("OFF");
                mqttClient.publish(command_topic, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }
            /*else if (currentAvgTemperature < TemperatureSensor.lowerBound){
                HeatingSystem.switchHeatingSystem("INC");
                mqttClient.publish(command_topic, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
                logger.logTemperature("Average level of Temperature too low: " + currentAvgTemperature + "°C, increase it");
            }
            else if (currentAvgTemperature > TemperatureSensor.upperBound){
                HeatingSystem.switchHeatingSystem("DEC");
                mqttClient.publish(command_topic, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
                logger.logTemperature("Average level of Temperature too high: " + currentAvgTemperature + "°C, decrease it");
            }*/
        }
        else if(topic.equals(waterLevelSensor.WATER_LEVEL_TOPIC)){
            String water_lev_command = "water-level-command";
            WaterLevelSample waterLevelSample = parser.fromJson(payload, WaterLevelSample.class);
            waterLevelSensor.saveWaterLevelSample(waterLevelSample);
            int currentWaterLevel = WaterLevelSensor.getCurrentWaterLevel();

            if ((currentWaterLevel < WaterLevelSensor.lowerBound) & (Objects.equals(WaterPump.isStatus(), "OFF")))
                sampleUnderWaterTh += 1;

            else if ((currentWaterLevel > WaterLevelSensor.upperBound) & (Objects.equals(WaterPump.isStatus(), "OFF")))
                sampleOverWaterTh += 1;

            else {
                sampleUnderWaterTh = 0;
                sampleOverWaterTh = 0;
            }

            if (sampleUnderWaterTh == 3){
                WaterPump.switchWaterPump("INC");
                mqttClient.publish(water_lev_command, new MqttMessage("INC".getBytes(StandardCharsets.UTF_8)));
                logger.logWaterLevel("Water level too high: " + currentWaterLevel + " %, WaterPump switched in DEC mode");
            }
            else if (sampleOverWaterTh == 3){
                WaterPump.switchWaterPump("DEC");
                mqttClient.publish(water_lev_command, new MqttMessage("DEC".getBytes(StandardCharsets.UTF_8)));
                logger.logWaterLevel("Water level too low: " + currentWaterLevel + " %, WaterPump switched in INC mode");
            }
            else if(((Objects.equals(WaterPump.isStatus(), "INC")) & (currentWaterLevel >= (float) (WaterLevelSensor.upperBound + WaterLevelSensor.lowerBound)/2)) ||
                    ((Objects.equals(WaterPump.isStatus(), "DEC")) & (currentWaterLevel <= (float) (WaterLevelSensor.upperBound + WaterLevelSensor.lowerBound)/2))){
                logger.logWaterLevel("Water level come back in the range");
                WaterPump.switchWaterPump("OFF");
                mqttClient.publish(water_lev_command, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            }

        }
        else if(topic.equals(chlorineSensor.CHLORINE_TOPIC)){
            String chlorine_command = "chlorine-command";
            ChlorineSample chlorineSample = parser.fromJson(payload, ChlorineSample.class);
            chlorineSensor.saveChlorineSample(chlorineSample);
            int currentChlorineLevel = ChlorineSensor.getLastChlorineLevel();

            if (currentChlorineLevel < ChlorineSensor.lowerBound)
                 sampleOutChlorineRange += 1;
            else
                sampleOutChlorineRange = 0;

            if (!ChlorineDispenser.lastStatus & (sampleOutChlorineRange == 3)){
                ChlorineDispenser.switchChlorineDispenser();
                mqttClient.publish(chlorine_command, new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
                logger.logChlorine("Average level of chlorine too low: " + currentChlorineLevel + "%, increase it");

            }

            else if(ChlorineDispenser.lastStatus & currentChlorineLevel >= (float)(ChlorineSensor.upperBound + ChlorineSensor.lowerBound)/2){
                ChlorineDispenser.switchChlorineDispenser();
                mqttClient.publish(chlorine_command, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
                logger.logChlorine("Switched OFF");
            }
        }
        else if (topic.equals(presenceSensor.PRESENCE_TOPIC)){
            String light_command = "light-command";
            PresenceSample presenceSample = parser.fromJson(payload, PresenceSample.class);
            presenceSensor.savePresenceSample(presenceSample);
            if((presenceSample.isPresence()) & (!Light.isLastStatus()) & (!SimulationParameters.isManualCommandLight())) {
                Light.lightSwitch(true);
                mqttClient.publish(light_command, new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
                logger.logPresence("Light switched ON");
            }
            else if((!presenceSample.isPresence()) & (Light.isLastStatus()) & (!SimulationParameters.isManualCommandLight())) {
                Light.lightSwitch(false);
                mqttClient.publish(light_command, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
                logger.logPresence("Light switched OFF");
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
        System.out.println(throwable);

        int attempts = 0;
        do {
            attempts++;
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
        //System.out.println("Subscribed to: " + presenceSensor.PRESENCE_TOPIC);
        logger.logInfo("Subscribed to: " + presenceSensor.PRESENCE_TOPIC);
        mqttClient.subscribe(temperatureSensor.TEMPERATURE_TOPIC);
        //System.out.println("Subscribed to: " + temperatureSensor.TEMPERATURE_TOPIC);
        logger.logInfo("Subscribed to: " + temperatureSensor.TEMPERATURE_TOPIC);
        mqttClient.subscribe(chlorineSensor.CHLORINE_TOPIC);
        //System.out.println("Subscribed to: " + chlorineSensor.CHLORINE_TOPIC);
        logger.logInfo("Subscribed to: " + chlorineSensor.CHLORINE_TOPIC);
        mqttClient.subscribe(waterLevelSensor.WATER_LEVEL_TOPIC);
        //System.out.println("Subscribed to: " + waterLevelSensor.WATER_LEVEL_TOPIC);
        logger.logInfo("Subscribed to: " + waterLevelSensor.WATER_LEVEL_TOPIC);
    }
}
