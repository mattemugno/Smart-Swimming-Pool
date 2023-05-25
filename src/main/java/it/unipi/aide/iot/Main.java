package it.unipi.aide.iot;

import it.unipi.aide.iot.coap.CoapRegistrationServer;
import it.unipi.aide.iot.mqtt.MQTTSubscriber;
import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {
        CoapRegistrationServer coapRegistrationServer = new CoapRegistrationServer();
        coapRegistrationServer.start();
        MQTTSubscriber mqttHandler = new MQTTSubscriber();
    }
}