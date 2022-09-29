package com.iot.IotManager;

import org.json.simple.JSONArray;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ListIterator;

public class IotCluster {
    private ArrayList<String> clusterDevices;

    public IotCluster(){
        this.clusterDevices = new ArrayList<>();
    }

    public void addNodes(JSONArray devices){
        ArrayList<String> tmp = new ArrayList<>();
        Inet6Address tmpIp = null;

        try {
            for (ListIterator it = devices.listIterator(); it.hasNext(); ) {
                String i = (String) it.next();
                tmpIp = (Inet6Address) Inet6Address.getByName(i);
                tmp.add(tmpIp.getHostAddress());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.clusterDevices = tmp;
    }

    public ArrayList<String> getClusterDevices() {
        return clusterDevices;
    }
}
