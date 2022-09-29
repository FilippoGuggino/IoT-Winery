package com.iot.IotManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.ListIterator;

public class BorderRouterNode extends GeneralNode{
    IotCluster iotCluster;
    private String valueJsonKey = "cluster_devices";

    /**
     * Initialize a BorderRouterNode instance by observing its "cluster_info" resource
     * @param deviceIp IP of the border router
     * @param resourcesList List of resources URLs
     * @param iotCluster Maintain a correspondence between the Border Router and the devices connected directly to it
     */
    public BorderRouterNode(String deviceIp, ArrayList<String> resourcesList, IotCluster iotCluster){
        super(deviceIp, resourcesList);

        this.iotCluster = iotCluster;
        int i = 0;
        CoapObserveRelation relation = null;

        for( i = 0; i < resourcesList.size(); i++){

            String resourceUrl = resourcesList.get(i);
            CoapClient client = new CoapClient("coap://[" + deviceIp + "]" + resourceUrl);

            relation = client.observe(
                    new CoapHandler() {
                        @Override
                        public void onLoad(CoapResponse response) {
                            String content = response.getResponseText();

                            System.out.println(content + " from ip: " + deviceIp);

                            try {
                                JSONObject payloadJsonObj = (JSONObject) JSONValue.parseWithException(content);

                                // If json contains key 'cluster_devices' than it's a cluster update
                                if (payloadJsonObj.containsKey(valueJsonKey)) {
                                    JSONArray devices = (JSONArray) payloadJsonObj.get(valueJsonKey);

                                    iotCluster.addNodes(devices);
                                }
                            }catch (ParseException e){
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
}
