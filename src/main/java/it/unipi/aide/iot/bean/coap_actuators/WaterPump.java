package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WaterPump {
    public static boolean lastStatus;
    private static boolean status = false;
    private static final List<CoapClient> waterPumpEndpoints = new ArrayList<>();

    public void registerWaterPump(String ip) {
        CoapClient waterPumpEndpoint = new CoapClient("coap://[" + ip + "]/water-pump/switch");
        waterPumpEndpoints.add(waterPumpEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "water-pump");
        System.out.print("[REGISTRATION] The water pump: [" + ip + "] is now registered\n");
    }

    public void unregisterWaterPump(String ip) {
        for(int i=0; i<waterPumpEndpoints.size(); i++) {
            if(waterPumpEndpoints.get(i).getURI().equals(ip)) {
                waterPumpEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "water-pump");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db\n");
    }

    public static void switchWaterPump(String mode){
        if(waterPumpEndpoints.size() == 0)
            return;

        CoapResponse response;
        status = !Objects.equals(mode, "OFF");
        Request req = new Request(CoAP.Code.POST);
        req.getOptions().addUriQuery("mode=" + mode);
        for(CoapClient waterPumpEndpoint: waterPumpEndpoints) {
            response = waterPumpEndpoint.advanced(req);
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
