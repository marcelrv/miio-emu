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

    AIRCONDITION_A1("aux.aircondition.v1", "AUX Air Conditioner"),
    AIRCONDITION_I1("idelan.aircondition.v1", "Idelan Air Conditioner"),
    AIRCONDITION_M1("midea.aircondition.v1", "Midea Air Conditioner v2"),
    AIRCONDITION_M2("midea.aircondition.v2", "Midea Air Conditioner v2"),
    AIRCONDITION_MXA1("midea.aircondition.xa1", "Midea Air Conditioner xa1"),
    AIRMONITOR1("zhimi.airmonitor.v1", "Mi Air Monitor v1"),
    AIRMONITOR_B1("cgllc.airmonitor.b1", "Mi Air Quality Monitor 2gen"),
    AIRMONITOR_S1("cgllc.airmonitor.s1", "Mi Air Quality Monitor S1"),
    AIR_HUMIDIFIER_V1("zhimi.humidifier.v1", "Mi Air Humidifier"),
    AIR_HUMIDIFIER_CA1("zhimi.humidifier.ca1", "Mi Air Humidifier"),
    AIR_HUMIDIFIER_CB1("zhimi.humidifier.cb1", "Mi Air Humidifier 2"),
    AIR_HUMIDIFIER_MJJSQ("deerma.humidifier.mjjsq", "Mija Smart humidifier"),
    AIR_PURIFIER1("zhimi.airpurifier.v1", "Mi Air Purifier v1"),
    AIR_PURIFIER2("zhimi.airpurifier.v2", "Mi Air Purifier v2"),
    AIR_PURIFIER3("zhimi.airpurifier.v3", "Mi Air Purifier v3"),
    AIR_PURIFIER5("zhimi.airpurifier.v5", "Mi Air Purifier v5"),
    AIR_PURIFIER6("zhimi.airpurifier.v6", "Mi Air Purifier Pro v6"),
    AIR_PURIFIER7("zhimi.airpurifier.v7", "Mi Air Purifier Pro v7"),
    AIR_PURIFIERM("zhimi.airpurifier.m1", "Mi Air Purifier 2 (mini)"),
    AIR_PURIFIERM2("zhimi.airpurifier.m2", "Mi Air Purifier (mini)"),
    AIR_PURIFIERMA1("zhimi.airpurifier.ma1", "Mi Air Purifier MS1"),
    AIR_PURIFIERMA2("zhimi.airpurifier.ma2", "Mi Air Purifier MS2"),
    AIR_PURIFIERMA4("zhimi.airpurifier.ma4", "Mi Air Purifier 3"),
    AIR_PURIFIERMB3("zhimi.airpurifier.mb3", "Mi Air Purifier 3"),
    AIR_PURIFIERSA1("zhimi.airpurifier.sa1", "Mi Air Purifier Super"),
    AIR_PURIFIERSA2("zhimi.airpurifier.sa2", "Mi Air Purifier Super 2"),
    AIRFRESH_T2017("dmaker.airfresh.t2017", "Mi Fresh Air Ventilator"),
    AIRFRESH_A1("dmaker.airfresh.a1", "Mi Fresh Air Ventilator A1"),
    ALARM_CLOCK_MYK01("zimi.clock.myk01", "Xiao AI Smart Alarm Clock"),
    BATHHEATER_V2("yeelight.bhf_light.v2", "Yeelight Smart Bath Heater"),
    DEHUMIDIFIER_FW1("nwt.derh.wdh318efw1", "XIAOMI MIJIA WIDETECH WDH318EFW1 Dehumidifier"),
    ZHIMI_AIRPURIFIER_MB1("zhimi.airpurifier.mb1", "Mi Air Purifier mb1"),
    ZHIMI_AIRPURIFIER_MC1("zhimi.airpurifier.mc1", "Mi Air Purifier 2S"),
    ZHIMI_AIRPURIFIER_VIRTUAL("zhimi.airpurifier.virtual", "Mi Air Purifier virtual"),
    ZHIMI_AIRPURIFIER_VTL_M1("zhimi.airpurifier.vtl_m1", "Mi Air Purifier vtl m1"),
    CHUANGMI_IR2("chuangmi.ir.v2", "Mi Remote v2"),
    CHUANGMI_V2("chuangmi.remote.v2", "Xiaomi IR Remote"),
    COOKER1("chunmi.cooker.normal1", "MiJia Rice Cooker"),
    COOKER2("chunmi.cooker.normal2", "MiJia Rice Cooker"),
    COOKER3("hunmi.cooker.normal3", "MiJia Rice Cooker"),
    COOKER4("chunmi.cooker.normal4", "MiJia Rice Cooker"),
    COOKER_P1("chunmi.cooker.press1", "MiJia Heating Pressure Rice Cooker"),
    COOKER_P2("chunmi.cooker.press2", "MiJia Heating Pressure Rice Cooker"),
    FAN1("zhimi.fan.v1", "Mi Smart Fan"),
    FAN2("zhimi.fan.v2", "Mi Smart Fan"),
    FAN3("zhimi.fan.v3", "Mi Smart Pedestal Fan"),
    FAN_SA1("zhimi.fan.sa1", "Xiaomi Mi Smart Pedestal Fan"),
    FAN_ZA1("zhimi.fan.za1", "Xiaomi Mi Smart Pedestal Fan"),
    FRIDGE_V3("viomi.fridge.v3", "Viomi Internet refrigerator iLive"),
    GATEWAY1("lumi.gateway.v1", "Mi Smart Home Gateway v1"),
    GATEWAY2("lumi.gateway.v2", "Mi Smart Home Gateway v2"),
    GATEWAY3("lumi.gateway.v3", "Mi Smart Home Gateway v3"),
    HUMIDIFIER("zhimi.humidifier.v1", "Mi Humdifier"),
    LUMI_C11("lumi.ctrl_neutral1.v1", "Light Control (Wall Switch)"),
    LUMI_C12("lumi.ctrl_neutral2.v1", "Light Control (Wall Switch)"),
    PHILIPS_R1("philips.light.sread1", "Xiaomi Philips Eyecare Smart Lamp 2"),
    PHILIPS_C("philips.light.ceiling", "Xiaomi Philips LED Ceiling Lamp"),
    PHILIPS_C2("philips.light.zyceiling", "Xiaomi Philips LED Ceiling Lamp"),
    PHILIPS_BULB("philips.light.bulb", "Xiaomi Philips Bulb"),
    PHILIPS_CANDLE("philips.light.candle", "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp"),
    PHILIPS_DOWN("philips.light.downlight", "Xiaomi Philips Downlight"),
    PHILIPS_MOON("philips.light.moonlight", "Xiaomi Philips ZhiRui bedside lamp"),
    PHILIPS_LIGHT_CANDLE2("philips.light.candle2",
            "Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal"),
    PHILIPS_LIGHT_MONO1("philips.light.mono1", "philips.light.mono1"),
    PHILIPS_LIGHT_VIRTUAL("philips.light.virtual", "philips.light.virtual"),
    PHILIPS_LIGHT_ZYSREAD("philips.light.zysread", "philips.light.zysread"),
    PHILIPS_LIGHT_ZYSTRIP("philips.light.zystrip", "philips.light.zystrip"),
    POWERPLUG("chuangmi.plug.m1", "Mi Power-plug"),
    POWERPLUG1("chuangmi.plug.v1", "Mi Power-plug v1"),
    POWERPLUG2("chuangmi.plug.v2", "Mi Power-plug v2"),
    POWERPLUG3("chuangmi.plug.v3", "Mi Power-plug v3"),
    POWERPLUGM3("chuangmi.plug.m3", "Mi Power-plug"),
    POWERPLUG_HMI205("chuangmi.plug.hmi205", "Mi Smart Plug"),
    POWERSTRIP("qmi.powerstrip.v1", "Qing Mi Smart Power Strip v1"),
    POWERSTRIP2("zimi.powerstrip.v2", "Mi Power-strip v2"),
    TOOTHBRUSH("soocare.toothbrush.x3", "Mi Toothbrush"),
    VACUUM("rockrobo.vacuum.v1", "Mi Robot Vacuum"),
    VACUUM_C1("roborock.vacuum.c1", "Mi Xiaowa Vacuum c1"),
    VACUUM2("roborock.vacuum.s5", "Mi Robot Vacuum v2"),
    VACUUM1S("roborock.vacuum.m1s", "Mi Robot Vacuum 1S"),
    VACUUMS4("roborock.vacuum.s4", "Mi Robot Vacuum S4"),
    VACUUMSTS4V2("roborock.vacuum.s4v2", "Roborock Vacuum S4v2"),
    VACUUMST6("roborock.vacuum.t6", "Roborock Vacuum T6"),
    VACUUMST6V2("roborock.vacuum.t6v2", "Roborock Vacuum T6 v2"),
    VACUUMST6V3("roborock.vacuum.t6v3", "Roborock Vacuum T6 v3"),
    VACUUMST4("roborock.vacuum.t4", "Roborock Vacuum T4"),
    VACUUMST4V2("roborock.vacuum.t4v2", "Roborock Vacuum T4 v2"),
    VACUUMST4V3("roborock.vacuum.t4v3", "Roborock Vacuum T4 v3"),
    VACUUMST7("roborock.vacuum.t7", "Roborock Vacuum T7"),
    VACUUMST7V2("roborock.vacuum.t7v2", "Roborock Vacuum T7 v2"),
    VACUUMST7V3("roborock.vacuum.t7v3", "Roborock Vacuum T7 v3"),
    VACUUMST7P("roborock.vacuum.t7p", "Roborock Vacuum T7p"),
    VACUUMST7PV2("roborock.vacuum.t7pv2", "Roborock Vacuum T7 v2"),
    VACUUMST7PV3("roborock.vacuum.t7pv3", "Roborock Vacuum T7 v3"),
    VACUUMS5MAX("roborock.vacuum.s5e", "Roborock Vacuum S5 Max"),
    VACUUMSS6("rockrobo.vacuum.s6", "Roborock Vacuum S6"),
    VACUUME2("roborock.vacuum.e2", "Rockrobo Xiaowa Vacuum v2"),
    VACUUME_V6("viomi.vacuum.v6", "Xiaomi Mijia vacuum V-RVCLM21B"),
    VACUUME_V7("viomi.vacuum.v7", "Xiaomi Mijia vacuum STYJ02YM"),
    ROBOROCK_VACUUM_C1("roborock.vacuum.c1", "roborock.vacuum.c1"),
    SWEEPER2("roborock.sweeper.e2v2", "Rockrobo Xiaowa Sweeper v2"),
    SWEEPER3("roborock.sweeper.e2v3", "Rockrobo Xiaowa Sweeper v3"),
    SWITCH01("090615.switch.xswitch01", " Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch"),
    SWITCH02("090615.switch.xswitch02", " Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch"),
    SWITCH03("090615.switch.xswitch03", " Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch"),
    WATER_PURIFIER2("yunmi.waterpuri.v2", "Mi Water Purifier v2"),
    WATER_PURIFIERLX2("yunmi.waterpuri.lx2", "Mi Water Purifier lx2"),
    WATER_PURIFIERLX3("yunmi.waterpuri.lx3", "Mi Water Purifier lx3"),
    WATER_PURIFIERLX4("yunmi.waterpuri.lx4", "Mi Water Purifier lx4"),
    WATER_PURIFIER("yunmi.waterpurifier.v2", "Mi Water Purifier v2"),
    WATER_PURIFIER3("yunmi.waterpurifier.v3", "Mi Water Purifier v3"),
    WATER_PURIFIER4("yunmi.waterpurifier.v4", "Mi Water Purifier v4"),
    WIFI2("xiaomi.repeater.v2", "Xiaomi Wifi Extender"),
    WIFISPEAKER("xiaomi.wifispeaker.v1", "Mi Internet Speaker"),
    YEELIGHT_BSLAMP("yeelink.light.bslamp1", "Yeelight Lamp"),
    YEELIGHT_BSLAMP2("yeelink.light.bslamp2", "Yeelight Lamp"),
    YEELIGHT_CEIL1("yeelink.light.ceiling1", "Yeelight LED Ceiling Lamp"),
    YEELIGHT_CEIL2("yeelink.light.ceiling2", "Yeelight LED Ceiling Lamp v2"),
    YEELIGHT_CEIL3("yeelink.light.ceiling3", "Yeelight LED Ceiling Lamp v3"),
    YEELIGHT_CEIL4("yeelink.light.ceiling4", "Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB)"),
    YEELIGHT_CEIL4A("yeelink.light.ceiling4.ambi", "Yeelight LED Ceiling Lamp v4"),
    YEELIGHT_CEIL5("yeelink.light.ceiling5", "Yeelight LED Ceiling Lamp v5"),
    YEELIGHT_CEIL6("yeelink.light.ceiling6", "Yeelight LED Ceiling Lamp v6"),
    YEELIGHT_CEIL7("yeelink.light.ceiling7", "Yeelight LED Ceiling Lamp v7"),
    YEELIGHT_CEIL8("yeelink.light.ceiling8", "Yeelight LED Ceiling Lamp v8"),
    YEELIGHT_CEIL9("yeelink.light.ceiling9", "Yeelight LED Ceiling Lamp v9"),
    YEELIGHT_CEIL10("yeelink.light.ceiling10", "Yeelight LED Meteorite lamp"),
    YEELIGHT_CEIL11("yeelink.light.ceiling11", "Yeelight LED Ceiling Lamp v11"),
    YEELIGHT_CEIL12("yeelink.light.ceiling12", "Yeelight LED Ceiling Lamp v12"),
    YEELIGHT_CEIL13("yeelink.light.ceiling13", "Yeelight LED Ceiling Lamp v13"),
    YEELIGHT_CT2("yeelink.light.ct2", "Yeelight ct2"),
    YEELIGHT_DOLPHIN("yeelink.light.mono1", "Yeelight White Bulb"),
    YEELIGHT_DOLPHIN2("yeelink.light.mono2", "Yeelight White Bulb v2"),
    YEELIGHT_DONUT("yeelink.wifispeaker.v1", "Yeelight Wifi Speaker"),
    YEELIGHT_MANGO("yeelink.light.lamp1", "Yeelight"),
    YEELIGHT_MANGO2("yeelink.light.lamp2", "Yeelight"),
    YEELIGHT_MANGO3("yeelink.light.lamp3", "Yeelight"),
    YEELIGHT_STRIP("yeelink.light.strip1", "Yeelight Strip"),
    YEELIGHT_STRIP2("yeelink.light.strip2", "Yeelight Strip"),
    YEELIGHT_VIRT("yeelink.light.virtual", "Yeelight"),
    YEELIGHT_C1("yeelink.light.color1", "Yeelight Color Bulb"),
    YEELIGHT_C2("yeelink.light.color2", "Yeelight Color Bulb YLDP06YL 10W"),
    YEELIGHT_C3("yeelink.light.color3", "Yeelight Color Bulb YLDP02YL 9W"),
    YEELIGHT_C4("yeelink.light.color4", "Yeelight Bulb YLDP13YL (8,5W)"),
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
