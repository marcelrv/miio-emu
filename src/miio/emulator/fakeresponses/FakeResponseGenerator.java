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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link FakeResponseGenerator} is responsible for fiding fake responses.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class FakeResponseGenerator {
    private final Logger logger = LoggerFactory.getLogger(FakeResponseGenerator.class);

    private String model;
    private String fn;
    private ModelLoader modelData;
    private final JsonParser parser = new JsonParser();

    public FakeResponseGenerator(String model) {
        this.model = model;
        this.fn = model + ".json";
        loadResponses();
        modelData.setModel(model);
    }

    public void loadResponses() {
        logger.info("Load device specific responses for {}", model);
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(String.class, new StringConverter());
        Gson gson = gb.create();

        // Gson gson = new Gson();
        try (JsonReader reader = new JsonReader(new FileReader(fn))) {
            modelData = gson.fromJson(reader, ModelLoader.class);
        } catch (FileNotFoundException e1) {
            logger.info("Mode file not found {}", fn);
        } catch (IOException e) {
            logger.info("Could not read file {}. {}", fn, e.getMessage());
        }
        if (modelData == null) {
            modelData = new ModelLoader();
        }
    }

    public void saveResponses() {
        logger.info("Save device specific responses for {}", model);
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(fn)) {
            gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            gson.toJson(modelData, writer);
        } catch (IOException e) {
            logger.info("Could not write model file {}: {}", fn, e.getMessage());
        }
    }

    public JsonElement getResponse(String command, JsonElement param) {
        JsonElement resp = JsonNull.INSTANCE;
        boolean found = false;
        for (Command cmd : modelData.getCommands()) {
            if (cmd.getCommand().equals(command)) {
                resp = cmd.getResponse();
                found = true;
                break;
            }
        }
        if (resp.isJsonNull()) {
            logger.debug("No Fake response for command {} using default [ok]", command);
            resp = new JsonArray();
            ((JsonArray) resp).add("ok");
        }
        if (!found) {
            logger.debug("Command {} not found... adding", command);
            modelData.getCommands().add(new Command(command, param));
        }
        return resp;
    }

    public JsonElement getPropery(String property) {
        JsonElement resp = JsonNull.INSTANCE;
        boolean found = false;
        for (Property prop : modelData.getProperties()) {
            if (prop.getProperty().equals(property)) {
                resp = prop.getResponse();
                logger.trace("found {} with {}", property, resp.toString());
                // workaround gson empty string handling
                if (resp.isJsonNull()) {
                    resp = new JsonPrimitive("");
                    logger.trace("found {} with '{}' ", property, resp.toString());

                }

                found = true;
                break;
            }
        }
// if (resp.isJsonNull()) {
// Object r = FakeResponses.getCommand(property).getResponse();
// resp = parser.parse(r.toString());
// logger.debug("No model specific response for propert {} using default {}", property, resp.toString());
// }
        if (!found) {
            Object r = FakeResponses.getCommand(property).getResponse();
            if (r instanceof String) {
                resp = new JsonPrimitive((String) r);
            } else {
                resp = parser.parse(r.toString());
            }
            // JsonElement element = gson.fromJson(myData.toString(), JsonElement.class);

            logger.debug("No model specific response for propert {} using default {}", property, resp.toString());
            logger.debug("Property {} not found... adding", property);
            modelData.getProperties().add(new Property(property, resp));
        }
        return resp;
    }

    public static class NullStringToEmptyAdapterFactory<T> implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

            Class<T> rawType = (Class<T>) type.getRawType();
            if (rawType != String.class) {
                return null;
            }
            return (TypeAdapter<T>) new StringAdapter();
        }
    }

    public static class StringAdapter extends TypeAdapter<String> {
        @Override
        public String read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return "";
            }
            return reader.nextString();
        }

        @Override
        public void write(JsonWriter writer, String value) throws IOException {
            if (value == null) {
                writer.nullValue();
                return;
            }
            writer.value(value);
        }
    }
}