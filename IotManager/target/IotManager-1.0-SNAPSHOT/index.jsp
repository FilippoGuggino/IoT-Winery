<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.concurrent.ConcurrentHashMap" %>
<%@ page import="com.iot.IotManager.GeneralNode" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.iot.IotManager.IotCluster" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="com.iot.IotManager.CoapDeviceResource" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<link rel="stylesheet" href="css/welcome.css">
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1>Winery IoT Clusters
</h1>
<br/>
<%
    ConcurrentHashMap<String, IotCluster> iotClusters = CoapDeviceResource.getIotClusters();
    Set<String> keys = iotClusters.keySet();
    ArrayList<String> clusterDevices;
    IotCluster cluster;
    for (String ip: keys) {
        out.println("<ul class=\"round-border\">");
        cluster = iotClusters.get(ip);
        clusterDevices = cluster.getClusterDevices();

        if(clusterDevices != null) {
            for (String deviceIp: clusterDevices) {
                out.println("<li><a href=\"device?selectedDevice=" + deviceIp+ "\">" + deviceIp+ "</a></li>");
            }
        }
        out.println("</ul>");
    }
%>
</body>
</html>