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

public class ChlorineDispenser {

    public static final List<CoapClient> chlorineDispenserEndpoints = new ArrayList<>();
    public static boolean lastStatus;
    private Logger logger = Logger.getInstance();

    public void registerChlorineDispenser(String ip) {
        CoapClient chlorineDispenserEndpoint = new CoapClient("coap://[" + ip + "]/chlorine_dispenser");
        chlorineDispenserEndpoints.add(chlorineDispenserEndpoint);
        MySqlDbHandler.getInstance().insertNewDevice(ip, "chlorine_dispenser");
        //System.out.print("[REGISTRATION] The chlorine dispenser: [" + ip + "] is now registered\n");
        logger.logInfo("The chlorine dispenser: [" + ip + "] is now registered");

    }

    public void unregisterChlorineDispenser(String ip) {
        for (int i = 0; i < chlorineDispenserEndpoints.size(); i++) {
            if (chlorineDispenserEndpoints.get(i).getURI().equals(ip)) {
                chlorineDispenserEndpoints.remove(i);
            }
        }
        MySqlDbHandler.getInstance().removeDevice(ip, "chlorine_dispenser");
        System.out.print("Device " + ip + " removed detached from endpoint and removed from db\n");
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

        Request req = new Request(CoAP.Code.POST);
        req.getOptions().addUriQuery("mode=" + msg);
        for(CoapClient chlorineDispenserEndpoint: chlorineDispenserEndpoints) {
            chlorineDispenserEndpoint.advanced(req);
        }
    }
}
