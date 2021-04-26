package miio.discovery;

import java.util.Scanner;

import org.slf4j.LoggerFactory;

import miio.Base;
import miio.emulator.MiIoDevices;

public class ssdpTest {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Base.class);
    // enter the default device here
    public final static MiIoDevices DEFAULT_DEVICE = MiIoDevices.DREAME_VACUUM_P2009;
    private final static String DEFAULTSFILE = "miio-default.json";

    public static void main(String[] args) {
        logger.info("Mi IO SSDP Emulator");
        SSDP ssdp = new SSDP();
        ssdp.start();

        Scanner scan = new Scanner(System.in);
        while (true) {
            String s = scan.next();

            if ("q".equals(s)) {
                logger.info("Quitting");
                ssdp.shutdown();
                break;
            }
            if ("r".equals(s)) {
                logger.info("stop");
                ssdp.shutdown();
                logger.info("starting");

                ;
                ssdp.start();
            }
        }
        scan.close();
    }
}