package miio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class jsonTest {

    public static void main(String[] args) {
        // JsonParser parser= new JsonParser();

        String js = "{\"test\": \"123\"}";
        JsonObject json = JsonParser.parseString(js).getAsJsonObject();

        System.out.println(json.get("test").getAsString().matches("^[0-9]+$"));

        System.out.println(json.get("test").getAsString());
        System.out.println(json.get("test").getAsInt());
        System.out.println(json.get("test").getAsJsonPrimitive().isNumber());

        System.out.println(json.get("test").getAsJsonPrimitive().isString());

        System.out.println(json.get("test").getAsString().matches("^[0-9]+$"));

        System.out.println(json.get("test").getAsString());
        System.out.println(json.get("test").getAsInt());
        System.out.println(json.get("test").getAsJsonPrimitive().isNumber());
    }

}
