package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class ChlorineDispenser {

    public static final List<CoapClient> chlorineDispenserEndpoints = new ArrayList<>();
    public static boolean lastStatus;

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

    public static void switchChlorineDispenser(){
        if(chlorineDispenserEndpoints.size() == 0)
            return;
        String msg;
        if(lastStatus) {
            msg = "OFF";
            lastStatus = false;
        }
        else {
            msg = "ON";
            lastStatus = true;
        }

        for(CoapClient chlorineDispenserEndpoint: chlorineDispenserEndpoints) {
            chlorineDispenserEndpoint.put(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (coapResponse != null) {
                        if (!coapResponse.isSuccess())
                            System.out.print("[ERROR]Chlorine dispenser switching: PUT request unsuccessful");
                    }
                }

                @Override
                public void onError() {
                    System.err.print("[ERROR] Chlorine dispenser switching " + chlorineDispenserEndpoint.getURI() + "]");
                }
            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
    }
}
