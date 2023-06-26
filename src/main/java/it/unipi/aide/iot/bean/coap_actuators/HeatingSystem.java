package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import it.unipi.aide.iot.utility.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HeatingSystem {
    private static final List<CoapClient> heatingSystemEndpoints = new ArrayList<>();
    private static String status = "OFF";
    private Logger logger = Logger.getInstance();

    public void registerHeatingSystem(String ip) {
        CoapClient heatingSystemEndpoint = new CoapClient("coap://[" + ip + "]/heating-system/switch");
        heatingSystemEndpoints.add(heatingSystemEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "heating_system");
        //System.out.print("[REGISTRATION] The heating system: [" + ip + "] is now registered\n");
        logger.logInfo("The heating system: [" + ip + "] is now registered");
    }

    public void unregisterHeatingSystem(String ip) {
        for (int i = 0; i < heatingSystemEndpoints.size(); i++) {
            if (heatingSystemEndpoints.get(i).getURI().equals(ip)) {
                heatingSystemEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "heating_system");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db\n");
    }

    public static void switchHeatingSystem(String mode){
        if(heatingSystemEndpoints.size() == 0)
            return;

        if (Objects.equals(mode, "OFF"))
            status = "OFF";
        else if (Objects.equals(mode, "INC"))
            status = "INC";
        else if (Objects.equals(mode, "DEC"))
            status = "DEC";

        String msg = "mode=" + mode;
        for(CoapClient heatingSystemEndpoint: heatingSystemEndpoints) {
            heatingSystemEndpoint.put(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (coapResponse != null) {
                        if (!coapResponse.isSuccess())
                            System.out.print("[ERROR]Heating system switch: PUT request unsuccessful\n");
                    }
                }

                @Override
                public void onError() {
                    System.err.print("[ERROR] Heating system switch " + heatingSystemEndpoint.getURI() + "]");
                }
            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
    }

    public static String isStatus() {
        return status;
    }
}
