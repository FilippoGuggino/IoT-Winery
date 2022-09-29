<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.concurrent.ConcurrentHashMap" %>
<%@ page import="com.iot.IotManager.GeneralNode" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<link rel="stylesheet" href="css/welcome.css">
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script type="text/javascript" src="javascript/utils.js"></script>

<script>
<%
    String resources = (String) session.getAttribute("resourceListCsv");
    String[] resourceArray = resources.split(",");

    Float currentValue = null;
    if(session.getAttribute("actuatorValue") != null){
        currentValue = (Float) session.getAttribute("actuatorValue");
    }

    out.println("var serverSocket = new WebSocket(\"ws://localhost:8080/IotManager_war_exploded/websocketendpoint\");\n" +
        "    var selectedDevice = findGetParameter(\"selectedDevice\");");

    out.println("var chart = {};");
    out.println("var labels = {};");
    out.println("var datapoints = {};");

    out.println("function chartLoader() {");
    for(String res: resourceArray){
        out.println("var ctx" + res + " = document.getElementById(\"chart"+ res +"\").getContext(\"2d\");");
        out.println("labels[\"" + res + "\"] = [" + session.getAttribute("labels-"+res) + "];");
        out.println("datapoints[\"" + res + "\"] = [" + session.getAttribute("dataset-"+res) + "];");
        out.println("const data" + res + " = {\n" +
            "            labels: labels[\"" + res + "\"],\n" +
            "            datasets: [\n" +
            "                {\n" +
            "                    label: 'History Value',\n" +
            "                    data: datapoints[\""+res+"\"],\n" +
            "                    borderColor: \"red\",\n" +
            "                    fillStyle: \"red\",\n" +
            "                    backgroundColor: 'rgba(255, 99, 132, 0.5)',\n" +
            "                    pointBackgroundColor: 'rgb(255, 99, 132)',\n" +
            "                    cubicInterpolationMode: 'monotone',\n" +
            "                    tension: 0.4\n" +
            "                }\n" +
            "            ]\n" +
            "        };");
        out.println("const config" + res + " = {\n" +
            "            type: 'line',\n" +
            "            data: data" + res + ",\n" +
            "            options: {\n" +
            "                responsive: true,\n" +
            "                plugins: {\n" +
            "                    legend: {\n" +
            "                        position: 'top',\n" +
            "                        display: true\n" +
            "                    },\n" +
            "                    title: {\n" +
            "                        display: true,\n" +
            "                        text: '"+res.substring(7)+" Value History'\n" +
            "                    }\n" +
            "                },\n" +
            "                interaction: {\n" +
            "                    intersect: false,\n" +
            "                },\n" +
            "                scales: {\n" +
            "                    x: {\n" +
            "                        display: true,\n" +
            "                        title: {\n" +
            "                            display: true\n" +
            "                        }\n" +
            "                    },\n" +
            "                    y: {\n" +
            "                        display: true,\n" +
            "                        title: {\n" +
            "                            display: true,\n" +
            "                            text: 'Value'\n" +
            "                        },\n" +
            "                        suggestedMin: -10,\n" +
            "                        suggestedMax: 200\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "        };\n" +
            "\n" +
            "        chart[\""+res+"\"] = new Chart(ctx"+res+", config" +res+ ");");
    }
    out.println("}");
%>

    function setNewValue(){
        var new_value = document.getElementById("new_value_input").value;

        serverSocket.send(JSON.stringify({"new_ac_value": new_value, "selected_device": selectedDevice}));
    }

    // Receives update from server
    serverSocket.onmessage = function(event)
    {
        var jsonobj = JSON.parse(event.data);
        if(jsonobj.hasOwnProperty("new_ac_value")){
            document.getElementById("actuator_label").innerHTML = "Current Value: " + jsonobj.new_ac_value;
        }
        else{
            var resourceUrl = jsonobj.resource;

            labels[resourceUrl].push(labels[resourceUrl][labels[resourceUrl].length-1]+1)
            chart[resourceUrl].labels = labels;
            chart[resourceUrl].data.datasets[0].data.push(jsonobj.sensor_value);
            chart[resourceUrl].update();
        }
    }

    // Send the selected device to the server (meaning that I am interested
    // in updates regarding this device)
    serverSocket.onopen = function (event){
        serverSocket.send(JSON.stringify({"selected_device": selectedDevice}));
    }

    function findGetParameter(parameterName) {
        var result = null,
            tmp = [];
        var items = location.search.substr(1).split("&");
        for (var index = 0; index < items.length; index++) {
            tmp = items[index].split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        }
        return result;
    }
</script>
<head>
    <title>JSP - Hello World</title>
</head>
<body onload="chartLoader()">
<a href="index.jsp">HOME</a>
<h1>Resources Details</h1>
<%
    for(String res: resourceArray){
        out.println("<div class=\"chart\">" +
                        "<canvas id=\"chart" + res +"\" ></canvas> "+
                "</div>");
    }

    if(currentValue != null) {
        out.println("<div class=\"actuator\">\n" +
                "    <form name=\"actuator\">\n" +
                "        <h2 id=\"actuator_label\">Current Value:  " + currentValue + "</h2>\n" +
                "        <input id=\"new_value_input\" name=\"new_value\" type=\"Input\">\n" +
                "        <input type=\"button\" value=\"Submit\" onclick=\"setNewValue()\">\n" +
                "    </form>\n" +
                "</div>");
    }
%>

</body>
</html>