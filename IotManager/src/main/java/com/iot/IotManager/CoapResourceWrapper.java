package com.iot.IotManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
/**
 * Used to manage a single instance of an observable sensor resource
 */
public class CoapResourceWrapper {
    String resourceUrl;
    CoapClient client;
    CoapObserveRelation relation;
    ArrayList<Float> valueHistory;
    public boolean updateClient = false;
    private final String valueJsonKey = "sensor_value";
    private final String deviceIp;

    /**
     * Start observing the resource identified by resourceUrl.
     * When a new value is received, if the client has a browser page open on this device info
     * the send an update using the websocket
     * @param resourceUrl
     * @param client
     * @param deviceIp
     */
    public CoapResourceWrapper(String resourceUrl, CoapClient client, String deviceIp) {
        this.resourceUrl = resourceUrl;
        this.client = client;
        this.deviceIp = deviceIp;
        this.valueHistory = new ArrayList<>();

        System.out.println("Started Observer to device ip: " + deviceIp + resourceUrl);
        relation = client.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse response) {
                        String content = response.getResponseText();

                        System.out.println(content + " from ip: " + deviceIp + resourceUrl);

                        try {
                            JSONObject payloadJsonObj = (JSONObject) JSONValue.parseWithException(content);

                            if (updateClient) {
                                String tmp = resourceUrl.replace("/", "");
                                payloadJsonObj.put("resource", tmp);
                                WebSocketServer.updateClients(deviceIp, payloadJsonObj.toJSONString());
                            }

                            if (payloadJsonObj.containsKey(valueJsonKey)) {
                                float resourceValue = ((Number) payloadJsonObj.get(valueJsonKey)).floatValue();
                                valueHistory.add(resourceValue);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError() {
                        System.err.println("-Failed--------");
                    }
                });
    }
}
