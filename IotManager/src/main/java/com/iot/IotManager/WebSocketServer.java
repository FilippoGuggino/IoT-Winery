package com.iot.IotManager;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
* This class works as an endpoint to all clients.
* */
@ServerEndpoint("/websocketendpoint")
public class WebSocketServer {
    /**
     * String: device IP
     * ArrayList<Session>: list of client sessions that are interested in updates from that node
     */
    static ConcurrentHashMap<String, ArrayList<Session>> nodeToInterestedClientsMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("Open Connection ...");
        System.out.println(session.getId());
    }

    /**
     * When a message is received it can be:
     * <ul>
     *     <li>A new value set for an actuator</li>
     *     <li>The device selected by the user</li>
     * </ul>
     * In the first case, a new PUT request will be forwarded to the specified device.
     * In the latter, update nodeToInterestedClientsMap in order for the client to receives
     * updates from the selected device.
     * @param session
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        //activeSession = session;
        SensorNode interestedNode = null;
        try {
            JSONObject payload = (JSONObject) JSONValue.parseWithException(message);
            if(payload.containsKey("selected_device")) {
                String selectedNode = (String) payload.get("selected_device");
                interestedNode = (SensorNode) CoapDeviceResource.activeSensorNodes.get(selectedNode);

                if(interestedNode != null) {
                    if (payload.containsKey("new_ac_value")) {
                        float newValue = Float.parseFloat((String) payload.get("new_ac_value"));

                        CoapActuatorWrapper actuator = interestedNode.getActuatorWrapper();
                        actuator.setNewValue(newValue);
                    } else {
                        Inet6Address addr = (Inet6Address) InetAddress.getByName(selectedNode);
                        System.out.println("started following sensor: " + selectedNode);
                        interestedNode.setUpdateClient(true);
                        updateNodeToClientsMap(selectedNode, session);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session){
        removeSessionFromMap(session);
    }

    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }

    /**
     * Update the clients, via websocket, that are interested in the device which IP is "deviceIp"
     * @param deviceIp
     * @param payload
     */
    public static void updateClients(String deviceIp, String payload){

        try {
            ArrayList<Session> interestedClients = nodeToInterestedClientsMap.get(deviceIp);
            System.out.println("Send update from device: " + deviceIp + " to sessions: " + interestedClients.toString());
            for(Session tmp: interestedClients){
                tmp.getBasicRemote().sendText(payload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When a user disconnect from the device-info.jsp it is removed from the "interested"
     * users of that device, so that it will no longer receives updates.
     *
     * @param sessionWebSocket WebSocketSession of the user who have disconnected
     */
    public static void removeSessionFromMap(Session sessionWebSocket){
        for (Map.Entry<String, ArrayList<Session>> pair : nodeToInterestedClientsMap.entrySet()) {
            String deviceIp = pair.getKey();
            ArrayList<Session> listOfWebSocket = pair.getValue();
            if (listOfWebSocket.contains(sessionWebSocket)) {
                listOfWebSocket.remove(sessionWebSocket);
                if (listOfWebSocket.isEmpty()) {
                    // Since no other client is interested in updates from this node
                    // set update client to false
                    SensorNode interestedNode = (SensorNode)CoapDeviceResource.activeSensorNodes.get(deviceIp);
                    interestedNode.setUpdateClient(false);

                    nodeToInterestedClientsMap.remove(deviceIp);
                } else {
                    nodeToInterestedClientsMap.put(deviceIp, listOfWebSocket);
                }
                break;
            }
        }
    }

    public void updateNodeToClientsMap(String selectedNode, Session session){
        ArrayList<Session> interestedClients;
        if(nodeToInterestedClientsMap.containsKey(selectedNode)) {
            interestedClients = nodeToInterestedClientsMap.get(selectedNode);
            if(!interestedClients.contains(session))
                interestedClients.add(session);
        }
        else{
            interestedClients = new ArrayList<>();
            interestedClients.add(session);
            nodeToInterestedClientsMap.put(selectedNode, interestedClients);
        }
    }
}
