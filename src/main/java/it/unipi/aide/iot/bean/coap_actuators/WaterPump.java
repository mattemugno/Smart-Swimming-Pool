package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;

import java.util.ArrayList;
import java.util.List;

public class WaterPump {
    private final List<CoapClient> waterPumpEndpoints = new ArrayList<>();

    public void registerWaterPump(String ip) {
        CoapClient waterPumpEndpoint = new CoapClient("coap://[" + ip + "]/water_pump");
        waterPumpEndpoints.add(waterPumpEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "water_pump");
        System.out.print("[REGISTRATION] The water pump: [" + ip + "] is now registered");
    }

    public void unregisterWaterPump(String ip) {
        for(int i=0; i<waterPumpEndpoints.size(); i++) {
            if(waterPumpEndpoints.get(i).getURI().equals(ip)) {
                waterPumpEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "water_pump");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db");
    }

    public void switchWaterPump(){}
}
