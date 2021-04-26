package miio.discovery;

import java.util.Scanner;

import org.slf4j.LoggerFactory;

import miio.Base;
import miio.emulator.MiIoDevices;

public class MDNSTest {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Base.class);
    // enter the default device here
    public final static MiIoDevices DEFAULT_DEVICE = MiIoDevices.DREAME_VACUUM_P2009;
    public final static String DID = "AABBCCFF";

    private final static String DEFAULTSFILE = "miio-default.json";

    public static void main(String[] args) {
        logger.info("Mi IO SSDP Emulator");
        MDNSServiceRegistration mdns = new MDNSServiceRegistration();
        mdns.registerService(DEFAULT_DEVICE.getModel(), DID);

        Scanner scan = new Scanner(System.in);
        while (true) {
            String s = scan.next();

            if ("q".equals(s)) {
                logger.info("Quitting");
                mdns.unRegisterService();
                mdns.close();
                break;
            }
            if ("r".equals(s)) {
                logger.info("stop");
                mdns.unRegisterService();
                logger.info("starting");
                mdns.registerService(DEFAULT_DEVICE.getModel(), DID);
            }
        }
        scan.close();
        logger.info("Mi IO MDNS Emulator closed");

    }
}