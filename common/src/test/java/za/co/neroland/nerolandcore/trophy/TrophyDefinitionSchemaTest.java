package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.id;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

/** The definition and collection file schema: defaults, optional trigger, mod lists, forward compatibility. */
class TrophyDefinitionSchemaTest {

    private static DataResult<TrophyDefinition.Data> decode(String json) {
        return TrophyDefinition.DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    }

    @Test
    void defaultsAreFilledFromTheId() {
        TrophyDefinition definition = TrophyDefinition.of(id("nerotrophies:nerospace/first_launch"),
                decode(TrophyTestData.trophyJson("minecraft:firework_rocket", "")).result().orElseThrow());
        assertEquals("trophy.nerotrophies.nerospace.first_launch.title", definition.title());
        assertEquals("trophy.nerotrophies.nerospace.first_launch.desc", definition.description());
        assertEquals("nerotrophies", definition.bookCategory());
        assertEquals(1, definition.schema());
        assertEquals(0, definition.order());
        assertEquals(Optional.empty(), definition.reward());
        assertTrue(!definition.hidden());
    }

    @Test
    void fullDefinitionRoundTrips() {
        TrophyDefinition.Data data = decode("{\"schema\":1,\"icon\":\"nerospace:rocket_tier_1\",\"tier\":\"gold\","
                + "\"trigger\":{\"type\":\"advancement\",\"advancement\":\"nerospace:worlds/mars/arrive\"},"
                + "\"hidden\":true,\"title\":\"trophy.x.mars.title\",\"description\":\"trophy.x.mars.desc\","
                + "\"requires_mod\":\"nerospace\",\"category\":\"space\",\"reward\":{\"coins\":50},\"order\":7}")
                .result().orElseThrow();
        TrophyDefinition definition = TrophyDefinition.of(id("x:mars"), data);
        assertEquals(List.of("nerospace"), definition.requiresMods());
        assertEquals("space", definition.bookCategory());
        assertEquals(Optional.of(new TrophyReward(50)), definition.reward());
        assertEquals(7, definition.order());
        var encoded = TrophyDefinition.DATA_CODEC.encodeStart(JsonOps.INSTANCE, definition.toData()).result().orElseThrow();
        assertEquals(definition, TrophyDefinition.of(id("x:mars"),
                TrophyDefinition.DATA_CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow()));
    }

    @Test
    void triggerIsOptionalForAwardOnlyTrophies() {
        TrophyDefinition definition = TrophyDefinition.of(id("x:gift"),
                decode("{\"icon\":\"minecraft:cake\",\"tier\":\"silver\"}").result().orElseThrow());
        assertTrue(definition.awardOnly());
        assertTrue(decode("{\"icon\":\"minecraft:cake\",\"tier\":\"silver\",\"trigger\":{\"type\":\"nope\"}}")
                .error().isPresent(), "a present but broken trigger is still malformed");
    }

    @Test
    void requiresModAcceptsAStringOrAList() {
        assertEquals(List.of("neroquests", "nerocompanion"), decode(TrophyTestData.trophyJson("minecraft:cake",
                "\"requires_mod\":[\"neroquests\",\"nerocompanion\"]")).result().orElseThrow().requiresMods());
        assertEquals(List.of("neroquests"), decode(TrophyTestData.trophyJson("minecraft:cake",
                "\"requires_mod\":\"neroquests\"")).result().orElseThrow().requiresMods());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"requires_mod\":[]")).error().isPresent());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"requires_mod\":\"Bad Mod\"")).error().isPresent());
    }

    @Test
    void unknownKeysOfAnyShapeAreIgnored() {
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake",
                "\"future\":{\"nested\":{\"deep\":[1,2,{\"x\":true}]}},\"other\":[\"a\"]")).result().isPresent());
    }

    @Test
    void boundsOnKeysRewardsAndSchema() {
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"title\":\"Not A Key\"")).error().isPresent());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"reward\":{\"coins\":10001}")).error().isPresent());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"schema\":0")).error().isPresent());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"schema\":2")).result().isPresent());
        assertTrue(decode(TrophyTestData.trophyJson("minecraft:cake", "\"tier\":\"platinum\"")).error().isPresent());
    }

    @Test
    void longIdsStillGetAValidDefaultKey() {
        TrophyDefinition definition = TrophyDefinition.of(id("nerotrophies:" + "a".repeat(110)),
                decode(TrophyTestData.trophyJson("minecraft:cake", "")).result().orElseThrow());
        assertTrue(definition.title().length() > 128);
    }

    @Test
    void collectionDecodesTrophyAndModList() {
        TrophyCollection.Data data = TrophyCollection.DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"icon\":\"minecraft:map\",\"trim\":\"orbital_plasma\",\"members\":[\"x:a\",\"x:b\"],"
                        + "\"trophy\":\"x:all\",\"requires_mod\":[\"nerospace\"]}")).result().orElseThrow();
        assertEquals(Optional.of(id("x:all")), data.trophy());
        assertEquals(List.of("nerospace"), data.requiresMods());
        assertTrue(TrophyCollection.DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"icon\":\"minecraft:map\",\"trim\":\"Bad Trim\",\"members\":[\"x:a\",\"x:b\"]}")).error().isPresent());
        TrophyCollection collection = TrophyCollection.of(id("x:set"), data, List.of(id("x:a"), id("x:b")),
                data.trophy());
        assertEquals("collection.x.set.title", collection.title());
        assertEquals(Set.of(id("x:a"), id("x:b")), Set.copyOf(collection.members()));
    }
}
