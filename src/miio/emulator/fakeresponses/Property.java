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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Property {

    @SerializedName("property")
    @Expose
    private String property;
    @SerializedName("fakeresponse")
    @Expose
    private JsonElement response;

    @SerializedName("datatype")
    @Expose
    private String datatype;

    // MIoT service / property id. When present the property is also found by siid/piid
    // (the binding sends the channel name in 'did' for writes and the property name for reads)
    @SerializedName("siid")
    @Expose
    private Integer siid;
    @SerializedName("piid")
    @Expose
    private Integer piid;

    public Property(String property, JsonElement response) {
        this.property = property;
        this.response = response;
    }

    public Property(String property, JsonElement response, Integer siid, Integer piid) {
        this(property, response);
        this.siid = siid;
        this.piid = piid;
    }

    public boolean matches(Integer siid, Integer piid) {
        return siid != null && piid != null && siid.equals(this.siid) && piid.equals(this.piid);
    }

    public void setSiidPiid(Integer siid, Integer piid) {
        this.siid = siid;
        this.piid = piid;
    }

    public Integer getSiid() {
        return siid;
    }

    public Integer getPiid() {
        return piid;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public JsonElement getResponse() {
        return response;
    }

    public void setResponse(JsonElement response) {
        this.response = response;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
