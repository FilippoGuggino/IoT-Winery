package com.iot.IotManager;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Expose a "devices" resource, used by the IoT nodes to subscribe to the server.
 */
public class CoapDeviceResource extends CoapResource {
    static ConcurrentHashMap<String, IotCluster> iotClusters = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, GeneralNode> activeSensorNodes = new ConcurrentHashMap<>();

    public CoapDeviceResource(String name) {
        super(name);
        setObservable(true);
    }

    public static ConcurrentHashMap<String, IotCluster> getIotClusters() {
        return iotClusters;
    }

    public void handleGET(CoapExchange exchange){
        handler(exchange);
    }

    public void handlePOST(CoapExchange exchange){
        handler(exchange);
    }

    /**
     * Handler of the requests coming from iot devices.
     * <ul>
     *     <li>Discover device resources using .well-known/core</li>\
     *     <li>Create a url list of SensorResources(observable and ActuatorResources) and ActuatorResources(non-observable)</li>
     *     <li>Based on the fiels "device_role" contained in the request's payload create an instance of a SensorNode or a BorderRouterNode</li>
     * </ul>
     * @param exchange
     */
    public void handler(CoapExchange exchange){
        String payload = exchange.getRequestText();
        Inet6Address ipAddr = (Inet6Address) exchange.getSourceAddress();
        String deviceIp = ipAddr.getHostAddress();

        GeneralNode addedNode = null;
        System.out.println("Connected device with ip: " + deviceIp);

        System.out.println(payload);

        CoapClient client = new CoapClient("coap://[" + deviceIp + "]/");
        //Request req = new Request(CoAP.Code.GET);

        String resourceUrl = "";
        ArrayList<String> observablerResourcesList = new ArrayList<>();
        String actuatorUrl = "";
        //req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
        try {
            //CoapResponse resp = client.advanced(req);
            Set<WebLink> links = client.discover();
            for (WebLink link : links) {
                resourceUrl = link.getURI();
                System.out.println(link.toString());
                System.out.println(link.getAttributes().containsAttribute("obs"));
                if(!resourceUrl.equals("/.well-known/core")){
                    if(link.getAttributes().containsAttribute("obs")) {
                        observablerResourcesList.add(resourceUrl);
                    }
                    else{
                        actuatorUrl = resourceUrl;
                    }
                }
            }
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject payloadJsonObj = (JSONObject) JSONValue.parseWithException(payload);
            if(payloadJsonObj.containsKey("device_role")){
                String deviceRole = (String) payloadJsonObj.get("device_role");

                if(deviceRole.equals("border_router")){
                    IotCluster newCluster = new IotCluster();
                    iotClusters.put(deviceIp, newCluster);
                    addedNode = new BorderRouterNode(deviceIp, observablerResourcesList, newCluster);
                }
                else if(deviceRole.equals("sensor_node")){
                    addedNode = new SensorNode(deviceIp, observablerResourcesList, actuatorUrl);
                }

                activeSensorNodes.put(deviceIp, addedNode);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Response response = new Response(CoAP.ResponseCode.CREATED);
        response.setPayload("ok");
        exchange.respond(response);
    }


}
