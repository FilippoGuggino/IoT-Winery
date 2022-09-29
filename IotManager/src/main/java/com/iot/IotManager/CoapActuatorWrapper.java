package com.iot.IotManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to manage a single instance of an actuator
 */
public class CoapActuatorWrapper{
    String resourceUrl;
    CoapClient client;
    float currentValue = 0;
    boolean updateClient = false;
    private final String deviceIp;

    /**
     * Retrieve the current value of the actuator via a GET request to the resourceUrl
     * @param resourceUrl
     * @param client
     * @param deviceIp
     */
    public CoapActuatorWrapper(String resourceUrl, CoapClient client, String deviceIp) {
        this.resourceUrl = resourceUrl;
        this.deviceIp = deviceIp;
        this.client = client;

        System.out.println("Created ActuatorWrapper of device ip: " + deviceIp + resourceUrl);

        try {
            CoapResponse res = client.get();
            System.out.println(res.getResponseText() + " from ip: " + deviceIp);
            JSONObject responseJson = (JSONObject) JSONValue.parseWithException(res.getResponseText());

            this.currentValue = ((Number) responseJson.get("actuator_value")).floatValue();
        } catch (ConnectorException | ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the actuator CoAP resource via a PUT request containing the new value to set.
     * When a response has been received from the device (confirming the correct update), send
     * a feedback to the client using a websocket.
     * @param newValue new value set by the client
     */
    public void setNewValue(float newValue){
        try {
            CoapResponse response = client.put("set_ac_value="+ newValue, MediaTypeRegistry.TEXT_PLAIN);
            System.out.println(response.getResponseText() + " from ip: " + deviceIp + resourceUrl);
            JSONObject responseJson = (JSONObject) JSONValue.parseWithException(response.getResponseText());
            if(responseJson.containsKey("new_ac_value")) {
                this.currentValue = ((Number) responseJson.get("new_ac_value")).floatValue();
                if (updateClient) {
                    JSONObject payload = new JSONObject();
                    payload.put("new_ac_value", this.currentValue);
                    WebSocketServer.updateClients(deviceIp, payload.toJSONString());
                }
            }
        } catch (ConnectorException | IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
