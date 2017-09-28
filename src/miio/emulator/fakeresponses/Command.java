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
package miio.emulator.fakeresponses;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Command {

    @SerializedName("command")
    @Expose
    private String command;
    @SerializedName("param")
    @Expose
    private JsonElement param;
    @SerializedName("fakeresponse")
    @Expose
    private JsonElement response;

    Command(String command, JsonElement param) {
        this.command = command;
        this.param = param;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public JsonElement getParameter() {
        return param;
    }

    public void setParameter(JsonElement parameter) {
        this.param = parameter;
    }

    public JsonElement getResponse() {
        return response != null ? response : JsonNull.INSTANCE;
    }

    public void setResponse(JsonElement response) {
        this.response = response != null ? response : JsonNull.INSTANCE;
    }

}
