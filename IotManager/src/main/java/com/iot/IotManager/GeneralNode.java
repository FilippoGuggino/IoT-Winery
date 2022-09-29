package com.iot.IotManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.ArrayList;

/**
 * Superclass of SensorNode and BorderRouterNode, initialize and define
 * some fundamentals fields.
 */
public class GeneralNode {
    ArrayList<CoapClient> observerClient;
    ArrayList<CoapObserveRelation> relationList;
    String deviceIp;
    ArrayList<String> resourcesList;

    public GeneralNode(String deviceIp, ArrayList<String> resourcesList) {
        this.deviceIp = deviceIp;
        this.resourcesList = resourcesList;
        this.relationList = new ArrayList<>();
        this.observerClient = new ArrayList<>();
    }

    public String getDeviceIp() {
        return deviceIp;
    }
}
