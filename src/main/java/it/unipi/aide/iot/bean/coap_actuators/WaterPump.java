package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;
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
        System.out.print("[REGISTRATION] The water pump: [" + ip + "] is now registered");
    }

    public void unregisterWaterPump(String ip) {
        for(int i=0; i<waterPumpEndpoints.size(); i++) {
            if(waterPumpEndpoints.get(i).getURI().equals(ip)) {
                waterPumpEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "water-pump");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db");
    }

    public static void switchWaterPump(String mode){
        if(waterPumpEndpoints.size() == 0)
            return;

        String msg = "mode?=" + mode;
        status = !Objects.equals(mode, "OFF");

        for(CoapClient waterPumpEndpoint: waterPumpEndpoints) {
            waterPumpEndpoint.post(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (coapResponse != null) {
                        if (!coapResponse.isSuccess())
                            System.out.print("[ERROR]Water Pump Switching: POST request unsuccessful");
                    }
                }

                @Override
                public void onError() {
                    System.err.print("[ERROR] Water Pump Switching " + waterPumpEndpoint.getURI() + "]");
                }
            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
    }

    public static boolean isStatus() {
        return status;
    }
}
