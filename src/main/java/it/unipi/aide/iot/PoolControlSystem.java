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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;

public class PoolControlSystem {
    public static void main(String[] args) throws SQLException, MqttException {
        CoapRegistrationServer coapRegistrationServer = new CoapRegistrationServer();
        coapRegistrationServer.start();
        MQTTSubscriber mqttSubscriber = new MQTTSubscriber();
        MySqlDbHandler mySqlDbHandler = MySqlDbHandler.getInstance();
        mySqlDbHandler.getConnection();
        MqttClient mqttClient = mqttSubscriber.getMqttClient();

        printCommands();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String[] arguments;

        while (true) {
            try {
                command = bufferedReader.readLine();
                arguments = command.split(" ");
                switch(arguments[0]){
                    case "!get_temp":
                        getTemperature(TemperatureSensor.getCurrentTemperature());
                        break;
                    case "!set_temp":
                        setTemperatureBounds(arguments, mqttClient);
                        break;
                    case "!start_heater":
                        startHeater(arguments, mqttClient);
                        break;
                    case "!stop_heater":
                        stopHeater(mqttClient);
                        break;
                    case "!get_chlorine":
                        getChlorine(ChlorineSensor.getLastChlorineLevel());
                        break;
                    case "!set_chlorine":
                        setChlorine(arguments, mqttClient);
                        break;
                    case "!start_chlorine":
                        startChlorineDispenser(mqttClient);
                        break;
                    case "!stop_chlorine":
                        stopChlorineDispenser(mqttClient);
                        break;
                    case "!get_water_level":
                        getWaterLevel(WaterLevelSensor.getCurrentWaterLevel());
                        break;
                    case "!set_water_level":
                        setWaterLevel(arguments, mqttClient);
                        break;
                    case "!start_water_pump":
                        startWaterPump(arguments, mqttClient);
                        break;
                    case "!stop_water_pump":
                        stopWaterPump(mqttClient);
                        break;
                    case "!get_presence":
                        getPresence();
                        break;
                    case "!set_color":
                        setLightColor(arguments);
                        break;
                    case "!start_light":
                        startLight(mqttClient);
                        break;
                    case "!stop_light":
                        stopLight(mqttClient);
                        break;
                    case "!exit":
                        MySqlDbHandler.getInstance().removeAllDevices();
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
                "2) !set_temp <lower bound> <upper bound> --> sets the range within which the temperature must stay in the pool\n" +
                "3) !start_heater <mode> --> starts heating system\n" +
                "4) !stop_heater --> stops heating system\n" +
                "5) !get_chlorine --> recovers the last chlorine level measurement\n" +
                "6) !set_chlorine <lower bound> <upper bound> --> sets the range within which the chlorine must stay in the pool\n" +
                "7) !start_chlorine --> starts chlorine level dispenser\n" +
                "8) !stop_chlorine --> stops chlorine level dispenser\n" +
                "9) !get_water_level --> recovers the last water level measurement\n" +
                "10) !set_water_level <lower bound> <upper bound> --> sets the limit below which the water level must stay\n" +
                "11) !start_water_pump <mode> --> starts water pump\n" +
                "12) !stop_water_pump --> stops water pump\n" +
                "13) !set_color <color> --> sets the light color (GREEN, YELLOW or RED)\n" +
                "14) !get_presence --> check if there is someone in the pool\n" +
                "15) !start_light --> starts light\n" +
                "16) !stop_light --> stops light\n" +
                "17) !exit --> terminates the program\n"
        );
    }

    private static void getTemperature(float currentTemperature) {
        System.out.println("Current temperature is: " + currentTemperature + "°C");
    }

    private static void setTemperatureBounds(String[] arguments, MqttClient mqttClient) throws MqttException {
        if (arguments.length != 3){
            System.out.println("Incorrect number of arguments in the request");
            return;
        }
        int lowerBound = Integer.parseInt(arguments[1]);
        int upperBound = Integer.parseInt(arguments[2]);
        if(upperBound < lowerBound) {
            System.out.println("ERROR: The upper bound must be larger than the lower bound\n");
            return;
        }
        TemperatureSensor.lowerBound = lowerBound;
        TemperatureSensor.upperBound = upperBound;
        System.out.println("Temperature thresholds updated");
    }

    private static void startHeater(String[] arguments, MqttClient mqttClient) throws MqttException {
        if (arguments.length != 2){
            System.out.println("Missing argument/s in the request");
            return;
        }
        if (!Objects.equals(HeatingSystem.isStatus(), "OFF"))
            System.out.println("Heating system already active");
        else {
            if (!Objects.equals(arguments[1], "INC") & !Objects.equals(arguments[1], "DEC")) {
                System.out.println("Not valid mode");
            }
            HeatingSystem.switchHeatingSystem(arguments[1]);
            mqttClient.publish("temperature-command", new MqttMessage(arguments[1].getBytes(StandardCharsets.UTF_8)));
            System.out.println("Heating system started in " + arguments[1] + " mode");
        }

    }

    private static void stopHeater(MqttClient mqttClient) throws MqttException {
        if(Objects.equals(HeatingSystem.isStatus(), "OFF"))
            System.out.println("Heating system is already off");
        else {
            HeatingSystem.switchHeatingSystem("OFF");
            mqttClient.publish("temperature-command", new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Heating system switched OFF");
        }
    }

    private static void getChlorine(float lastChlorineLevel) {
        System.out.println("Current chlorine level is: " + lastChlorineLevel);
    }

    private static void setChlorine(String[] arguments, MqttClient mqttClient) throws MqttException {
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
        ChlorineSensor.lowerBound = lowerBound;
        ChlorineSensor.upperBound = upperBound;
        System.out.println("Chlorine thresholds updated");
    }

    private static void startChlorineDispenser(MqttClient mqttClient) throws MqttException {
        if(ChlorineDispenser.lastStatus)
            System.out.println("Chlorine dispenser is already active");
        else {
            ChlorineDispenser.switchChlorineDispenser();
            mqttClient.publish("chlorine-command", new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Chlorine dispenser switched ON");
        }
    }

    private static void stopChlorineDispenser(MqttClient mqttClient) throws MqttException {
        if(!ChlorineDispenser.lastStatus)
            System.out.println("Chlorine dispenser is already off");
        else {
            ChlorineDispenser.switchChlorineDispenser();
            mqttClient.publish("chlorine-command", new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Chlorine dispenser switched OFF");
        }
    }

    private static void getWaterLevel(int currentWaterLevel) {
        System.out.println("Current water level is: " + currentWaterLevel);
    }

    private static void setWaterLevel(String[] arguments, MqttClient mqttClient) throws MqttException {
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
        WaterLevelSensor.lowerBound = lowerBound;
        WaterLevelSensor.upperBound = upperBound;
        System.out.println("Water level thresholds updated");
    }

    private static void startWaterPump(String[] arguments, MqttClient mqttClient) throws MqttException {
        if (arguments.length < 2){
            System.out.println("Missing argument in the request");
            return;
        }
        if(!Objects.equals(WaterPump.isStatus(), "OFF"))
            System.out.println("Water pump is already active");
        else {
            if (!Objects.equals(arguments[1], "INC") & !Objects.equals(arguments[1], "DEC")) {
                System.out.println("Not valid mode");
                return;
            }
            WaterPump.switchWaterPump(arguments[1]);
            mqttClient.publish("water-level-command", new MqttMessage(arguments[1].getBytes(StandardCharsets.UTF_8)));
            System.out.println("Water pump started in " + arguments[1] + " mode");

        }
    }

    private static void stopWaterPump(MqttClient mqttClient) throws MqttException {
        if(Objects.equals(WaterPump.isStatus(), "OFF"))
            System.out.println("Water pump is already off");
        else {
            WaterPump.switchWaterPump("OFF");
            mqttClient.publish("water-level-command", new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Water pump switched OFF");
        }

    }

    private static void setLightColor(String[] arguments) {
        if (arguments.length != 2){
            System.out.println("Missing argument/s in the request");
            return;
        }
        if(!Objects.equals(arguments[1], "r") & !Objects.equals(arguments[1], "g") & !Objects.equals(arguments[1], "b"))
            System.out.println("Color not available");
        else {
            Light.setLightColor(arguments[1]);
            System.out.println("Color changed to " + arguments[1]);
        }
    }

    private static void getPresence() {
        if(PresenceSensor.currentPresence)
            System.out.println("There is someone in the swimming pool");
        else
            System.out.println("Swimming pool is empty");
    }

    private static void stopLight(MqttClient mqttClient) throws MqttException {
        if(!Light.lastStatus)
            System.out.println("Light is already off");
        else {
            Light.lightSwitch(false);
            mqttClient.publish("light-command", new MqttMessage("OFF".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Lights switched OFF");
        }
    }

    private static void startLight(MqttClient mqttClient) throws MqttException {
        if(Light.lastStatus)
            System.out.println("Light is already on");
        else {
            Light.lightSwitch(true);
            mqttClient.publish("light-command", new MqttMessage("ON".getBytes(StandardCharsets.UTF_8)));
            System.out.println("Lights switched ON");
        }
    }

}