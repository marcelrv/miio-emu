package miio.emulator.fakeresponses;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Builds initial fake responses for a model from the openHAB miio binding device database file
 * (&lt;model&gt;-miot.json or &lt;model&gt;.json), so a new device can be emulated without hand writing the model
 * file. The database folder is taken from the system property 'miio.database'.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class DatabaseImporter {
    public static final String DATABASE_PROPERTY = "miio.database";
    private static final String DEFAULT_DATABASE = "../../oh4/openhab-main/git/openhab-addons/bundles/org.openhab.binding.miio/src/main/resources/database";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseImporter.class);

    private DatabaseImporter() {
    }

    /**
     * @return the model data build from the binding database or null if the model can not be found there
     */
    public static ModelLoader importModel(String model) {
        Path dir = Paths.get(System.getProperty(DATABASE_PROPERTY, DEFAULT_DATABASE));
        for (String suffix : new String[] { "-miot.json", ".json" }) {
            Path file = dir.resolve(model + suffix);
            if (Files.isRegularFile(file)) {
                try {
                    logger.info("Building responses for {} from database file {}", model, file.toAbsolutePath());
                    return importFile(model, file);
                } catch (IOException | RuntimeException e) {
                    logger.warn("Could not import {}: {}", file, e.getMessage());
                }
            }
        }
        logger.info("No database file for {} found in {} (set -D{}=<dir> to point to it)", model, dir.toAbsolutePath(),
                DATABASE_PROPERTY);
        return null;
    }

    private static ModelLoader importFile(String model, Path file) throws IOException {
        JsonObject mapping = JsonParser.parseString(new String(Files.readAllBytes(file), StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonObject("deviceMapping");
        ModelLoader data = new ModelLoader();
        data.setModel(model);
        boolean miot = false;
        List<Action> actions = new ArrayList<>();
        for (JsonElement channelElement : mapping.getAsJsonArray("channels")) {
            JsonObject channel = channelElement.getAsJsonObject();
            Integer siid = intOrNull(channel, "siid");
            Integer piid = intOrNull(channel, "piid");
            String property = channel.has("property") ? channel.get("property").getAsString() : "";
            if (channel.has("actions")) {
                for (JsonElement actionElement : channel.getAsJsonArray("actions")) {
                    JsonObject action = actionElement.getAsJsonObject();
                    Integer aiid = intOrNull(action, "aiid");
                    Integer actionSiid = intOrNull(action, "siid");
                    if (aiid != null && actionSiid != null) {
                        miot = true;
                        actions.add(new Action(actionSiid, aiid));
                    }
                }
            }
            if (property.isEmpty()) {
                continue;
            }
            miot |= siid != null && piid != null;
            data.getProperties().add(new Property(property, defaultValue(channel), siid, piid));
        }
        data.setIsmiot(miot);
        data.getActions().addAll(actions);
        return data;
    }

    private static Integer intOrNull(JsonObject json, String name) {
        return json.has(name) && !json.get(name).isJsonNull() ? json.get(name).getAsInt() : null;
    }

    private static JsonElement defaultValue(JsonObject channel) {
        String type = channel.has("type") ? channel.get("type").getAsString().toLowerCase() : "string";
        boolean number = type.startsWith("number") || type.equals("dimmer");
        JsonObject state = channel.has("stateDescription") ? channel.getAsJsonObject("stateDescription") : null;
        if (state != null) {
            JsonArray options = state.has("options") ? state.getAsJsonArray("options") : null;
            if (options != null && options.size() > 0) {
                String value = options.get(0).getAsJsonObject().get("value").getAsString();
                return number ? new JsonPrimitive(Double.valueOf(value).intValue()) : new JsonPrimitive(value);
            }
            if (number && state.has("minimum")) {
                return state.get("minimum");
            }
        }
        if (type.equals("switch")) {
            return new JsonPrimitive(true);
        }
        if (number) {
            return new JsonPrimitive(0);
        }
        return type.equals("string") ? new JsonPrimitive("") : JsonNull.INSTANCE;
    }
}
