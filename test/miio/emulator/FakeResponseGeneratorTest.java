package miio.emulator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import miio.emulator.fakeresponses.FakeResponseGenerator;

/**
 * Tests the MIoT responses of the careli.fryer.maf02 model file (loaded from the working directory).
 */
class FakeResponseGeneratorTest {
    private static final String MODEL = "careli.fryer.maf02";

    @Test
    void loadsModelProperties() {
        FakeResponseGenerator gen = new FakeResponseGenerator(MODEL);
        assertEquals(new JsonPrimitive(1), gen.getProperty("status", 2, 1));
        assertEquals(new JsonPrimitive(180), gen.getProperty("target-temperature", 2, 4));
    }

    @Test
    void propertyIsFoundBySiidPiidWhenDidIsChannelName() {
        FakeResponseGenerator gen = new FakeResponseGenerator(MODEL);
        // the binding reads with the property name and writes with the channel name as did
        assertTrue(gen.setProperty("target_temperature", 2, 4, new JsonPrimitive(160)));
        assertEquals(new JsonPrimitive(160), gen.getProperty("target-temperature", 2, 4));
    }

    @Test
    void actionChangesProperties() {
        FakeResponseGenerator gen = new FakeResponseGenerator(MODEL);
        assertEquals(new JsonArray(), gen.performAction(2, 1));
        assertEquals(new JsonPrimitive(4), gen.getProperty("status", 2, 1));
        assertEquals(new JsonPrimitive(15), gen.getProperty("left-time", 2, 5));
        gen.performAction(2, 2);
        assertEquals(new JsonPrimitive(1), gen.getProperty("status", 2, 1));
        assertEquals(new JsonPrimitive(0), gen.getProperty("left-time", 2, 5));
    }

    @Test
    void unknownActionAndPropertyGetDefaults() {
        FakeResponseGenerator gen = new FakeResponseGenerator(MODEL);
        assertEquals(new JsonArray(), gen.performAction(99, 99));
        assertEquals(new JsonPrimitive(""), gen.getProperty("unknown-property", 99, 98));
    }
}
