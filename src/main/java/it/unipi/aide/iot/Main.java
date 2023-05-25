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
        MQTTSubscriber mqttHandler = new MQTTSubscriber();
        MySqlDbHandler mySqlDbHandler = new MySqlDbHandler();
        Connection connection = mySqlDbHandler.getConnection();
    }
}