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
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class MiIoEmulator implements MiIoMessageListener {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MiIoEmulator.class);
    private MiIoReceiver comms;
    private final JsonParser parser = new JsonParser();

    private byte[] did = Utils.hexStringToByteArray("AABBCCDD");
    private byte[] token = Utils.hexStringToByteArray("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    // private final static MiIoDevices EMULATED_DEVICE = AIR_PURIFIER;
    private final MiIoDevices emulatedDevice;

    public MiIoEmulator(MiIoDevices emulatedDevice, String did, String token) {
        this.emulatedDevice = emulatedDevice;
        this.did = Utils.hexStringToByteArray(did);
        this.token = Utils.hexStringToByteArray(token);
    }

    public void start() {
        comms = new MiIoReceiver();
        comms.registerListener(this);
        logger.info("Mi Io Emulator started as device {} ({})", emulatedDevice.getDescription(),
                emulatedDevice.getModel());
    }

    @Override
    public void onMessageReceived(Message message, String client, SocketAddress socketAddress) {
        logger.trace("onMessage received from {} {}", client, socketAddress);
        String decryptedResponse = "";

        if (message.getLength() == 32) {
            logger.info("<-Received Ping Request from {}", socketAddress);
            LocalDateTime y = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
            Message pingResponse;
            try {
                pingResponse = Message.createMsg(new byte[0], token, did, (int) y.toEpochSecond(ZoneOffset.UTC));
                pingResponse.setChecksum(token);
                pingResponse.getRawData();
                logger.info("->Send Ping response to {}", socketAddress);
                comms.sendData(pingResponse.getRawData(), socketAddress);
            } catch (MiIoCryptoException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                decryptedResponse = new String(MiIoCrypto.decrypt(message.getData(), token), "UTF-8").trim();
                logger.debug("Received command from {}: {}", client, decryptedResponse);

                JsonElement response = parser.parse(decryptedResponse);
                if (response.isJsonObject()) {
                    commandReceived(response, socketAddress);
                    return;
                } else {
                    logger.info("Received message is not Json {}", response);

                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MiIoCryptoException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                logger.warn("Could not parse '{}' <- {} (Device: {}) gave error {}", decryptedResponse, socketAddress,
                        e);
            }
            // return decryptedResponse;
        }

    }

    private void commandReceived(JsonElement response, SocketAddress socketAddress) {

        try {
            JsonObject command = response.getAsJsonObject();
            int msgId = command.get("id").getAsInt();
            String method = command.get("method").getAsString();
            JsonElement params = command.get("params");
            MiIoCommand miCmd = MiIoCommand.getCommand(method);

            logger.info("<-Received {} ({}) command from {}", miCmd, command, response);

            JsonObject fullCommand = new JsonObject();
            fullCommand.addProperty("id", msgId);

            switch (miCmd) {
                case MIIO_INFO:
                    String res = "{\"life\":88749,\"cfg_time\":0,\"token\":\"" + Utils.getHex(token)
                            + "\",\"mac\":\"34:CE:00:84:D6:AA\",\"fw_ver\":\"1.2.4_59\",\"hw_ver\":\"MC200\",\"model\":\""
                            + emulatedDevice.getModel()
                            + "\",\"wifi_fw_ver\":\"SD878x-14.76.36.p79-702.1.0-WM\",\"ap\":{\"rssi\":-36,\"ssid\":\"here\",\"bssid\":\"34:81:C4:24:29:BB\"},\"netif\":{\"localIp\":\"192.168.1.73\",\"mask\":\"255.255.255.0\",\"gw\":\"192.168.1.199\"},\"mmfree\":27272,\"ot\":\"otu\",\"otu_stat\":[307,292,247,0,247,419],\"ott_stat\":[0,0,0,0]}";
                    fullCommand.add("result", parser.parse(res).getAsJsonObject());
                    break;
                case GET_PROPERTY:
                    JsonArray result = new JsonArray();
                    // respond to each property
                    for (JsonElement e : params.getAsJsonArray()) {
                        Object r = FakeResponses.getCommand(e.getAsString()).getResponse();
                        result.add(parser.parse(r.toString()));
                    }
                    fullCommand.add("result", result);
                    break;
                default:
                    logger.info("No Fake response for command {} -> {}", miCmd.toString(), socketAddress.toString());
                    JsonArray resultok = new JsonArray();
                    resultok.add("ok");
                    fullCommand.add("result", resultok);
                    break;
            }
            logger.info("->Send response {} -> {}", fullCommand.toString(), socketAddress.toString());
            sendResponse(fullCommand, socketAddress);

        } catch (

        JsonSyntaxException e) {
            logger.warn("Could not parse '{}' <- {} (Device: {}) gave error {}", response, socketAddress, e);
        }
    }

    private void sendResponse(JsonObject fullCommand, SocketAddress socketAddress) {
        logger.debug("Send command to {}: {}", socketAddress, fullCommand.toString());

        try {
            byte[] encr;
            encr = MiIoCrypto.encrypt(fullCommand.toString().getBytes(), token);
            int timeStamp = (int) TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTime().getTime());
            byte[] sendMsg = Message.createMsgData(encr, token, did, timeStamp);

            comms.sendData(sendMsg, socketAddress);
        } catch (IOException | MiIoCryptoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void stop() {
        comms.close();
    }
}
