package za.co.neroland.nerolandcore.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PlayerDataExportTest {

    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @AfterEach
    void reset() {
        PlayerDataExport.clearForTests();
    }

    @Test
    void documentHasSectionsPerModAndIsolatesFailures() {
        PlayerDataExport.register("zeta", (server, uuid) -> new JsonPrimitive(uuid.equals(PLAYER) ? "mine" : "other"));
        PlayerDataExport.register("alpha", (server, uuid) -> null);
        PlayerDataExport.register("broken", (server, uuid) -> {
            throw new IllegalStateException("x");
        });
        JsonObject doc = PlayerDataExport.assemble(null, PLAYER, Instant.EPOCH, uuid -> true);
        assertEquals("nerolandcore-data-export", doc.get("format").getAsString());
        assertEquals(PlayerDataExport.SCHEMA, doc.get("schema").getAsInt());
        assertEquals("1970-01-01T00:00:00Z", doc.get("generatedAt").getAsString());
        assertTrue(doc.get("erased").getAsBoolean());
        JsonObject mods = doc.getAsJsonObject("mods");
        assertEquals("mine", mods.get("zeta").getAsString());
        assertFalse(mods.has("alpha"));
        assertFalse(mods.has("broken"));
        assertEquals("alpha", doc.getAsJsonArray("nothingStored").get(0).getAsString());
        assertEquals("broken", doc.getAsJsonArray("failed").get(0).getAsString());
    }

    @Test
    void tombstoneFailureReadsAsNotErased() {
        JsonObject doc = PlayerDataExport.assemble(null, PLAYER, Instant.EPOCH, uuid -> {
            throw new IllegalStateException("no store");
        });
        assertFalse(doc.get("erased").getAsBoolean());
        assertEquals(0, doc.getAsJsonObject("mods").size());
    }
}
