package com.iot.IotManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This servlet shows the cluster connected and subscribed to the server.
 */
@WebServlet(name = "welcomeServlet", value = "/welcome")
public class WelcomeServlet extends HttpServlet{

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession(true);

        session.setAttribute("connected_devices_list", CoapDeviceResource.activeSensorNodes);
        RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
        rd.forward(request,response);
    }

    public void destroy() {
    }
}
