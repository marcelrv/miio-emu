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
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MiIoReceiver} is responsible for communications with the Mi IO devices
 *
 * The MiIoAsyncCommunication is WORK IN PROGRESS! to replace the synchronous MiIoCommunication class
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoReceiver {
    public static final byte[] DISCOVER_STRING = Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    public static final int PORT = 54321;
    public static final Set<String> IGNORED_TOLKENS = ImmutableSet.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
            "00000000000000000000000000000000");

    private static final int MSG_BUFFER_SIZE = 2048;
    private static final int TIMEOUT = 10000;

    private final Logger logger = LoggerFactory.getLogger(MiIoReceiver.class);

    private final String ip;
    private final byte[] token;
    private byte[] deviceId;
    private DatagramSocket socket;

    private List<MiIoMessageListener> listeners = new CopyOnWriteArrayList<>();

    private AtomicInteger id = new AtomicInteger();
    private int timeDelta;
    private int timeStamp;
    private final JsonParser parser;
    private MessageSenderThread senderThread;
    private boolean connected;
    private int errorCounter;
    private boolean needPing = true;
    private static final int MAX_ERRORS = 3;

    private ConcurrentLinkedQueue<MiIoSendCommand> concurrentLinkedQueue = new ConcurrentLinkedQueue<MiIoSendCommand>();

    public MiIoReceiver(String ip, byte[] token, byte[] did, int id) {
        this.ip = ip;
        this.token = token;
        this.deviceId = did;
        setId(id);
        parser = new JsonParser();
        senderThread = new MessageSenderThread();
        senderThread.start();
    }

    protected List<MiIoMessageListener> getListeners() {
        return listeners;
    }

    /**
     * Registers a {@link XiaomiSocketListener} to be called back, when data is received.
     * If no {@link XiaomiSocket} exists, when the method is called, it is being set up.
     *
     * @param listener - {@link XiaomiSocketListener} to be called back
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

    public int queueCommand(MiIoCommand command) throws MiIoCryptoException, IOException {
        return queueCommand(command, "[]");
    }

    public int queueCommand(MiIoCommand command, String params) throws MiIoCryptoException, IOException {
        return queueCommand(command.getCommand(), params);
    }

    public int queueCommand(String command, String params)
            throws MiIoCryptoException, IOException, JsonSyntaxException {
        try {
            JsonObject fullCommand = new JsonObject();
            int cmdId = id.incrementAndGet();
            fullCommand.addProperty("id", cmdId);
            fullCommand.addProperty("method", command);
            fullCommand.add("params", parser.parse(params));
            MiIoSendCommand sendCmd = new MiIoSendCommand(cmdId, MiIoCommand.getCommand(command),
                    fullCommand.toString());
            concurrentLinkedQueue.add(sendCmd);
            logger.info("Command added to Queue {} -> {} (Device: {} token: {} Queue: {})", fullCommand.toString(), ip,
                    Utils.getHex(deviceId), Utils.getHex(token), concurrentLinkedQueue.size());

            return cmdId;
        } catch (JsonSyntaxException e) {
            logger.warn("Send command '{}' with parameters {} -> {} (Device: {}) gave error {}", command, params, ip,
                    Utils.getHex(deviceId), e.getMessage());
            throw e;
        }
    }

// MiIoSendCommand sendMiIoSendCommand(MiIoSendCommand miIoSendCommand, int port) {
// String errorMsg = "Unknown Error while sending command";
// String decryptedResponse = "";
// try {
// decryptedResponse = sendCommand(miIoSendCommand.getCommandString(), token, ip, deviceId, port);
// JsonElement response;
// response = parser.parse(decryptedResponse);
// if (response.isJsonObject()) {
// logger.trace("Received JSON message {}", response.toString());
// miIoSendCommand.setResponse(response.getAsJsonObject());
// return miIoSendCommand;
// } else {
// errorMsg = "Received message is invalid JSON";
// logger.info("{}: {}", errorMsg, decryptedResponse);
// }
// } catch (MiIoCryptoException | IOException e) {
// logger.warn("Send command '{}' -> {} (Device: {}) gave error {}", miIoSendCommand.getCommandString(), ip,
// Utils.getHex(deviceId), e.getMessage());
// errorMsg = e.getMessage();
// } catch (JsonSyntaxException e) {
// logger.warn("Could not parse '{}' <- {} (Device: {}) gave error {}", decryptedResponse,
// miIoSendCommand.getCommandString(), Utils.getHex(deviceId), e.getMessage());
// errorMsg = "Received message is invalid JSON";
// }
// JsonObject erroResp = new JsonObject();
// erroResp.addProperty("error", errorMsg);
// miIoSendCommand.setResponse(erroResp);
// return miIoSendCommand;
// }

    private synchronized void startReceiver() {
        needPing = true;
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

                    if (DISCOVER_STRING.equals(response)) {
                        logger.info("Received Discovery package from {}, data {} from {}",
                                receivePacket.getAddress().toString());

                    }

                    Message message = new Message(response);
                    // logger.info("Received {} {}", receivePacket.getAddress().toString(), message.toSting());

                    for (MiIoMessageListener listener : listeners) {
                        logger.trace("inform listener {}, data {} from {}", listener);
                        try {
                            listener.onMessageReceived(message, receivePacket.getAddress().toString(),
                                    receivePacket.getSocketAddress());
                        } catch (Exception e) {
                            logger.info("Could not inform listener {}: {}: ", listener, e.getMessage(), e);
                        }
                    }
                } catch (NoSuchElementException e) {
                    // ignore

                } catch (Exception e) {
                    logger.warn("Error while polling/sending message", e);
                }
            }
            logger.info("Finished Mi IO MessageSenderThread");
        }
    }

    private void sendCommand(String command, byte[] token, String ip, byte[] deviceId, int port)
            throws MiIoCryptoException, IOException {
        byte[] encr;
        encr = MiIoCrypto.encrypt(command.getBytes(), token);
        timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
        byte[] sendMsg = Message.createMsgData(encr, token, deviceId, timeStamp + timeDelta);
        sendData(sendMsg, ip, port);

    }

    private void pingFail() {
        connected = false;

    }

    private void pingSuccess() {
        if (!connected) {
            connected = true;

        }
    }

    public void sendData(byte[] message, SocketAddress socketAddress) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        DatagramSocket clientSocket = getSocket();
        logger.trace("Connection {}:{}", ip, clientSocket.getLocalPort());
        byte[] sendData = new byte[MSG_BUFFER_SIZE];
        sendData = message;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socketAddress);
        clientSocket.send(sendPacket);
    }

    public void sendData(byte[] message, String ip, int port) throws IOException {
        InetAddress ipAddress = InetAddress.getByName(ip);
        DatagramSocket clientSocket = getSocket();
        logger.trace("Connection {}:{}", ip, clientSocket.getLocalPort());
        byte[] sendData = new byte[MSG_BUFFER_SIZE];
        sendData = message;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, PORT);
        clientSocket.send(sendPacket);
    }

    private DatagramSocket getSocket() throws SocketException {
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
            // socket.setSoTimeout(TIMEOUT);
            return socket;
        } else {
            return socket;
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
        senderThread.interrupt();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id.incrementAndGet();
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id.set(id);
    }

    /**
     * Time delta between device time & server time
     */
    public int getTimeDelta() {
        return timeDelta;
    }

    public byte[] getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(byte[] deviceId) {
        this.deviceId = deviceId;
    }

    public int getQueueLenght() {
        return concurrentLinkedQueue.size();
    }
}
