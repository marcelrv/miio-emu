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

import com.google.gson.JsonNull;

public enum FakeResponses {

    P1("power", "on"),
    P2("mode", "normal"),
    P3("humidity", 22),
    P4("aqi", 22),
    P5("led", "on"),
    P6("buzzer", "off"),
    P7("f1_hour", 33),
    P8("temp_dec", 21),
    P9("favorite_level", 21),
    P10("bright", 21),
    P11("filter1_life", 21),
    P12("f1_hour_used", 21),
    P13("use_time", 21),
    P14("led", 21),
    P15("led_b", 21),
    // P13("led_b", 21),
    UNKNOWN("", JsonNull.INSTANCE);

    private final String command;
    private final Object response;

    private FakeResponses(String command, Object response) {
        this.command = command;
        this.response = response;
    }

    public String getCommand() {
        return command;
    }

    public static FakeResponses getCommand(String commandString) {
        for (FakeResponses mioCmd : FakeResponses.values()) {
            if (mioCmd.getCommand().equals(commandString)) {
                return mioCmd;
            }
        }
        return UNKNOWN;
    }

    public Object getResponse() {

        return response;
    }

}
