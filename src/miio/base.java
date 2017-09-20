package miio;

import org.slf4j.LoggerFactory;

import miio.emulator.MiIoEmulator;

public class base {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(base.class);

    public static void main(String[] args) {
        logger.info("Emulator Started");
        MiIoEmulator a = new MiIoEmulator();
        a.start();
        try {
            Thread.sleep(100000000L);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
//
//
//
// try {
// byte[] m = comms(HELLO, "192.168.8.1");
//
// // byte[] m =
// // Utils.hexStringToByteArray("2131002000000000034F0E4500000229335641783733484C36615A7856594263");
// Message n = new Message(m);
// System.out.println(Utils.getHex(n.getSerialByte()));
// System.out.println("token: " + Utils.getHex(n.getChecksum()));
// System.out.println(new String(n.getChecksum()));
//
// System.out.println(Utils.getHex(RoboCrypto.md5(n.getChecksum())));
// System.out.println(Utils.getHex(RoboCrypto.encrypt(n.getChecksum(), n.getChecksum())));
// System.out.println(new String(RoboCrypto.encrypt(n.getChecksum(), n.getChecksum())));
// System.out.println(new String(RoboCrypto.md5(n.getChecksum())));
//
// } catch (NoSuchAlgorithmException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (Exception e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// @Override
// public void onMessageReceived(MiIoSendCommand cmd) {
// // TODO Auto-generated method stub
//
// }
//
// }
//
// private static void sendcommands() {
// byte[] token = Utils.hexStringToByteArray("4e454b466a34694745345858354d4f43");
// List a1 = new ArrayList<String>();
// a1.add("get_status");
// a1.add("find_me"); // {'params': [''], 'id': 1, 'method': 'find_me'}
// a1.add("get_map_v1");
// a1.add("get_clean_summary");
// a1.add("get_timer"); // + timerID
// a1.add("upd_timer");// "upd_timer", ["ts", "on"])
//
// String cmd = "{'method': '" + a1.get(2) + "', 'id': 2}";
//
// sendCommand(cmd, token, "192.168.3.109");
// logger.info("respone {}", sendCommand(cmd, token, "192.168.3.109"));
// }
//
// private static String sendCommand(String command, byte[] token, String IP) {
// // append 0
// ByteBuffer cmdBuf = ByteBuffer.allocate(command.length() + 1);
// cmdBuf.put(command.getBytes());
// cmdBuf.put((byte) 0x00);
// byte[] encr;
// try {
// encr = RoboCrypto.encrypt(cmdBuf.array(), token);
// byte[] response = comms(Message.createMsgData(encr, token), IP);
// Message roboResponse = new Message(response);
// String decryptedResponse = new String(RoboCrypto.decrypt(roboResponse.getData(), token)).trim();
// logger.debug("respone {}", decryptedResponse);
// return decryptedResponse;
//
// } catch (Exception e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// return null;
// }
//
// // import java.io.*;
// // import java.net.*;
//
// public static byte[] comms(byte[] message, String IP) throws Exception {
//
// // BufferedReader inFromUser = new BufferedReader(new InputStreamReader(message));
// DatagramSocket clientSocket = new DatagramSocket(PORT);
// InetAddress IPAddress = InetAddress.getByName(IP);
// byte[] sendData = new byte[1024];
// byte[] receiveData = new byte[1024];
//
// // InetSocketAddress address = new InetSocketAddress("0.0.0.0", PORT);
// // clientSocket.bind(address);
// clientSocket.setSoTimeout(10000);
// sendData = message;
// DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
// clientSocket.send(sendPacket);
// // DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
// // clientSocket.receive(receivePacket);
// sendPacket.setData(new byte[1024]);
// clientSocket.receive(sendPacket);
// byte[] response = sendPacket.getData();
// String modifiedSentence = new String(sendPacket.getData());
// logger.debug("FROM SERVER:" + modifiedSentence);
// logger.debug("FROM SERVER:" + Utils.getHex(response));
// logger.debug("FROM SERVER:" + Utils.getHex(modifiedSentence.getBytes()));
// clientSocket.close();
// return response;
// }
//
// public static void testM() {
// String cmd = "{ \"result\": [ { \"msg_ver\": 4, \"msg_seq\": 18, \"state\": 8, \"battery\": 100, \"clean_time\": 955,
// \"clean_area\": 15370000, \"error_code\": 0, \"map_present\": 0, \"in_cleaning\": 0, \"fan_power\": 60,
// \"dnd_enabled\": 1 } ], \"id\": 1 }";
//
// cmd = "{'method': 'get_status', 'id': 2}";
// byte[] token = Utils.hexStringToByteArray("4e454b466a34694745345858354d4f43");
//
// // cmd += Byte.toString((byte) 0x00);
// // byte[] cmdbyte = new byte[cmd.length() + 1];
// // cmdbyte = cmd.getBytes();
//
// ByteBuffer cmdBuf = ByteBuffer.allocate(cmd.length() + 1);
// cmdBuf.put(cmd.getBytes());
// cmdBuf.put((byte) 0x00);
//
// try {
//
// byte[] encr = RoboCrypto.encrypt(cmdBuf.array(), token);
// Message m = Message.createMsg(encr, token);
//
// logger.info("input: {}", m.toSting());
// logger.info("cmd: {} ", Utils.getHex(cmd.getBytes()));
// logger.info("encr: {} ", Utils.getHex(encr));
// logger.info("encr: {} ", Utils.getHex(new String(encr).getBytes()));
// logger.info("msgdata:{} ", Utils.getHex(m.getData()));
// logger.info("raw: {} ", Utils.getHex(m.getRaw()));
// logger.info("rawer {}", Utils.getHex(Message.createMsgData(encr, token)));
//
// logger.info("cmdmsg {}", Utils.getHex(RoboCrypto.decrypt(m.getData(), token)));
// logger.info("cmdmsg {}", new String((RoboCrypto.decrypt(m.getData(), token))));
//
// // comms(m.getRaw());
//
// byte[] DBG = Utils.hexStringToByteArray(
// "2131005000000000034f0e45593888ac53c1e9cd7a4ae1d93431950b6a6599d7abdf147a60899e35fced688c93eb93d0df20ac396cc4cb787503958bad8e6cb16019c4794fa7ff48f159e771cff380cf");
// // byte[] response = comms((HELLO), "192.168.3.109");
// // m = new Message(DBG);
// // logger.info("msg {}", m.toSting());
//
// // byte[] response = comms(DBG, "192.168.3.109");
// byte[] response = comms(Message.createMsgData(encr, token), "192.168.3.109");
// Message nm = new Message(response);
//
// logger.info("resp {}", Utils.getHex(response));
// logger.info("MD5 h {}", Utils.getHex(Message.getChecksum(java.util.Arrays.copyOf(response, 16), token,
// java.util.Arrays.copyOfRange(response, 32, nm.getLenght()))));
// logger.info("msg {}", nm.toSting());
// logger.info("respone {}", new String((RoboCrypto.decrypt(nm.getData(), token))).trim());
//
// // comms(Message.createMsgData((encr)));
//
// } catch (Exception e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// // Message y = new Message(m.getRaw());
// // logger.info("out: {}", y.toSting());
//
// }
//
// private static void testdecypher() {
// byte[] token = Utils.hexStringToByteArray("4e454b466a34694745345858354d4f43");
//
// logger.info("start decypher");
// byte[] command = Utils.hexStringToByteArray(
// "2131010000000000034f0e45593888acd32bff80f87519df6b919317d42ed8ec5aa8e555a0c28c1d4644458c90cff9baa487777846efbc94ac7f316bc5e175c4ede371baf8e8961e1392c9d852fbf2d82a2b170626cf5c4e9c8962b687b05c733b9af3745e518aab2ff8e4fcf1f48189d8d6ffddb5ff53f10ae948b5e0e52cb8c473f2148531184c80d87302167ef37e9d72557c63deca9d7f3a8d25260d8738a074a2ef6a93431a6801af0fb52d935c50d5da9768de3832fd0eea72fe19701e3c0849fc1d6c1730049d62e505a840bbdf7d8059020832385d08babe6b59a619880ce5190a76fb5c8f125f1a1982a184181a4a2b6034508eb8a72351c8a1256d");
// Message m = new Message(command);
// // logger.info("Message Header :{}", Utils.getHex(m.getHeader()));
// logger.info("Message info:{}", m.toSting());
// logger.info("Message data:{}", Utils.getHex(m.getData()));
//
// try {
// logger.info("Message info:{}", new String(RoboCrypto.decrypt(m.getData(), token)).trim());
// } catch (Exception e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// }
//
// private static void discover() {
// // MaxCubeBridgeDiscovery.startScan();
// logger.info("start");
// logger.debug("start");
// String command = new String(Utils.hexStringToByteArray(
// "2131005000000000034f0e455936fa7e5d72862b3d246a9a9fb2c98780f898ce4215038612de0a30eb06c4bbe1d299d61da4d16c3a78a111ccd058a72292910a135db2f828684144dc7ab77bf6fe083a"));
// MaxCubeBridgeDiscovery n = new MaxCubeBridgeDiscovery();
// // n.startScan();
// // n.sendCommand(command, "192.168.3.109");
//
// n.startScan();
//
// try {
// Thread.sleep(10000);
// } catch (InterruptedException e) {
// logger.info("start");
//
// e.printStackTrace();
// }
//
// logger.info("DONE");
// }
//
// private static void decode() {
// byte[] command = Utils
// .hexStringToByteArray("7b226964223a20312c20226d6574686f64223a20226765745f737461747573227d00");
// byte[] token = Utils.hexStringToByteArray("4e454b466a34694745345858354d4f43");
// // new String (command)
// String i;
//
// try {
// byte[] deco = Utils.hexStringToByteArray(
// "abdf147a60899e35fced688c93eb93d0df20ac396cc4cb787503958bad8e6cb16019c4794fa7ff48f159e771cff380cf");
//
// byte[] theKey = RoboCrypto.md5(token);
// byte[] iv = RoboCrypto.iv(token);
//
// logger.info("key md {}", Utils.getHex(theKey));
// logger.info("key iv {}", Utils.getHex(iv));
// logger.info("key enc {}", Utils.getHex(RoboCrypto.encrypt(command, theKey, iv)));
// logger.info("key dec {}", Utils.getHex(RoboCrypto.decrypt(deco, token)));
//
// } catch (NoSuchAlgorithmException e1) {
// logger.info("keymd5 Encrypt failed");
// e1.printStackTrace();
//
// } catch (Exception e) {
// logger.info("Message Encrypt failed");
//
// e.printStackTrace();
// }
// // expert abdf147a60899e35fced688c93eb93d0df20ac396cc4cb787503958bad8e6cb16019c4794fa7ff48f159e771cff380cf
// }
//
// }
