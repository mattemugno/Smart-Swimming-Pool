package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;

import java.util.ArrayList;
import java.util.List;

public class ChlorineDispenser {

    private final List<CoapClient> chlorineDispenserEndpoints = new ArrayList<>();

    public void registerChlorineDispenser(String ip) {
        CoapClient chlorineDispenserEndpoint = new CoapClient("coap://[" + ip + "]/chlorine_dispenser");
        chlorineDispenserEndpoints.add(chlorineDispenserEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "chlorine_dispenser");
        System.out.print("[REGISTRATION] The chlorine dispenser: [" + ip + "] is now registered");
    }

    public void unregisterChlorineDispenser(String ip) {
        for (int i = 0; i < chlorineDispenserEndpoints.size(); i++) {
            if (chlorineDispenserEndpoints.get(i).getURI().equals(ip)) {
                chlorineDispenserEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "chlorine_dispenser");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db");
    }

    public void switchChlorineDispenser(){}
}
