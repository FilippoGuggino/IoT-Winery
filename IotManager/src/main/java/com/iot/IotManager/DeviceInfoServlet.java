package com.iot.IotManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Servlet that shows in real-time, values coming from the selected device
 */

@WebServlet(name = "deviceServlet", value = "/device")
public class DeviceInfoServlet extends HttpServlet{

    public void init() {
    }

    /**
     * In order to dynamically create an html page with a graph for each sensor
     * and a form for each actuator, some parameters are created
     * <ul>
     *     <li>resourceListCsv: list of comma separated resource names</li>
     *     <li>dataset-X: arraylist of history values about device X</li>
     *     <li>labels-X: arraylist of graph's labels about device X</li>
     * </ul>
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession(true);

        // Retrieve GET parameter to show info about the selected device
        String deviceIp = request.getParameter("selectedDevice");

        SensorNode requestedNode = (SensorNode) CoapDeviceResource.activeSensorNodes.get(deviceIp);

        if(requestedNode == null){
            RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
            rd.forward(request,response);
        }
        else {
            StringBuilder resourcesStringCsv = new StringBuilder();
            ArrayList<CoapResourceWrapper> resourceWrappers = requestedNode.getResourceWrappers();
            CoapActuatorWrapper actuator = requestedNode.getActuatorWrapper();

            if (actuator != null) {
                String actuatorUrl = actuator.resourceUrl.replace("/", "");

                session.setAttribute("actuatorValue", actuator.currentValue);
            }
            else{
                session.removeAttribute("actuatorValue");
            }

            for (CoapResourceWrapper resTmp : resourceWrappers) {
                String resourceString = resTmp.resourceUrl.replace("/", "");
                resourcesStringCsv.append(resourceString).append(",");

                StringBuilder datasetString = new StringBuilder();
                StringBuilder labelsString = new StringBuilder();
                ArrayList<Float> valueHistory = resTmp.valueHistory;
                int i = 0;
                for (Iterator<Float> it = valueHistory.iterator(); it.hasNext(); i++) {
                    Float f = it.next();

                    datasetString.append(f);
                    datasetString.append(',');

                    labelsString.append(i);
                    labelsString.append(',');
                }
                if (datasetString.toString().length() != 0) {
                    datasetString.deleteCharAt(datasetString.toString().length() - 1);
                    labelsString.deleteCharAt(labelsString.toString().length() - 1);
                }
                session.setAttribute("dataset-" + resourceString, datasetString.toString());
                session.setAttribute("labels-" + resourceString, labelsString.toString());
            }
            session.setAttribute("resourceListCsv", resourcesStringCsv.toString());

            RequestDispatcher rd = request.getRequestDispatcher("/device-info.jsp");
            rd.forward(request, response);
        }
    }

    public void destroy() {
    }
}
