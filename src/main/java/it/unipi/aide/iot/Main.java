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

        //implementare USER LOGIC facendogli impostare dei parametri
        //get current temperature
        //set heating system
        //get chlorine level
        //set chlorine dispenser
        //get water level
        //set water pump
        //set light color
        //get presence
        //set lower bound and upper bound for temperature, chlorine and water level
        //spegni water pump, heating, chlorine
        //aggiungi qualche analytics o query dal db
    }
}