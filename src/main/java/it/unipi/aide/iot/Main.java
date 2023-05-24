package it.unipi.aide.iot;

import it.unipi.aide.iot.mqtt.MQTTSubscriber;
import org.eclipse.californium.core.CoapServer;

public class Main {
    public static void main(String[] args) {
        CoapServer coapServer = new CoapServer();
        coapServer.start();
        MQTTSubscriber mqttHandler = new MQTTSubscriber();
    }
}