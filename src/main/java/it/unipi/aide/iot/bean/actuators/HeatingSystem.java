package it.unipi.aide.iot.bean.actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;

import java.util.ArrayList;
import java.util.List;

public class HeatingSystem {
    private final List<CoapClient> heatingSystemEndpoints = new ArrayList<>();

    public void registerHeatingSystem(String ip) {
        CoapClient heatingSystemEndpoint = new CoapClient("coap://[" + ip + "]/heating_system");
        heatingSystemEndpoints.add(heatingSystemEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "heating_system");
        System.out.print("[REGISTRATION] The heating system: [" + ip + "] is now registered");
    }

    public void unregisterHeatingSystem(String ip) {
        for (int i = 0; i < heatingSystemEndpoints.size(); i++) {
            if (heatingSystemEndpoints.get(i).getURI().equals(ip)) {
                heatingSystemEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "heating_system");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db");
    }
}
