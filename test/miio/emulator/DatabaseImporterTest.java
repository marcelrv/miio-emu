package miio.emulator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonPrimitive;

import miio.emulator.fakeresponses.DatabaseImporter;
import miio.emulator.fakeresponses.ModelLoader;

class DatabaseImporterTest {
    private static final String DB = """
            {"deviceMapping":{"id":["test.dev.v1"],"channels":[
              {"property":"","channel":"actions","type":"String","actions":[{"command":"action","siid":2,"aiid":1}]},
              {"property":"status","siid":2,"piid":1,"channel":"status","type":"Number",
                "stateDescription":{"options":[{"value":"3","label":"x"},{"value":"4","label":"y"}]}},
              {"property":"temp","siid":2,"piid":2,"channel":"temp","type":"Number:Temperature",
                "stateDescription":{"minimum":40,"maximum":200}},
              {"property":"name","siid":2,"piid":3,"channel":"name","type":"String"},
              {"property":"power","siid":2,"piid":4,"channel":"power","type":"Switch"}
            ]}}
            """;

    @AfterEach
    void clearProperty() {
        System.clearProperty(DatabaseImporter.DATABASE_PROPERTY);
    }

    @Test
    void importsMiotModel(@TempDir Path dir) throws IOException {
        Files.write(dir.resolve("test.dev.v1-miot.json"), DB.getBytes(StandardCharsets.UTF_8));
        System.setProperty(DatabaseImporter.DATABASE_PROPERTY, dir.toString());

        ModelLoader model = DatabaseImporter.importModel("test.dev.v1");

        assertNotNull(model);
        assertTrue(model.isIsmiot());
        assertEquals(4, model.getProperties().size());
        assertEquals(new JsonPrimitive(3), model.getProperties().get(0).getResponse());
        assertEquals(new JsonPrimitive(40), model.getProperties().get(1).getResponse());
        assertEquals(new JsonPrimitive(""), model.getProperties().get(2).getResponse());
        assertEquals(new JsonPrimitive(true), model.getProperties().get(3).getResponse());
        assertEquals(2, model.getProperties().get(1).getSiid());
        assertEquals(2, model.getProperties().get(1).getPiid());
        assertEquals(1, model.getActions().size());
    }

    @Test
    void missingModelReturnsNull(@TempDir Path dir) {
        System.setProperty(DatabaseImporter.DATABASE_PROPERTY, dir.toString());
        assertNull(DatabaseImporter.importModel("does.not.exist"));
    }
}
