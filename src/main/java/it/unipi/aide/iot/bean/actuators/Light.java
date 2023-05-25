package it.unipi.aide.iot.bean.actuators;

import org.eclipse.californium.core.CoapClient;

import java.util.ArrayList;
import java.util.List;

public class Light {
    private final List<CoapClient> clientLightStatusList = new ArrayList<>();
    private final List<CoapClient> clientLightColorList = new ArrayList<>();

    /**
     * This function is used to register a light actuator as coap client
     */
    public void registerLight(String ip) {
        System.out.print("[REGISTRATION] The light: [" + ip + "] is now registered");
        CoapClient newClientLightStatus = new CoapClient("coap://[" + ip + "]/light/status");
        CoapClient newClientLightColor = new CoapClient("coap://[" + ip + "]/light/color");

        clientLightStatusList.add(newClientLightStatus);
        clientLightColorList.add(newClientLightColor);
        storeDeviceDb();
    }

    public void unregisterLight(String ip) {
        for(int i=0; i<clientLightStatusList.size(); i++) {
            if(clientLightStatusList.get(i).getURI().equals(ip)) {
                clientLightStatusList.remove(i);
                clientLightColorList.remove(i);
            }
        }
        removeDeviceFromDb();
    }

    private void storeDeviceDb() {
    }

    private void removeDeviceFromDb() {
    }

}
