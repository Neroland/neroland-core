package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;

/** One round trip per trigger type and nero_event kind, plus the rejections decision-time rules require. */
class TrophyTriggerCodecTest {

    private static DataResult<TrophyTrigger> decode(String json) {
        return TrophyTrigger.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    private static TrophyTrigger roundTrip(String json) {
        TrophyTrigger decoded = decode(json).result().orElseThrow();
        JsonElement encoded = TrophyTrigger.CODEC.encodeStart(JsonOps.INSTANCE, decoded).result().orElseThrow();
        assertEquals(decoded, TrophyTrigger.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow());
        return decoded;
    }

    private static Identifier id(String id) {
        return Identifier.parse(id);
    }

    @Test
    void advancement() {
        assertEquals(new TrophyTrigger.Advancement(id("nerospace:worlds/mars/arrive")),
                roundTrip("{\"type\":\"advancement\",\"advancement\":\"nerospace:worlds/mars/arrive\"}"));
    }

    @Test
    void neroEventGate() {
        TrophyTrigger trigger = roundTrip("{\"type\":\"nero_event\",\"event\":\"gate\",\"gate\":\"nerotech:fusion\"}");
        assertEquals(TrophyTrigger.NeroEvent.ofGate(id("nerotech:fusion")), trigger);
        assertEquals(Optional.of(id("nerotech:fusion")), ((TrophyTrigger.NeroEvent) trigger).gate());
    }

    @Test
    void neroEventMaterialMilestoneWithAndWithoutMaterial() {
        assertEquals(TrophyTrigger.NeroEvent.ofMaterialMilestone(id("nerolandcore:material_discovered"),
                        Optional.of(id("nerospace:mars_iron"))),
                roundTrip("{\"type\":\"nero_event\",\"event\":\"material_milestone\","
                        + "\"milestone\":\"nerolandcore:material_discovered\",\"material\":\"nerospace:mars_iron\"}"));
        assertEquals(TrophyTrigger.NeroEvent.ofMaterialMilestone(id("nerolandcore:material_discovered"), Optional.empty()),
                roundTrip("{\"type\":\"nero_event\",\"event\":\"material_milestone\","
                        + "\"milestone\":\"nerolandcore:material_discovered\"}"));
    }

    @Test
    void neroEventReputationAcceptsNegativeValues() {
        assertEquals(TrophyTrigger.NeroEvent.ofReputation(id("nerofactions:raiders"), -500),
                roundTrip("{\"type\":\"nero_event\",\"event\":\"reputation\",\"faction\":\"nerofactions:raiders\","
                        + "\"at_least\":-500}"));
    }

    @Test
    void neroEventCurrencyDefaultsToCredits() {
        assertEquals(TrophyTrigger.NeroEvent.ofCurrency(TrophyTrigger.NeroEvent.DEFAULT_CURRENCY, 1000),
                roundTrip("{\"type\":\"nero_event\",\"event\":\"currency\",\"at_least\":1000}"));
        assertTrue(decode("{\"type\":\"nero_event\",\"event\":\"currency\",\"at_least\":0}").error().isPresent());
    }

    @Test
    void stat() {
        assertEquals(new TrophyTrigger.Stat(TrophyTrigger.Stat.DEFAULT_STAT_TYPE, id("minecraft:jump"), 100),
                roundTrip("{\"type\":\"stat\",\"stat\":\"minecraft:jump\",\"at_least\":100}"));
        assertEquals(new TrophyTrigger.Stat(id("minecraft:killed"), id("nerocreatures:cinder_tyrant"), 1),
                roundTrip("{\"type\":\"stat\",\"stat_type\":\"minecraft:killed\","
                        + "\"stat\":\"nerocreatures:cinder_tyrant\",\"at_least\":1}"));
    }

    @Test
    void itemObtainedDefaultsCountToOne() {
        assertEquals(new TrophyTrigger.ItemObtained(id("minecraft:diamond"), 1),
                roundTrip("{\"type\":\"item_obtained\",\"item\":\"minecraft:diamond\"}"));
        assertEquals(new TrophyTrigger.ItemObtained(id("minecraft:diamond"), 64),
                roundTrip("{\"type\":\"item_obtained\",\"item\":\"minecraft:diamond\",\"count\":64}"));
    }

    @Test
    void blockPlaced() {
        assertEquals(new TrophyTrigger.BlockPlaced(id("nerodecor:hull_panel"), 500),
                roundTrip("{\"type\":\"block_placed\",\"block\":\"nerodecor:hull_panel\",\"count\":500}"));
    }

    @Test
    void dimensionVisited() {
        assertEquals(new TrophyTrigger.DimensionVisited(id("minecraft:the_nether")),
                roundTrip("{\"type\":\"dimension_visited\",\"dimension\":\"minecraft:the_nether\"}"));
    }

    @Test
    void absentModIdsDecodeBecauseTargetsAreNeverCheckedAgainstARegistry() {
        for (String json : List.of(
                "{\"type\":\"advancement\",\"advancement\":\"notinstalled:some/advancement\"}",
                "{\"type\":\"nero_event\",\"event\":\"gate\",\"gate\":\"notinstalled:gate\"}",
                "{\"type\":\"stat\",\"stat_type\":\"notinstalled:type\",\"stat\":\"notinstalled:stat\",\"at_least\":3}",
                "{\"type\":\"item_obtained\",\"item\":\"notinstalled:item\"}",
                "{\"type\":\"block_placed\",\"block\":\"notinstalled:block\"}",
                "{\"type\":\"dimension_visited\",\"dimension\":\"notinstalled:world\"}")) {
            assertTrue(decode(json).result().isPresent(), json);
        }
    }

    @Test
    void unknownTypeAndReservedThresholdAreRejected() {
        assertTrue(decode("{\"type\":\"kill_count\",\"entity\":\"minecraft:zombie\"}").error().isPresent());
        assertTrue(decode("{\"type\":\"nero_event\",\"event\":\"threshold\",\"channel\":\"nerotech:power\"}").error().isPresent());
    }

    @Test
    void numbersAreBoundedAndWhole() {
        assertTrue(decode("{\"type\":\"stat\",\"stat\":\"minecraft:jump\",\"at_least\":0}").error().isPresent());
        assertTrue(decode("{\"type\":\"stat\",\"stat\":\"minecraft:jump\",\"at_least\":2147483648}").error().isPresent());
        assertTrue(decode("{\"type\":\"stat\",\"stat\":\"minecraft:jump\",\"at_least\":1.5}").error().isPresent());
        assertTrue(decode("{\"type\":\"item_obtained\",\"item\":\"minecraft:dirt\",\"count\":-1}").error().isPresent());
    }

    @Test
    void overLongIdentifiersAreRejected() {
        String longPath = "a".repeat(130);
        assertTrue(decode("{\"type\":\"advancement\",\"advancement\":\"x:" + longPath + "\"}").error().isPresent());
    }

    @Test
    void tierCodecAndRanks() {
        assertEquals(TrophyTier.NERO, TrophyTier.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"nero\""))
                .result().orElseThrow());
        assertTrue(TrophyTier.BRONZE.rank() < TrophyTier.SILVER.rank());
        assertTrue(TrophyTier.GOLD.rank() < TrophyTier.NERO.rank());
    }
}
