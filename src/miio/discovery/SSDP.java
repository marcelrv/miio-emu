package miio.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import miio.emulator.fakeresponses.FakeResponseGenerator;

public class SSDP extends Thread {

    /**
     * Default IPv4 multicast address for SSDP messages
     */
    public static final String ADDRESS = "239.255.255.250";
    public static final int SSDP_PORT = 1902; // Yeelight uses 1902 iso standard 1900

    public static final String IPV6_LINK_LOCAL_ADDRESS = "FF02::C";
    public static final String IPV6_SUBNET_ADDRESS = "FF03::C";
    public static final String IPV6_ADMINISTRATIVE_ADDRESS = "FF04::C";
    public static final String IPV6_SITE_LOCAL_ADDRESS = "FF05::C";
    public static final String IPV6_GLOBAL_ADDRESS = "FF0E::C";

    public static final String ST = "ST";
    public static final String LOCATION = "LOCATION";
    public static final String NT = "NT";
    public static final String NTS = "NTS";

    /* Definitions of start line */
    public static final String SL_NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String SL_MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String SL_OK = "HTTP/1.1 200 OK";

    /* Definitions of notification sub type */
    public static final String NTS_ALIVE = "ssdp:alive";
    public static final String NTS_BYEBYE = "ssdp:byebye";
    public static final String NTS_UPDATE = "ssdp:update";

    public static final String LOG_TAG = "SSDP";

    private SocketAddress mMulticastGroupAddress = new InetSocketAddress("239.255.255.250", SSDP_PORT);
    private MulticastSocket mMulticastSocket;
    private DatagramSocket mUnicastSocket;

    private NetworkInterface mNetIf;

    private boolean mRunning = false;
    private final Logger logger = LoggerFactory.getLogger(FakeResponseGenerator.class);

    @Override
    public synchronized void start() {
        mRunning = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            mMulticastSocket = new MulticastSocket(1900);
            mMulticastSocket.setLoopbackMode(true);
            mMulticastSocket.joinGroup(mMulticastGroupAddress, mNetIf);

            mUnicastSocket = new DatagramSocket(null);
            mUnicastSocket.setReuseAddress(true);
            // mUnicastSocket.bind();

        } catch (IOException e) {
            logger.debug("Setup SSDP failed.", e);
        }
        while (mRunning) {
            DatagramPacket dp = null;
            try {
                dp = receive();

                String startLine = parseStartLine(dp);
                logger.debug("Search: {} from {}", startLine, dp.getAddress());
                logger.debug("Data: {}", new String(dp.getData()));

                if (startLine.equals(SL_MSEARCH)) {

                    String st = parseHeaderValue(dp, ST);

                    if (st.contains("wifi_bulb")) {
                        String responsePayload = "HTTP/1.1 200 OK\r\n" + "Cache-Control: max-age=3600\r\n" + "Date:"
                                + "\r\n" + "Ext:" + "\r\n" + "Location: yeelight://"
                                + InetAddress.getLocalHost().getHostAddress().toString() + ":55443\n"
                                + "Server: POSIX UPnP/1.0 YGLC/1\n" + "id: 0x000000000015243f\n" + "model: color\n"
                                + "fw_ver: 18\n"
                                + "support: get_prop set_default set_power toggle set_bright start_cf stop_cf set_scene "
                                + "cron_add cron_get cron_del set_ct_abx set_rgb\r\n";
                        DatagramPacket response = new DatagramPacket(responsePayload.getBytes(),
                                responsePayload.length(), new InetSocketAddress(dp.getAddress(), dp.getPort()));
                        mUnicastSocket.send(response);

                        logger.debug("Responding to " + dp.getAddress().getHostAddress());
                    }
                }
            } catch (IOException e) {
                logger.debug("SSDP fail.", e);
            }
        }
        logger.debug("SSDP shutdown.");

    }

    public synchronized void shutdown() {
        mRunning = false;
    }

    private DatagramPacket receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        mMulticastSocket.receive(dp);

        return dp;
    }

    private String parseHeaderValue(String content, String headerName) {
        Scanner s = new Scanner(content);
        s.nextLine(); // Skip the start line

        while (s.hasNextLine()) {
            String line = s.nextLine();
            int index = line.indexOf(':');
            String header = line.substring(0, index);
            if (headerName.equalsIgnoreCase(header.trim())) {
                return line.substring(index + 1).trim();
            }
        }

        return null;
    }

    private String parseHeaderValue(DatagramPacket dp, String headerName) {
        return parseHeaderValue(new String(dp.getData()), headerName);
    }

    private String parseStartLine(String content) {
        Scanner s = new Scanner(content);
        return s.nextLine();
    }

    private String parseStartLine(DatagramPacket dp) {
        return parseStartLine(new String(dp.getData()));
    }
}

/*
 * 1. The start line must be "M-SEARCH * HTTP/1.1" without any leading LWP.
 * 2. "HOST" header is optional, if it's present, the value should be "239.255.255.250:1982".
 * 3. "MAN" header is required. The value for "MAN" header must be "ssdp:discover",
 * double quotes included.
 * 4. "ST" header is required. The value for "ST" header must be "wifi_bulb".
 * 5. The headers are case-insensitive while the start line and all the header values are case
 * sensitive. Each line should be terminated by "\r\n"
 */
