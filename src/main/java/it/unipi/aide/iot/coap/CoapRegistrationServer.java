package it.unipi.aide.iot.coap;

import it.unipi.aide.iot.bean.actuators.Light;
import org.eclipse.californium.core.CoapServer;

public class CoapRegistrationServer extends CoapServer {

    public CoapRegistrationServer() {
        this.add(new CoapRegistrationResource("registration"));
    }

}
