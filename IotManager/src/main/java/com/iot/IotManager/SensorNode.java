package com.iot.IotManager;

import org.eclipse.californium.core.CoapClient;

import java.util.ArrayList;

/**
 * Object encapsulating the info regarding a specific node:
 * <ul>
 *     <li>Sensor Resources (observable) </li>
 *     <li>Actuator Resources (non-observable) </li>
 * </ul>
 */
public class SensorNode extends GeneralNode{
    private ArrayList<CoapResourceWrapper> resourceWrappers = new ArrayList<>();
    private CoapActuatorWrapper actuatorWrapper = null;

    public SensorNode(String deviceIp, ArrayList<String> observableResourcesList, String actuatorUrl){
        super(deviceIp, observableResourcesList);

        for(String res: resourcesList){
            CoapClient client = new CoapClient("coap://[" + deviceIp + "]" + res);

            resourceWrappers.add(new CoapResourceWrapper(res, client, deviceIp));
        }

        if(!actuatorUrl.equals("")) {
            CoapClient client = new CoapClient("coap://[" + deviceIp + "]" + actuatorUrl);

            this.actuatorWrapper = new CoapActuatorWrapper(actuatorUrl, client, deviceIp);
        }
    }

    public void setUpdateClient(boolean updateClient){
        for(CoapResourceWrapper tmp: resourceWrappers){
            tmp.updateClient = updateClient;
        }
        if(actuatorWrapper != null) {
            actuatorWrapper.updateClient = updateClient;
        }
    }

    public ArrayList<CoapResourceWrapper> getResourceWrappers(){
        return this.resourceWrappers;
    }

    public CoapActuatorWrapper getActuatorWrapper() {
        return actuatorWrapper;
    }
}
