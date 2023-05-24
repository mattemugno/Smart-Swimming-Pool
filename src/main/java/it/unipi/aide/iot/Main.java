package it.unipi.aide.iot;

import it.unipi.aide.iot.mqtt.MQTTSubscriber;
import org.eclipse.californium.core.CoapServer;

public class Main {
    public static void main(String[] args) {
        CoapServer coapServer = new CoapServer();
        coapServer.start();
        MQTTSubscriber mqttHandler = new MQTTSubscriber();

        System.out.println("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}