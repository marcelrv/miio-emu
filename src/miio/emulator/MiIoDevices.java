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

/**
 * MiIO Devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum MiIoDevices {
    VACUUM("rockrobo.vacuum.v1", "Mi Robot Vacuum"),
    AIR_PURIFIER("zhimi.airpurifier.m1", "Mi Air Purifier"),
    AIR_PURIFIER1("zhimi.airpurifier.v1", "Mi Air Purifier v1"),
    AIR_PURIFIER2("zhimi.airpurifier.v2", "Mi Air Purifier v2"),
    AIR_PURIFIER3("zhimi.airpurifier.v3", "Mi Air Purifier v3"),
    AIR_PURIFIER6("zhimi.airpurifier.v6", "Mi Air Purifier v6"),
    HUMIDIFIER("zhimi.humidifier.v1", "Mi Humdifier"),
    AIRMONITOR1("zhimi.airmonitor.v1", "Mi Air Monitor v1"),
    WATER_PURIFIER2("yunmi.waterpuri.v2", "Mi Water Purifier v2"),
    POWERPLUG("chuangmi.plug.m1", "Mi Power-plug"),
    POWERPLUG1("chuangmi.plug.v1", "Mi Power-plug v1"),
    POWERPLUG2("chuangmi.plug.v2", "Mi Power-plug v2"),
    POWERSTRIP("qmi.powerstrip.v1", "Mi Power-strip v1"),
    POWERSTRIP2("zimi.powerstrip.v2", "Mi Power-strip v2"),
    GATEWAY1("lumi.gateway.v1", "Mi Smart Home Gateway 1"),
    GATEWAY2("lumi.gateway.v2", "Mi Smart Home Gateway 2"),
    GATEWAY3("lumi.gateway.v3", "Mi Smart Home Gateway 3"),
    YEELIGHT_L1("yeelink.light.lamp1", "Yeelight"),
    YEELIGHT_M1("yeelink.light.mono1", "Yeelight White Bulb"),
    YEELIGHT_M2("yeelink.light.mono2", "Yeelight White Bulb v2"),
    YEELIGHT_C1("yeelink.light.color1", "Yeelight Color Bulb"),
    YEELIGHT_CEIL1("yeelink.light.ceiling1", "Yeelight LED Ceiling Lamp"),
    YEELIGHT_CEIL2("yeelink.light.ceiling2", "Yeelight LED Ceiling Lamp v2"),
    YEELIGHT_CEIL3("yeelink.light.ceiling3", "Yeelight LED Ceiling Lamp v3"),
    YEELIGHT_BS("yeelink.light.bslamp1", "Yeelight Lamp"),
    YEELIGHT_STRIP("yeelink.light.strip1", "Yeelight Strip"),
    TOOTHBRUSH("soocare.toothbrush.x3", "Mi Toothbrush"),
    WIFISPEAKER("xiaomi.wifispeaker.v1", "Mi Internet Speaker"),
    PHILIPSBULB("philips.light.bulb", "Xiaomi Philips Bulb"),
    PHILIPS("philips.light.sread1", "Xiaomi Philips Eyecare Smart Lamp 2"),
    PHILIPS2("philips.light.ceiling", "Xiaomi Philips LED Ceiling Lamp"),
    CHUANGMIIR2("chuangmi.ir.v2", "Mi Remote v2"),
    UNKNOWN("unknown", "Unknown Mi IO Device");

    private final String model;
    private final String description;

    MiIoDevices(String model, String description) {
        this.model = model;
        this.description = description;
    }

    public static MiIoDevices getType(String modelString) {
        for (MiIoDevices mioDev : MiIoDevices.values()) {
            if (mioDev.getModel().equals(modelString)) {
                return mioDev;
            }
        }
        return UNKNOWN;
    }

    public String getModel() {
        return model;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description + " (" + model + ")";
    }
}
