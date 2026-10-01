/**
 * Mi IO device emulator Copyright (C) 2017  M. Verpaalen
 *
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package miio.emulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoReceiver} is responsible for communications with the Mi IO devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */

public class MiIoReceiver {
    public static final byte[] DISCOVER_STRING = Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    public static final int DEFAULT_PORT = 54321;
    /** UDP port to listen on. The openHAB binding always connects to 54321, so only change it for other clients. */
    public static final int PORT = Integer.getInteger("miio.port", DEFAULT_PORT);
    public static final Set<String> IGNORED_TOLKENS = Set.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "00000000000000000000000000000000");

    private static final int MSG_BUFFER_SIZE = 2048;

    private final Logger logger = LoggerFactory.getLogger(MiIoReceiver.class);

    private byte[] deviceId;
    private DatagramSocket socket;

    private List<MiIoMessageListener> listeners = new CopyOnWriteArrayList<>();
    private MessageSenderThread senderThread;

    public MiIoReceiver() throws SocketException {
        // bind right away, so a port that can't be used fails with a clear message instead of in the receiver thread
        try {
            getSocket();
        } catch (SocketException e) {
            throw new SocketException("Could not open UDP port " + PORT + ": " + e.getMessage()
                    + (PORT == DEFAULT_PORT ? ". Is another process (or emulator) using it? On Windows the port can also"
                            + " be inside a reserved range, check 'netsh int ipv4 show excludedportrange protocol=udp'"
                            : ""));
        }
        senderThread = new MessageSenderThread();
        senderThread.start();
    }

    protected List<MiIoMessageListener> getListeners() {
        return listeners;
    }

    /**
     * Registers a {@link MiIoReceiver} to be called back, when data is received.
     * If no {@link MiIoReceiver} exists, when the method is called, it is being set up.
     *
     * @param listener - {@link MiIoMessageListener} to be called back
     */
    public synchronized void registerListener(MiIoMessageListener listener) {
        startReceiver();
        if (!getListeners().contains(listener)) {
            logger.trace("Adding socket listener {}", listener);
            getListeners().add(listener);
        }
    }

    /**
     * Unregisters a {@link XiaomiSocketListener}. If there are no listeners left,
     * the {@link XiaomiSocket} is being closed.
     *
     * @param listener - {@link XiaomiSocketListener} to be unregistered
     */
    public synchronized void unregisterListener(MiIoMessageListener listener) {
        getListeners().remove(listener);
    }

    private synchronized void startReceiver() {
        if (senderThread == null) {
            senderThread = new MessageSenderThread();
        }
        if (!senderThread.isAlive()) {
            senderThread.start();
        }
    }

    private class MessageSenderThread extends Thread {
        public MessageSenderThread() {
            super("Mi IO MessageSenderThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            logger.info("Starting Mi IO MessageSenderThread");
            while (!interrupted()) {
                try {
                    DatagramSocket clientSocket = getSocket();
                    DatagramPacket receivePacket = new DatagramPacket(new byte[MSG_BUFFER_SIZE], MSG_BUFFER_SIZE);
                    clientSocket.receive(receivePacket);
                    byte[] response = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                            receivePacket.getOffset() + receivePacket.getLength());

                    if (Arrays.equals(DISCOVER_STRING, response)) {
                        logger.info("Received Discovery package from {}", receivePacket.getAddress().toString());
                    }
                    if (response.length < 32) {
                        logger.debug("Ignoring too short package ({} bytes) from {}", response.length,
                                receivePacket.getAddress());
                        continue;
                    }

                    Message message = new Message(response);
                    logger.trace("Received {} {}", receivePacket.getAddress().toString(), message.toSting());
                    for (MiIoMessageListener listener : listeners) {
                        logger.trace("inform listener {}", listener);
                        try {
                            listener.onMessageReceived(message, receivePacket.getAddress().toString(),
                                    receivePacket.getSocketAddress());
                        } catch (Exception e) {
                            logger.info("Could not inform listener {}: {}: ", listener, e.getMessage(), e);
                        }
                    }
                } catch (NoSuchElementException e) {
                    // ignore
                } catch (SocketException e) {
                    if (isInterrupted() || socket == null || socket.isClosed()) {
                        // closed on shutdown
                        break;
                    }
                    logger.warn("Socket: {}?", e.getMessage());
                } catch (Exception e) {
                    logger.warn("Error while polling/sending message", e);
                }
            }
            logger.info("Finished Mi IO MessageSenderThread");
        }
    }

    public void sendData(byte[] message, SocketAddress socketAddress) throws IOException {
        DatagramSocket clientSocket = getSocket();
        logger.trace("Connection {}", clientSocket.getLocalPort());
        byte[] sendData = new byte[MSG_BUFFER_SIZE];
        sendData = message;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socketAddress);
        clientSocket.send(sendPacket);
    }

    private DatagramSocket getSocket() throws SocketException {
        if (socket == null || socket.isClosed()) {
            // reuse must be set before binding
            DatagramSocket newSocket = new DatagramSocket(null);
            newSocket.setReuseAddress(true);
            newSocket.setBroadcast(true);
            newSocket.bind(new InetSocketAddress(PORT));
            socket = newSocket;
            return socket;
        }
        return socket;
    }

    public void close() {
        logger.info("Closing Mi IO MessageSenderThread");
        senderThread.interrupt();
        if (socket != null) {
            socket.close();
        }
    }

    public byte[] getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(byte[] deviceId) {
        this.deviceId = deviceId;
    }
}
