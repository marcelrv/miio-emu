package miio.emulator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Talks to the emulator over UDP like the binding does. Uses the port from the 'miio.port' system property (see pom).
 */
@Timeout(60)
class MiIoEmulatorTest {
    private static final String TOKEN = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String DID = "AABBCCDD";

    private final byte[] token = Utils.hexStringToByteArray(TOKEN);
    private final byte[] did = Utils.hexStringToByteArray(DID);
    private MiIoEmulator emulator;
    private DatagramSocket client;
    private int id = 0;

    @BeforeEach
    void setUp() throws IOException {
        emulator = new MiIoEmulator("careli.fryer.maf02", "test fryer", DID, TOKEN);
        emulator.start();
        client = new DatagramSocket();
        client.setSoTimeout(10000);
    }

    @AfterEach
    void tearDown() {
        // setUp can fail half way
        if (client != null) {
            client.close();
        }
        if (emulator != null) {
            emulator.close();
        }
    }

    private byte[] receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
        client.receive(packet);
        return Arrays.copyOf(packet.getData(), packet.getLength());
    }

    private JsonObject send(String method, String params) throws Exception {
        String json = "{\"id\":" + (++id) + ",\"method\":\"" + method + "\",\"params\":" + params + "}";
        byte[] data = Message.createMsgData(MiIoCrypto.encrypt(json.getBytes(StandardCharsets.UTF_8), token), token,
                did, (int) (System.currentTimeMillis() / 1000));
        client.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), MiIoReceiver.PORT));
        Message response = new Message(receive());
        assertArrayEquals(response.getChecksum(),
                Message.getChecksum(response.getHeader(), token, response.getData()));
        JsonObject result = JsonParser
                .parseString(new String(MiIoCrypto.decrypt(response.getData(), token), StandardCharsets.UTF_8).trim())
                .getAsJsonObject();
        assertEquals(id, result.get("id").getAsInt());
        return result;
    }

    private int status() throws Exception {
        JsonArray result = send("get_properties", "[{\"did\":\"status\",\"siid\":2,\"piid\":1}]")
                .getAsJsonArray("result");
        JsonObject prop = result.get(0).getAsJsonObject();
        assertEquals(0, prop.get("code").getAsInt());
        return prop.get("value").getAsInt();
    }

    @Test
    void answersHelloWithDeviceIdAndToken() throws Exception {
        byte[] hello = Utils.hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        client.send(new DatagramPacket(hello, hello.length, InetAddress.getLoopbackAddress(), MiIoReceiver.PORT));
        Message response = new Message(receive());
        assertArrayEquals(did, response.getDeviceId());
        assertArrayEquals(token, response.getChecksum());
    }

    @Test
    void answersMiioInfoWithModel() throws Exception {
        JsonObject result = send("miIO.info", "[]").getAsJsonObject("result");
        assertEquals("careli.fryer.maf02", result.get("model").getAsString());
    }

    @Test
    void getAndSetMiotProperties() throws Exception {
        JsonObject set = send("set_properties",
                "[{\"did\":\"target_temperature\",\"siid\":2,\"piid\":4,\"value\":165}]");
        assertEquals(0, set.getAsJsonArray("result").get(0).getAsJsonObject().get("code").getAsInt());

        JsonObject get = send("get_properties", "[{\"did\":\"target-temperature\",\"siid\":2,\"piid\":4}]");
        JsonObject prop = get.getAsJsonArray("result").get(0).getAsJsonObject();
        assertEquals(0, prop.get("code").getAsInt());
        assertEquals(2, prop.get("siid").getAsInt());
        assertEquals(4, prop.get("piid").getAsInt());
        assertEquals(165, prop.get("value").getAsInt());
    }

    @Test
    void actionIsAnsweredWithCodeAndChangesStatus() throws Exception {
        assertEquals(1, status());
        JsonObject action = send("action", "{\"did\":\"actions\",\"siid\":2,\"aiid\":1,\"in\":[]}")
                .getAsJsonObject("result");
        assertEquals(0, action.get("code").getAsInt());
        assertTrue(action.getAsJsonArray("out").isEmpty());
        assertEquals(4, status());
    }

    @Test
    void actionWithoutIdsIsAnsweredWithError() throws Exception {
        JsonObject action = send("action", "{\"did\":\"actions\",\"in\":[]}").getAsJsonObject("result");
        assertEquals(-4000, action.get("code").getAsInt());
        // status unchanged
        assertEquals(1, status());
    }

    @Test
    void secondEmulatorOnSamePortFailsToStart() {
        MiIoEmulator second = new MiIoEmulator("careli.fryer.maf02", "test fryer", DID, TOKEN);
        assertThrows(IOException.class, second::start);
    }
}
