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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelLoader {

    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("ismiot")
    @Expose
    private boolean ismiot = false;
    @SerializedName("commands")
    @Expose
    private List<Command> commands = new ArrayList<Command>();
    @SerializedName("properties")
    @Expose
    private List<Property> properties = new ArrayList<Property>();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public boolean isIsmiot() {
        return ismiot;
    }

    public void setIsmiot(boolean ismiot) {
        this.ismiot = ismiot;
    }

}
