package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
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
    private static boolean status = false;

    public void registerHeatingSystem(String ip) {
        CoapClient heatingSystemEndpoint = new CoapClient("coap://[" + ip + "]/heating_system");
        heatingSystemEndpoints.add(heatingSystemEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "heating_system");
        System.out.print("[REGISTRATION] The heating system: [" + ip + "] is now registered\n");
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

        CoapResponse response;
        status = !Objects.equals(mode, "OFF");
        Request req = new Request(CoAP.Code.POST);
        req.getOptions().addUriQuery("mode=" + mode);
        for(CoapClient heatingSystemEndpoint: heatingSystemEndpoints) {
            response = heatingSystemEndpoint.advanced(req);
            if (response != null) {
                System.out.println("Response: " + response.getResponseText());
                System.out.println("Payload: " + Arrays.toString(response.getPayload()));
            } else
                System.out.println("Request failed");
        }
    }

    public static boolean isStatus() {
        return status;
    }
}
