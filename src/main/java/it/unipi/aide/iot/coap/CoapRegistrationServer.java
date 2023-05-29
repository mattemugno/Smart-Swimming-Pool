package it.unipi.aide.iot.coap;

import org.eclipse.californium.core.CoapServer;

public class CoapRegistrationServer extends CoapServer {

    public CoapRegistrationServer() {
        this.add(new CoapRegistrationResource("registration"));
    }

}
