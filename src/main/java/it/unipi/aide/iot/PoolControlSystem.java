package it.unipi.aide.iot;

import it.unipi.aide.iot.bean.coap_actuators.ChlorineDispenser;
import it.unipi.aide.iot.bean.coap_actuators.HeatingSystem;
import it.unipi.aide.iot.bean.coap_actuators.Light;
import it.unipi.aide.iot.bean.coap_actuators.WaterPump;
import it.unipi.aide.iot.bean.mqtt_sensors.ChlorineSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.PresenceSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.TemperatureSensor;
import it.unipi.aide.iot.bean.mqtt_sensors.WaterLevelSensor;
import it.unipi.aide.iot.coap.CoapRegistrationServer;
import it.unipi.aide.iot.mqtt.MQTTSubscriber;
import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class PoolControlSystem {
    public static void main(String[] args) throws SQLException, MqttException {
        String BROKER = "tcp://127.0.0.1:1883";
        String CLIENT_ID = "RemoteControlApp";
        PresenceSensor presenceSensor = new PresenceSensor();
        ChlorineSensor chlorineSensor = new ChlorineSensor();
        WaterLevelSensor waterLevelSensor = new WaterLevelSensor();
        TemperatureSensor temperatureSensor = new TemperatureSensor();
        CoapRegistrationServer coapRegistrationServer = new CoapRegistrationServer();
        coapRegistrationServer.start();
        new MQTTSubscriber();
        MySqlDbHandler mySqlDbHandler = MySqlDbHandler.getInstance();
        Connection connection = mySqlDbHandler.getConnection();
        MqttClient mqttClient = new MqttClient(BROKER, CLIENT_ID);

        printCommands();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String[] arguments;

        while (true) {
            try {
                command = bufferedReader.readLine();
                arguments = command.split(" ");
                switch(arguments[0]){
                    case "!get_temperature":
                        getTemperature(TemperatureSensor.getCurrentTemperature());
                        break;
                    case "!set_temperature":
                        setTemperatureBounds(arguments);
                        break;
                    case "!start_heater":
                        startHeater(arguments);
                        break;
                    case "!stop_heater":
                        stopHeater();
                        break;
                    case "!get_chlorine":
                        getChlorine(ChlorineSensor.getLastChlorineLevel());
                        break;
                    case "!set_chlorine":
                        setChlorine(arguments);
                        break;
                    case "!start_chlorine":
                        startChlorineDispenser();
                        break;
                    case "!stop_chlorine":
                        stopChlorineDispenser();
                        break;
                    case "!get_water_level":
                        getWaterLevel(WaterLevelSensor.getCurrentWaterLevel());
                        break;
                    case "!set_water_level":
                        setWaterLevel(arguments, "water-level-command", mqttClient);
                        break;
                    case "!start_water_pump":
                        startWaterPump(arguments, "water-level-command", mqttClient);
                        break;
                    case "!stop_water_pump":
                        stopWaterPump("water-level-command", mqttClient);
                        break;
                    case "!get_presence":
                        getPresence();
                        break;
                    case "!set_color":
                        setLightColor(arguments);
                        break;
                    case "!start_light":
                        startLight();
                        break;
                    case "!stop_light":
                        stopLight();
                        break;
                    case "!exit":
                        System.out.println("Bye!");
                        coapRegistrationServer.stop();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Command not valid, try again!\n");
                        break;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void printCommands(){
        System.out.println("***************************** SMART SWIMMING POOL *****************************\n" +
                "The following commands are available:\n" +
                "1) !get_temp --> recovers the last temperature measurement\n" +
                "2) !set_temp_th <lower bound> <upper bound> --> sets the range within which the temperature must stay in the pool\n" +
                "3) !start_heater <mode> --> starts heating system\n" +
                "4) !stop_heater --> stops heating system\n" +
                "5) !get_chlorine --> recovers the last chlorine level measurement\n" +
                "6) !set_chlorine <lower bound> <upper bound> --> sets the range within which the chlorine must stay in the pool\n" +
                "7) !start_chlorine <id> --> starts chlorine level dispenser\n" +
                "8) !stop_chlorine <id> --> stops chlorine level dispenser\n" +
                "9) !get_water_level --> recovers the last water level measurement\n" +
                "10) !set_water_level <lower bound> <upper bound> --> sets the limit below which the water level must stay\n" +
                "11) !start_water_pump <mode> --> starts water pump\n" +
                "12) !stop_water_pump --> stops water pump\n" +
                "13) !set_color <id> <color> --> sets the light color (GREEN, YELLOW or RED)\n" +
                "14) !get_presence --> check if there is someone in the pool\n" +
                "15) !start_light <id> --> starts light\n" +
                "16) !stop_light <id> --> stops light\n" +
                "17) !exit --> terminates the program\n"
        );
    }

    private static void getTemperature(float currentTemperature) {
        System.out.println("Current temperature is: " + currentTemperature);
    }

    private static void setTemperatureBounds(String[] arguments) {
        if (arguments.length != 3){
            System.out.println("Missing argument in the request");
            return;
        }
        float lowerBound = Float.parseFloat(arguments[1]);
        float upperBound = Float.parseFloat(arguments[2]);
        if(upperBound < lowerBound) {
            System.out.println("ERROR: The upper bound must be larger than the lower bound\n");
            return;
        }
        TemperatureSensor.lowerBound = lowerBound;
        TemperatureSensor.upperBound = upperBound;
    }

    private static void startHeater(String[] arguments) {
        if (arguments.length != 2){
            System.out.println("Missing argument/s in the request");
            return;
        }
        if(!Objects.equals(arguments[1], "HOT") & !Objects.equals(arguments[1], "COLD")){
            System.out.println("Not valid mode");
        }
        HeatingSystem.switchHeatingSystem(arguments[1]);

    }

    private static void stopHeater() {
        HeatingSystem.switchHeatingSystem("OFF");
    }

    private static void getChlorine(float lastChlorineLevel) {
        System.out.println("Current chlorine level is: " + lastChlorineLevel);
    }

    private static void setChlorine(String[] arguments) {
        if (arguments.length != 3){
            System.out.println("Missing argument in the request");
            return;
        }
        float lowerBound = Float.parseFloat(arguments[1]);
        float upperBound = Float.parseFloat(arguments[2]);
        if(upperBound < lowerBound) {
            System.out.println("ERROR: The upper bound must be larger than the lower bound\n");
            return;
        }
        ChlorineSensor.lowerBound = lowerBound;
        ChlorineSensor.upperBound = upperBound;
    }

    private static void startChlorineDispenser() {
        if(ChlorineDispenser.lastStatus)
            System.out.println("Chlorine dispenser is already active");
        else
            ChlorineDispenser.switchChlorineDispenser();
    }

    private static void stopChlorineDispenser() {
        if(!ChlorineDispenser.lastStatus)
            System.out.println("Chlorine dispenser is already off");
        else
            ChlorineDispenser.switchChlorineDispenser();
    }

    private static void getWaterLevel(float currentWaterLevel) {
        System.out.println("Current water level is: " + currentWaterLevel);
    }

    private static void setWaterLevel(String[] arguments, String topic, MqttClient mqttClient) throws MqttException {
        if (arguments.length != 3){
            System.out.println("Missing argument in the request");
            return;
        }
        int lowerBound = Integer.parseInt(arguments[1]);
        int upperBound = Integer.parseInt(arguments[2]);
        if(upperBound < lowerBound) {
            System.out.println("ERROR: The upper bound must be larger than the lower bound\n");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("min", lowerBound);
        jsonObject.put("max", upperBound);

        String payload = jsonObject.toString();
        mqttClient.publish(topic, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
        WaterLevelSensor.lowerBound = lowerBound;
        WaterLevelSensor.upperBound = upperBound;
    }

    private static void startWaterPump(String[] arguments, String topic, MqttClient mqttClient) throws MqttException {
        if (arguments.length < 2){
            System.out.println("Missing argument in the request");
            return;
        }
        if(WaterPump.lastStatus)
            System.out.println("Water pump is already active");
        else {
            if (!Objects.equals(arguments[1], "INC") & !Objects.equals(arguments[1], "DEC")) {
                System.out.println("Not valid mode");
                return;
            }
            WaterPump.switchWaterPump(arguments[1]);
            mqttClient.publish(topic, new MqttMessage(arguments[1].getBytes(StandardCharsets.UTF_8)));
            System.out.println("Water pump started in " + arguments[1] + " mode");

        }
    }

    private static void stopWaterPump(String topic, MqttClient mqttClient) throws MqttException {
        if(!WaterPump.lastStatus)
            System.out.println("Water pump is already off");
        else {
            WaterPump.switchWaterPump("OFF");
            mqttClient.publish(topic, new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Water pump switched OFF");
        }

    }

    private static void setLightColor(String[] arguments) {
        if (arguments.length != 2){
            System.out.println("Missing argument/s in the request");
            return;
        }
        if(!Objects.equals(arguments[1], "RED") & !Objects.equals(arguments[1], "GREEN") & !Objects.equals(arguments[1], "YELLOW"))
            System.out.println("Color not available");
        else
            Light.setLightColor(arguments[1]);
    }

    private static void getPresence() {
        if(PresenceSensor.currentPresence)
            System.out.println("There is someone in the swimming pool");
        else
            System.out.println("Swimming pool is empty");
    }

    private static void stopLight() {
        if(!Light.lastStatus)
            System.out.println("Light is already off");
        else
            Light.lightSwitch(false);
    }

    private static void startLight() {
        if(Light.lastStatus)
            System.out.println("Light is already on");
        else
            Light.lightSwitch(true);
    }

}