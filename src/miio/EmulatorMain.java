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
package miio;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import miio.discovery.MDNSServiceRegistration;
import miio.emulator.MiIoDevices;
import miio.emulator.MiIoEmulator;

public class EmulatorMain {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(EmulatorMain.class);
    // enter the default device here
    public final static MiIoDevices DEFAULT_DEVICE = MiIoDevices.DREAME_VACUUM_P2009;
    // private final static String DEFAULTSFILE = "miio-default.yaml";
    private final static String DEFAULTSFILE = "miio-default.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean enableMdns = false;

    /**
     * Start without arguments for the interactive menu. With arguments the emulator starts directly and runs until it
     * is killed: EmulatorMain &lt;model&gt; [token] [did]
     */
    public static void main(String[] args) throws InterruptedException {
        logger.info("Mi IO Emulator");
        if (args.length > 0) {
            runHeadless(args);
            return;
        }
        MiIoDevices device = DEFAULT_DEVICE;
        Scanner scan = new Scanner(System.in);
        Defaults data = null;
        MDNSServiceRegistration mdns = new MDNSServiceRegistration();
        while (true) {
            try (FileReader reader = new FileReader(DEFAULTSFILE)) {
                data = GSON.fromJson(reader, Defaults.class);
            } catch (IOException e) {
                logger.info("Could not read {}: {}", DEFAULTSFILE, e.getMessage());
            }
            if (data == null) {
                logger.info("Could not read defaults from file {}. Using initial values.", DEFAULTSFILE);
                data = new Defaults();
            }
            data.setDid(data.getDid());
            data.setToken(data.getToken());
            data.setModel(data.getModel());
            device = MiIoDevices.getType(data.getModel());
            // the model from the defaults file is used as is, so models missing in the device list work too
            MiIoEmulator emu = new MiIoEmulator(data.getModel(), device.getDescription(), data.getDid(),
                    data.getToken());
            logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                    data.getModel(), data.getDid(), data.getToken());
            try {
                emu.start();
            } catch (IOException e) {
                logger.error("{}", e.getMessage());
                break;
            }
            if (enableMdns) {
                mdns.registerService(data.getModel(), data.getDid());
            }

            listOption();
            String s = scan.next();
            if ("?".equals(s)) {
                logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                        device.getModel(), data.getDid(), data.getToken());
            }
            if ("q".equals(s)) {
                emu.stop();
                break;
            }
            if ("l".equals(s)) {
                listDevices();
            }
            if ("r".equals(s)) {
                emu.reload();
            }
            if ("s".equals(s)) {
                emu.saveResponses();
            }
            if (s.startsWith("d:") && s.substring(2).trim().length() == 8) {
                data.setDid(s.substring(2).trim());
            } else if (s.startsWith("t:") && s.substring(2).trim().length() == 32) {
                data.setToken(s.substring(2).trim());
            } else if (MiIoDevices.UNKNOWN.equals(MiIoDevices.getType(s))) {
                // device = DEFAULT_DEVICE;
                logger.info("Unknown device");
            } else {
                device = MiIoDevices.getType(s);
                data.setModel(device.getModel());
            }
            try {
                int d = Integer.valueOf(s);
                if (d >= 0 && d < MiIoDevices.values().length) {
                    device = MiIoDevices.values()[d];
                    data.setModel(device.getModel());
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            // data.setModel(device.getModel());

            if (emu != null) {
                emu.stop();
            }
            if (enableMdns) {
                mdns.unRegisterService();
            }

            try (PrintWriter writer = new PrintWriter(DEFAULTSFILE)) {
                GSON.toJson(data, writer);
                logger.info("Defaults file written:{}", DEFAULTSFILE);
            } catch (FileNotFoundException e) {
                logger.info("Could not write defaults file {}", e.getMessage());
            }
        }
        scan.close();
        if (enableMdns) {
            mdns.close();
        }
    }

    private static void runHeadless(String[] args) throws InterruptedException {
        Defaults data = new Defaults();
        data.setModel(args[0]);
        if (args.length > 1) {
            data.setToken(args[1]);
        }
        if (args.length > 2) {
            data.setDid(args[2]);
        }
        MiIoDevices device = MiIoDevices.getType(data.getModel());
        String description = MiIoDevices.UNKNOWN.equals(device) ? "unlisted model" : device.getDescription();
        MiIoEmulator emu = new MiIoEmulator(data.getModel(), description, data.getDid(), data.getToken());
        logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", description, data.getModel(),
                data.getDid(), data.getToken());
        try {
            emu.start();
        } catch (IOException e) {
            logger.error("{}", e.getMessage());
            System.exit(1);
        }
        // the receiver runs in a daemon thread. Values changed by the client are not saved when stopped this way
        // (use the interactive menu to save)
        Thread.currentThread().join();
    }

    private static void listDevices() {
        for (MiIoDevices d : MiIoDevices.values()) {
            logger.info("Device {} : {} - {}", d.ordinal(), d.getModel(), d.getDescription());
        }
    }

    private static void listOption() {
        logger.info("this menu:     h");
        logger.info("Setting:       ?");
        logger.info("set did:       d:<did>");
        logger.info("set token:     t:<token>");
        logger.info("List Devices:  l");
        logger.info("Save Responses:s");
        logger.info("Reload response:r");
        logger.info("Quit:          q");
        logger.info("Enter option or Device:");
    }
}
