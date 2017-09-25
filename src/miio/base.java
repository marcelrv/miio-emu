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

import java.util.Scanner;

import org.slf4j.LoggerFactory;

import miio.emulator.MiIoDevices;
import miio.emulator.MiIoEmulator;

public class base {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(base.class);
    // enter the default device here
    private final static MiIoDevices DEFAULT = MiIoDevices.POWERSTRIP2;
    private final static String DEFAULT_DID = "AABBCCDD";
    private final static String DEFAULT_TOKEN = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    public static void main(String[] args) {
        logger.info("Mi IO Emulator");
        MiIoDevices device = DEFAULT;
        String did = DEFAULT_DID;
        String token = DEFAULT_TOKEN;
        Scanner scan = new Scanner(System.in);
        while (true) {
            MiIoEmulator emu = new MiIoEmulator(device, did, token);
            logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                    device.getModel(), did, token);
            emu.start();
            listOption();
            String s = scan.next();
            if ("?".equals(s)) {
                logger.info("Mi Io Emulator started as device {} {} (did: {} token: {})", device.getDescription(),
                        device.getModel(), did, token);
            }
            if ("q".equals(s)) {
                break;
            }
            if ("l".equals(s)) {
                listDevices();
            }

            if (s.startsWith("d") && s.substring(2).trim().length() == 8) {
                did = s.substring(2).trim();
            } else if (s.startsWith("t") && s.substring(2).trim().length() == 32) {
                token = s.substring(2).trim();
            } else if (MiIoDevices.UNKNOWN.equals(MiIoDevices.getType(s))) {
                device = DEFAULT;
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
            if (emu != null) {
                emu.stop();
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
