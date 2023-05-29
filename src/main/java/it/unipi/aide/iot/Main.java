package it.unipi.aide.iot;

import it.unipi.aide.iot.coap.CoapRegistrationServer;
import it.unipi.aide.iot.mqtt.MQTTSubscriber;
import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        CoapRegistrationServer coapRegistrationServer = new CoapRegistrationServer();
        coapRegistrationServer.start();
        MQTTSubscriber mqttSubscriber = new MQTTSubscriber();
        MySqlDbHandler mySqlDbHandler = MySqlDbHandler.getInstance();
        Connection connection = mySqlDbHandler.getConnection();
        System.out.println(connection);

        printCommands();
    }

    public static void printCommands(){
        System.out.println("***************************** SMART SWIMMING POOL *****************************\n" +
                "The following commands are available:\n" +
                "1) !get_temp --> recovers the last temperature measurement\n" +
                "2) !set_temp_th <lower bound> <upper bound> --> sets the range within which the temperature must stay in the pool\n" +
                "3) !start_heater <id> --> starts heating system\n" +
                "4) !stop_heater <id> --> stops heating system\n" +
                "5) !get_chlorine --> recovers the last chlorine level measurement\n" +
                "6) !set_chlorine <lower bound> <upper bound> --> sets the range within which the chlorine must stay in the pool\n" +
                "7) !start_chlorine <id> --> starts chlorine level dispenser\n" +
                "8) !stop_chlorine <id> --> stops chlorine level dispenser\n" +
                "9) !get_water --> recovers the last water level measurement\n" +
                "10) !set_water_level <lower bound> <upper bound> --> sets the limit below which the water level must stay\n" +
                "11) !start_water_pump <id> --> starts water pump\n" +
                "12) !stop_water_pump <id> --> stops water pump\n" +
                "13) !set_color <id> <color> --> sets the light color (GREEN, YELLOW or RED)\n" +
                "14) !get_presence --> check if there is someone in the pool\n" +
                "15) !start_light <id> --> starts light\n" +
                "16) !stop_light <id> --> stops light\n" +
                "17) !exit --> terminates the program\n"
        );
    }
}