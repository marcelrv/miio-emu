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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import miio.emulator.MiIoDevices;
import miio.emulator.MiIoEmulator;

public class Base {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Base.class);
    // enter the default device here
    public final static MiIoDevices DEFAULT_DEVICE = MiIoDevices.POWERSTRIP2;
    private final static String DEFAULTSFILE = "miio-default.json";

    public static void main(String[] args) {
        logger.info("Mi IO Emulator");
        MiIoDevices device = DEFAULT_DEVICE;
        Scanner scan = new Scanner(System.in);
        Defaults data = new Defaults();
        data.setDid(data.getDid());
        data.setToken(data.getToken());
        while (true) {
            Gson gson = new Gson();
            JsonReader reader;
            try {
                reader = new JsonReader(new FileReader(DEFAULTSFILE));
                data = gson.fromJson(reader, Defaults.class);
            } catch (FileNotFoundException e1) {
                logger.info("Defaults file not found {}", DEFAULTSFILE);
            }
            device = MiIoDevices.getType(data.getModel());
            MiIoEmulator emu = new MiIoEmulator(device, data.getDid(), data.getToken());
            logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                    device.getModel(), data.getDid(), data.getToken());
            emu.start();
            listOption();
            String s = scan.next();
            if ("?".equals(s)) {
                logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                        device.getModel(), data.getDid(), data.getToken());
            }
            if ("q".equals(s)) {
                break;
            }
            if ("l".equals(s)) {
                listDevices();
            }

            if (s.startsWith("d") && s.substring(2).trim().length() == 8) {
                data.setDid(s.substring(2).trim());
            } else if (s.startsWith("t") && s.substring(2).trim().length() == 32) {
                data.setToken(s.substring(2).trim());
            } else if (MiIoDevices.UNKNOWN.equals(MiIoDevices.getType(s))) {
                device = DEFAULT_DEVICE;
            } else {
                device = MiIoDevices.getType(s);
            }
            data.setModel(device.getModel());
            try {
                int d = Integer.valueOf(s);
                if (d >= 0 && d < MiIoDevices.values().length) {
                    device = MiIoDevices.values()[d];
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            if (emu != null) {
                emu.stop();
            }

            try (Writer writer = new FileWriter(DEFAULTSFILE)) {
                gson = new GsonBuilder().create();
                gson.toJson(data, writer);
            } catch (IOException e) {
                logger.info("Could not write defaults file {}", e.getMessage());
            }
        }
        scan.close();
    }

    /**
     *
     */
    private static void listDevices() {
        for (MiIoDevices d : MiIoDevices.values()) {
            logger.debug("Device {} : {}", d.ordinal(), d.getModel());
        }
    }

    private static void listOption() {
        logger.info("this menu:     h");
        logger.info("Setting:       ?");
        logger.info("set did:       d:<did>");
        logger.info("set token:     t:<token>");
        logger.info("List Devices:  l");
        logger.info("Quit:          q");
        logger.info("Enter option or Device:");
    }
}
