package it.unipi.aide.iot.coap;

import it.unipi.aide.iot.bean.coap_actuators.ChlorineDispenser;
import it.unipi.aide.iot.bean.coap_actuators.HeatingSystem;
import it.unipi.aide.iot.bean.coap_actuators.Light;
import it.unipi.aide.iot.bean.coap_actuators.WaterPump;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.nio.charset.StandardCharsets;

public class CoapRegistrationResource extends CoapResource {
    private final Light light = new Light();
    private final WaterPump waterPump = new WaterPump();
    private final ChlorineDispenser chlorineDispenser = new ChlorineDispenser();
    private final HeatingSystem heatingSystem = new HeatingSystem();

    public CoapRegistrationResource(String name) {
        super(name);
        setObservable(true);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        Response response = new Response(CoAP.ResponseCode.CONTENT);
        System.out.println(response);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        String deviceType = exchange.getRequestText();
        System.out.println("Device type: " + deviceType);
        String ip = exchange.getSourceAddress().getHostAddress();
        boolean success = true;

        switch (deviceType) {
            case "light":
                light.registerLight(ip);
                break;
            case "water_pump":
                waterPump.registerWaterPump(ip);
                break;
            case "chlorine_dispenser":
                chlorineDispenser.registerChlorineDispenser(ip);
                break;
            case "heating_system":
                heatingSystem.registerHeatingSystem(ip);
                break;
            default:
                success = false;
                break;
        }
        if (success)
            exchange.respond(CoAP.ResponseCode.CREATED, "Success".getBytes(StandardCharsets.UTF_8));
        else
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Unsuccessful".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        String deviceType = exchange.getRequestText();
        String ip = exchange.getSourceAddress().getHostAddress();
        boolean success = true;

        switch (deviceType) {
            case "light":
                light.unregisterLight(ip);
                break;
            case "water_pump":
                waterPump.unregisterWaterPump(ip);
                break;
            case "chlorine_dispenser":
                chlorineDispenser.unregisterChlorineDispenser(ip);
                break;
            case "heating_system":
                heatingSystem.unregisterHeatingSystem(ip);
                break;
            default:
                success = false;
                break;
        }
        if (success)
            exchange.respond(CoAP.ResponseCode.DELETED, "Device removed from DB".getBytes(StandardCharsets.UTF_8));
        else
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Unsuccessful".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void handlePUT(CoapExchange exchange) {}
}
