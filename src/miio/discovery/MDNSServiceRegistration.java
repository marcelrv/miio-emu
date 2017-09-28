package miio.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class MDNSServiceRegistration {
    public static final int PORT = 54321;
    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    public void registerService(String device, String did) {

        String id = device.replace(".", "-") + "_miio" + Long.parseUnsignedLong(did, 16);
        try {
            // Create a JmDNS instance
            jmdns = JmDNS.create(InetAddress.getLocalHost().getHostAddress());

            Logger logger = Logger.getLogger(JmDNS.class.getName());
            ConsoleHandler handler = new ConsoleHandler();
            logger.addHandler(handler);
            logger.setLevel(Level.INFO);
            handler.setLevel(Level.INFO);

            // Register a service
            serviceInfo = ServiceInfo.create("_miio._udp.local", id, PORT, "path=/mydevice");
            jmdns.registerService(serviceInfo);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void unRegisterService() {
        jmdns.unregisterService(serviceInfo);
    }

    public void close() {
        // Unregister all services
        jmdns.unregisterAllServices();
        try {
            jmdns.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}