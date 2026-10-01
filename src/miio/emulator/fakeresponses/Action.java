package miio.emulator.fakeresponses;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A MIoT action (siid/aiid). When invoked the emulator answers with code 0 and the 'out' values and applies the
 * 'sets' to the stored property values, so a simple state change (e.g. start cooking -> status 4) can be emulated.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class Action {

    public static class PropertyChange {
        @SerializedName("siid")
        @Expose
        private int siid;
        @SerializedName("piid")
        @Expose
        private int piid;
        @SerializedName("value")
        @Expose
        private JsonElement value;

        public PropertyChange(int siid, int piid, JsonElement value) {
            this.siid = siid;
            this.piid = piid;
            this.value = value;
        }

        public int getSiid() {
            return siid;
        }

        public int getPiid() {
            return piid;
        }

        public JsonElement getValue() {
            return value;
        }
    }

    @SerializedName("siid")
    @Expose
    private int siid;
    @SerializedName("aiid")
    @Expose
    private int aiid;
    @SerializedName("out")
    @Expose
    private JsonArray out;
    @SerializedName("sets")
    @Expose
    private List<PropertyChange> sets = new ArrayList<>();

    public Action(int siid, int aiid) {
        this.siid = siid;
        this.aiid = aiid;
    }

    public boolean matches(int siid, int aiid) {
        return this.siid == siid && this.aiid == aiid;
    }

    public JsonArray getOut() {
        return out != null ? out : new JsonArray();
    }

    public List<PropertyChange> getSets() {
        return sets != null ? sets : new ArrayList<>();
    }
}
