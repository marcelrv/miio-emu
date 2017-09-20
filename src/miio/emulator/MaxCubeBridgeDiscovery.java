/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package miio.emulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

//#import org.apache.log4j.*;

/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new MAX!
 * Cube LAN gateway devices on the network
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public class MaxCubeBridgeDiscovery implements Runnable {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // static final String MAXCUBE_DISCOVER_STRING = "eQ3Max*\0**********I";
    String DISCOVER_STRING = new String(
            Utils.hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
    byte[] DISCOVER_BYTE = Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

    // String MAXCUBE_DISCOVER_STRING = new String(Utils.hexStringToByteArray(
    // "2131005000000000034f0e455936fa7e5d72862b3d246a9a9fb2c98780f898ce4215038612de0a30eb06c4bbe1d299d61da4d16c3a78a111ccd058a72292910a135db2f828684144dc7ab77bf6fe083a"));
    private static final int SEARCH_TIME = 15;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(MaxCubeBridgeDiscovery.class);
    // static final Logger logger = Logger.getLogger(MaxCubeBridgeDiscovery.class);
    // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

    static boolean discoveryRunning = false;

    /** The refresh interval for discovery of MAX! Cubes */
    private static final long SEARCH_INTERVAL = 600;

    private static final int PORT = 54321;

    private ScheduledFuture<?> cubeDiscoveryJob;
    private Runnable cubeDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            discoverCube();
        }
    };

    private Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            logger.info("Start MAX! Cube receiving");
            receiveDiscoveryMessage();
        }
    };

    public void startScan() {
        logger.info("Start MAX! Cube discovery");
        discoverCube();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#stopBackgroundDiscovery()
     */
    protected void stopBackgroundDiscovery() {
        logger.info("Stop MAX! Cube background discovery");
        if (cubeDiscoveryJob != null && !cubeDiscoveryJob.isCancelled()) {
            cubeDiscoveryJob.cancel(true);
            cubeDiscoveryJob = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.config.discovery.AbstractDiscoveryService#startBackgroundDiscovery()
     */

    protected void startBackgroundDiscovery() {
        logger.info("Start MAX! Cube background discovery");
        if (cubeDiscoveryJob == null || cubeDiscoveryJob.isCancelled()) {
            cubeDiscoveryJob = scheduler.scheduleWithFixedDelay(cubeDiscoveryRunnable, 0, SEARCH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    private synchronized void discoverCube() {
        // receiveRunnable.run();
        scheduler.schedule(receiveRunnable, 0, TimeUnit.SECONDS);
        logger.info("Run MAX! Cube discovery");

        sendDiscoveryMessage(DISCOVER_STRING);

        logger.info("Done sending broadcast discovery messages.");
        receiveDiscoveryMessage();

        logger.info("Done receiving discovery messages.");
    }

    private void receiveDiscoveryMessage() {

        DatagramSocket bcReceipt = null;

        try {
            discoveryRunning = true;
            bcReceipt = new DatagramSocket(PORT);
            bcReceipt.setReuseAddress(true);
            //   bcReceipt.setBroadcast(true);
            bcReceipt.setSoTimeout(10000);

            while (discoveryRunning) {
                // Wait for a response
                byte[] recvBuf = new byte[1500];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                bcReceipt.receive(receivePacket);

                // We have a response
                byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                        receivePacket.getOffset() + receivePacket.getLength());
                String message = new String(messageBuf);
                logger.info("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
                        message);
                logger.info("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
                        Utils.getHex(messageBuf));
                String h = new String(Utils.hexStringToByteArray("2131"));
                Message msg = new Message(messageBuf);
                logger.info("Message:  {}", msg.toSting());

                // Check if the message is correct
                if (message.startsWith(h) && !message.equals(DISCOVER_STRING)) {
                    String maxCubeIP = receivePacket.getAddress().getHostAddress();

                    // Message msg = new Message(messageBuf);
                    logger.info("Message:  {}", msg.toSting());
                    String maxCubeState = message.substring(0, 8);
                    String serialNumber = message.substring(8, 18);
                    String msgValidid = message.substring(18, 19);
                    String requestType = message.substring(19, 20);
                    String rfAddress = "";
                    logger.info("MAX! Cube found on network");
                    logger.info("Found at  : {}", maxCubeIP);
                    logger.trace("Cube State: {}", maxCubeState);
                    logger.info("Serial    : {}", serialNumber);
                    logger.trace("Msg Valid : {}", msgValidid);
                    logger.trace("Msg Type  : {}", requestType);

                    if (requestType.equals("I")) {
                        rfAddress = Utils.getHex(Arrays.copyOfRange(messageBuf, 21, 24)).replace(" ", "").toLowerCase();
                        String firmwareVersion = Utils.getHex(Arrays.copyOfRange(messageBuf, 24, 26)).replace(" ", ".");
                        logger.info("RF Address: {}", rfAddress);
                        logger.info("Firmware  : {}", firmwareVersion);
                    }
                    discoveryResultSubmission(maxCubeIP, serialNumber, rfAddress);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.trace("No further response");
            discoveryRunning = false;
        } catch (IOException e) {
            logger.info("IO error during MAX! Cube discovery: {}", e.getMessage());
            discoveryRunning = false;
        } finally {
            logger.info("discovery receiving DONE  : ");
            // Close the port!
            try {
                if (bcReceipt != null) {
                    bcReceipt.close();
                }
            } catch (Exception e) {
                logger.info("{}", e.toString());
            }
        }
    }

    private void discoveryResultSubmission(String IpAddress, String cubeSerialNumber, String rfAddress) {
        if (cubeSerialNumber != null) {
            logger.info("Adding new MAX! Cube Lan Gateway on {} with id '{}' to Smarthome inbox", IpAddress,
                    cubeSerialNumber);
            Map<String, Object> properties = new HashMap<>(2);
            properties.put("ip", IpAddress);
            // properties.put(MaxBinding.PROPERTY_SERIAL_NUMBER, cubeSerialNumber);
            // properties.put(MaxBinding.PROPERTY_RFADDRESS, rfAddress);
        }
    }

    /**
     * Send broadcast message over all active interfaces
     *
     * @param discoverString
     *            String to be used for the discovery
     */
    private void sendDiscoveryMessage(String discoverString) {
        DatagramSocket bcSend = null;
        // Find the MaxCube using UDP broadcast
        try {
            bcSend = new DatagramSocket();
            bcSend.setBroadcast(true);

            byte[] sendData = discoverString.getBytes();

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    // InetAddress[] broadcast = new InetAddress[1];
                    // broadcast[0] = InetAddress.getByName("192.168.3.109");

                    InetAddress[] broadcast = new InetAddress[4];
                    broadcast[0] = InetAddress.getByName("224.0.0.1");
                    broadcast[1] = InetAddress.getByName("255.255.255.255");
                    broadcast[2] = interfaceAddress.getBroadcast();
                    //     broadcast[3] = InetAddress.getByName("192.168.3.109");

                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(DISCOVER_BYTE, 32, bc, PORT);
                                bcSend.send(sendPacket);
                            } catch (IOException e) {
                                logger.info("IO error during MAX! Cube discovery: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.info("{}", e.getMessage(), e);
                            }
                            logger.info("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }
            logger.trace("Done looping over all network interfaces. Now waiting for a reply!");

        } catch (IOException e) {
            logger.info("IO error during MAX! Cube discovery: {}", e.getMessage());
        } finally {
            try {
                if (bcSend != null) {
                    bcSend.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }

    }

    public void sendCommand(String discoverString, String ip) {
        DatagramSocket bcSend = null;

        try {
            bcSend = new DatagramSocket();
            // SocketAddress sa;
            // bcSend.bind(SocketAddress(InetAddress.getByName("192.168.3.19")));
            bcSend.setSoTimeout(10000);
            byte[] sendData = discoverString.getBytes();
            InetAddress roboHost = InetAddress.getByName("192.168.3.109");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, roboHost, PORT);
            bcSend.send(sendPacket);

            byte[] recvBuf = new byte[100];
            // Prepare the packet for receive
            sendPacket.setData(recvBuf);
            // Wait for a response from the server
            // socket.receive( packet ) ;

            // DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            bcSend.receive(sendPacket);

            // We have a response
            // byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
            // receivePacket.getOffset() + receivePacket.getLength());
            // We have a response
            byte[] messageBuf = Arrays.copyOfRange(sendPacket.getData(), sendPacket.getOffset(),
                    sendPacket.getOffset() + sendPacket.getLength());

            String message = new String(messageBuf);
            logger.info("Broadcast response from {} : {} '{}'", sendPacket.getAddress(), message.length(), message);
            // logger.info("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
            // Utils.getHex(messageBuf));
            String h = new String(Utils.hexStringToByteArray("2131"));
            Message msg = new Message(messageBuf);
            logger.info("Message:  {}", msg.toSting());

        } catch (IOException e) {
            logger.info("IO error during MAX! Cube discovery: {}", e.getMessage());
        } catch (Exception e) {
            logger.info("{}", e.getMessage(), e);
        }

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

}
