package it.unipi.aide.iot.bean.coap_actuators;

import it.unipi.aide.iot.persistence.MySqlDbHandler;
import it.unipi.aide.iot.utility.Logger;
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
    private static String status = "OFF";
    private static final List<CoapClient> waterPumpEndpoints = new ArrayList<>();
    private Logger logger;

    public void registerWaterPump(String ip) {
        CoapClient waterPumpEndpoint = new CoapClient("coap://[" + ip + "]/water-pump/switch");
        waterPumpEndpoints.add(waterPumpEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "water-pump");
        //System.out.print("[REGISTRATION] The water pump: [" + ip + "] is now registered\n");
        logger.logInfo("The water pump: [\" + ip + \"] is now registered");
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

        if (Objects.equals(mode, "OFF"))
            status = "OFF";
        else if (Objects.equals(mode, "INC"))
            status = "INC";
        else if (Objects.equals(mode, "DEC"))
            status = "DEC";

        Request req = new Request(CoAP.Code.POST);
        req.getOptions().addUriQuery("mode=" + mode);
        for(CoapClient waterPumpEndpoint: waterPumpEndpoints)
            waterPumpEndpoint.advanced(req);
    }

    public static String isStatus() {
        return status;
    }
}
