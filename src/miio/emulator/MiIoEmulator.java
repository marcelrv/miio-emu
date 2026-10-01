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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import miio.emulator.fakeresponses.FakeResponseGenerator;

public class MiIoEmulator implements MiIoMessageListener {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MiIoEmulator.class);
    private MiIoReceiver comms;
    private final JsonParser parser = new JsonParser();

    private byte[] did = Utils.hexStringToByteArray("AABBCCDD");
    private byte[] token = Utils.hexStringToByteArray("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    private Map<String, String> cmds = new HashMap<String, String>();

    // private final static MiIoDevices EMULATED_DEVICE = AIR_PURIFIER;
    private final String model;
    private final String description;
    private final FakeResponseGenerator responseGen;

    public MiIoEmulator(MiIoDevices emulatedDevice, String did, String token) {
        this(emulatedDevice.getModel(), emulatedDevice.getDescription(), did, token);
    }

    public MiIoEmulator(String model, String description, String did, String token) {
        this.model = model;
        this.description = description;
        this.did = Utils.hexStringToByteArray(did);
        this.token = Utils.hexStringToByteArray(token);
        responseGen = new FakeResponseGenerator(model);
    }

    public void start() throws IOException {
        comms = new MiIoReceiver();
        comms.registerListener(this);
        logger.info("Mi Io Emulator started as device {} ({})", description, model);
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
                logger.warn("Could not parse '{}' <- {} gave error {}", decryptedResponse, socketAddress,
                        e.getMessage());
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

            if (!cmds.containsKey(method) && params != null) {
                cmds.put(method, params.toString());
            }
            logger.info("<-Received {} command {} from {}", miCmd, command, socketAddress);

            JsonObject fullCommand = new JsonObject();
            fullCommand.addProperty("id", msgId);

            switch (miCmd) {
                case MIIO_INFO:
                    String res = "{\"life\":88749,\"cfg_time\":0,\"token\":\"" + Utils.getHex(token)
                            + "\",\"mac\":\"34:CE:00:84:D6:AA\",\"fw_ver\":\"1.2.4_59\",\"hw_ver\":\"MC200\",\"model\":\""
                            + model
                            + "\",\"wifi_fw_ver\":\"SD878x-14.76.36.p79-702.1.0-WM\",\"ap\":{\"rssi\":-36,\"ssid\":\"mygateway1\",\"bssid\":\"34:81:C4:24:29:BB\"},\"netif\":{\"localIp\":\"192.168.3.126\",\"mask\":\"255.255.255.0\",\"gw\":\"192.168.3.1\"},\"mmfree\":27272,\"ot\":\"otu\",\"otu_stat\":[307,292,247,0,247,419],\"ott_stat\":[0,0,0,0]}";
                    fullCommand.add("result", parser.parse(res).getAsJsonObject());
                    break;
                case GET_PROPERTIES:
                    JsonArray miotresult = new JsonArray();
                    for (JsonElement e : params.getAsJsonArray()) {
                        JsonObject miot = e.getAsJsonObject();
                        JsonObject miotResponse = miotResponse(miot);
                        miotResponse.add("value", responseGen.getProperty(did(miot), integer(miot, "siid"),
                                integer(miot, "piid")));
                        miotresult.add(miotResponse);
                    }
                    fullCommand.add("result", miotresult);
                    break;

                case SET_PROPERTIES:
                    JsonArray setResult = new JsonArray();
                    for (JsonElement e : params.getAsJsonArray()) {
                        JsonObject miot = e.getAsJsonObject();
                        boolean ok = responseGen.setProperty(did(miot), integer(miot, "siid"), integer(miot, "piid"),
                                miot.get("value"));
                        JsonObject miotResponse = miotResponse(miot);
                        // MIoT: 0 = ok, -4000 = device error
                        miotResponse.addProperty("code", ok ? 0 : -4000);
                        setResult.add(miotResponse);
                    }
                    fullCommand.add("result", setResult);
                    break;

                case ACTION:
                    // unlike other commands the miot action parameters are a json object instead of an array
                    JsonObject actionParams = params.isJsonArray() ? params.getAsJsonArray().get(0).getAsJsonObject()
                            : params.getAsJsonObject();
                    JsonObject actionResult = new JsonObject();
                    actionResult.addProperty("code", 0);
                    actionResult.add("out", responseGen.performAction(integer(actionParams, "siid"),
                            integer(actionParams, "aiid")));
                    fullCommand.add("result", actionResult);
                    break;

                case GET_PROPERTY:
                    JsonArray result = new JsonArray();
                    // respond to each property
                    for (JsonElement e : params.getAsJsonArray()) {
                        // Object r = FakeResponses.getCommand(e.getAsString()).getResponse();
                        // result.add(parser.parse(r.toString()));
                        result.add(responseGen.getPropery(e.getAsString()));
                    }
                    fullCommand.add("result", result);
                    break;
                default:
                    fullCommand.add("result", responseGen.getResponse(method, params));
                    break;
            }
            logger.info("->Send response {} -> {}", fullCommand.toString(), socketAddress.toString());
            sendResponse(fullCommand, socketAddress);

        } catch (

        JsonSyntaxException e) {
            logger.warn("Could not parse '{}' <- {} gave error {}", response, socketAddress, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Could not handle '{}' <- {}", response, socketAddress, e);
        }
    }

    private static String did(JsonObject miot) {
        return miot.has("did") && !miot.get("did").isJsonNull() ? miot.get("did").getAsString() : null;
    }

    private static Integer integer(JsonObject json, String name) {
        return json.has(name) && !json.get(name).isJsonNull() ? json.get(name).getAsInt() : null;
    }

    /** @return a MIoT response entry with the identifiers of the request and code 0 */
    private static JsonObject miotResponse(JsonObject request) {
        JsonObject response = new JsonObject();
        for (String key : new String[] { "did", "siid", "piid" }) {
            if (request.has(key)) {
                response.add(key, request.get(key));
            }
        }
        response.addProperty("code", 0);
        return response;
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

    public void saveResponses() {
        responseGen.saveResponses();
    }

    public void stop() {
        responseGen.saveResponses();
        comms.close();
    }

    public void reload() {
        responseGen.loadResponses();
    }
}
