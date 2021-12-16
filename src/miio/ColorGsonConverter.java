package miio;

import java.awt.Color;
import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@NonNullByDefault
public class ColorGsonConverter implements JsonSerializer<Color>, JsonDeserializer<Color> {

    @Override
    public Color deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            throw new JsonParseException("missing json text");
        }
        JsonObject colorSave = json.getAsJsonObject();
        Color color = new Color(colorSave.get("red").getAsInt(), colorSave.get("green").getAsInt(),
                colorSave.get("blue").getAsInt(), colorSave.get("alpha").getAsInt());
        return color;
    }

    @Override
    public JsonElement serialize(Color src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context) {
        JsonObject colorSave = new JsonObject();
        colorSave.addProperty("red", src.getRed());
        colorSave.addProperty("green", src.getGreen());
        colorSave.addProperty("blue", src.getBlue());
        colorSave.addProperty("alpha", src.getAlpha());
        return colorSave;
    }
}
