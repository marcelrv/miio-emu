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

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import miio.discovery.MDNSServiceRegistration;
import miio.emulator.MiIoDevices;
import miio.emulator.MiIoEmulator;

public class Base {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Base.class);
    // enter the default device here
    public final static MiIoDevices DEFAULT_DEVICE = MiIoDevices.POWERSTRIP2;
    private final static String DEFAULTSFILE = "miio-default.yaml";

    private static boolean enableMdns = false;

    public static void main(String[] args) {
        logger.info("Mi IO Emulator");
        MiIoDevices device = DEFAULT_DEVICE;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Scanner scan = new Scanner(System.in);
        Defaults data = new Defaults();
        data.setDid(data.getDid());
        data.setToken(data.getToken());
        MDNSServiceRegistration mdns = new MDNSServiceRegistration();
        while (true) {
            try {
                data = mapper.readValue(new File(DEFAULTSFILE), Defaults.class);
            } catch (IOException e) {
                logger.info("Could not read {}: {}", DEFAULTSFILE, e.getMessage());
            }
            device = MiIoDevices.getType(data.getModel());
            MiIoEmulator emu = new MiIoEmulator(device, data.getDid(), data.getToken());
            logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                    device.getModel(), data.getDid(), data.getToken());
            emu.start();
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
            if (s.startsWith("d") && s.substring(2).trim().length() == 8) {
                data.setDid(s.substring(2).trim());
            } else if (s.startsWith("t") && s.substring(2).trim().length() == 32) {
                data.setToken(s.substring(2).trim());
            } else if (MiIoDevices.UNKNOWN.equals(MiIoDevices.getType(s))) {
                device = DEFAULT_DEVICE;
            } else {
                device = MiIoDevices.getType(s);
            }
            try {
                int d = Integer.valueOf(s);
                if (d >= 0 && d < MiIoDevices.values().length) {
                    device = MiIoDevices.values()[d];
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            data.setModel(device.getModel());

            if (emu != null) {
                emu.stop();
            }
            if (enableMdns) {
                mdns.unRegisterService();
            }
            ObjectWriter writerJ = mapper.writer();
            try (SequenceWriter sw = writerJ.writeValues(new File(DEFAULTSFILE))) {
                sw.write(data);
            } catch (IOException e) {
                logger.info("Could not write defaults file {}", e.getMessage());
            }
        }
        scan.close();
        if (enableMdns) {
            mdns.close();
        }
    }

    /**
     *
     */
    private static void listDevices() {
        for (MiIoDevices d : MiIoDevices.values()) {
            logger.info("Device {} : {}", d.ordinal(), d.getModel());
        }
    }

    private static void listOption() {
        logger.info("this menu:     h");
        logger.info("Setting:       ?");
        logger.info("set did:       d:<did>");
        logger.info("set token:     t:<token>");
        logger.info("List Devices:  l");
        logger.info("Reload response:r");
        logger.info("Quit:          q");
        logger.info("Enter option or Device:");
    }
}
