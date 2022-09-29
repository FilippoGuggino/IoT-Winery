package com.iot.IotManager;

import org.eclipse.californium.core.CoapServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * This class is only used to start the Coap registration resource (called "devices")
 */
@WebListener
public class HostInitializer implements ServletContextListener {
    private String resourceName = "devices";

    public void contextInitialized(ServletContextEvent event) {
        CoapServer deviceServer = new CoapServer();
        deviceServer.add(new CoapDeviceResource(resourceName));
        deviceServer.start();
    }

    public void contextDestroyed(ServletContextEvent event) {
        // Do your thing during webapp's shutdown.
    }
}
