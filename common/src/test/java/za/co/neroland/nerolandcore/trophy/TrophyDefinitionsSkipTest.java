package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.id;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.trophyJson;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

/** The loader's quiet skip rules, issues and hard bounds. */
class TrophyDefinitionsSkipTest {

    private static final String COLLECTION = "{\"icon\":\"minecraft:map\",\"trim\":\"gold_leaf\",\"members\":[%s]%s}";

    private static String collection(String members, String extra) {
        return String.format(COLLECTION, members, extra.isEmpty() ? "" : "," + extra);
    }

    private static List<String> reasons(TrophyLoadReport report) {
        return report.issues().stream().map(TrophyLoadReport.Issue::reason).toList();
    }

    @Test
    void absentRequiredModIsSkippedQuietly() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:kept", trophyJson("minecraft:cake", ""))
                .trophy("x:absent", trophyJson("minecraft:cake", "\"requires_mod\":\"notinstalled\""))
                .trophy("x:one_absent", trophyJson("minecraft:cake", "\"requires_mod\":[\"present\",\"notinstalled\"]"))
                .load(Set.of("notinstalled"), Set.of());
        assertEquals(Set.of(id("x:kept")), result.snapshot().trophies().keySet());
        assertEquals(2, result.report().skippedAbsentMod());
        assertTrue(result.report().issues().isEmpty());
    }

    @Test
    void missingIconIsSkippedQuietly() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:no_icon", trophyJson("gone:item", ""))
                .load(Set.of(), Set.of("gone:item"));
        assertTrue(result.snapshot().trophies().isEmpty());
        assertEquals(1, result.report().skippedMissingIcon());
        assertEquals(0, result.report().issueCount());
    }

    @Test
    void malformedFilesAreDroppedWholeWithAnIssue() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:not_json", "{ this is not json")
                .trophy("x:no_tier", "{\"icon\":\"minecraft:cake\"}")
                .trophy("x:array", "[1,2,3]")
                .trophy("x:deep", "[".repeat(15_000))
                .trophy("x:ok", trophyJson("minecraft:cake", ""))
                .load(Set.of(), Set.of());
        assertEquals(Set.of(id("x:ok")), result.snapshot().trophies().keySet());
        assertEquals(List.of("malformed", "malformed", "malformed", "malformed"), reasons(result.report()));
    }

    @Test
    void oversizeFileIsRejected() {
        byte[] big = (trophyJson("minecraft:cake", "\"pad\":\"" + "x".repeat(TrophyDefinitions.MAX_FILE_BYTES) + "\""))
                .getBytes(StandardCharsets.UTF_8);
        TrophyDefinitions.Result result = new TrophyTestData()
                .put(TrophyDefinitions.TROPHY_DIRECTORY, "x:big", big)
                .load(Set.of(), Set.of());
        assertTrue(result.snapshot().trophies().isEmpty());
        assertEquals(List.of("too_large"), reasons(result.report()));
    }

    @Test
    void trophyLimitStopsAtTheCapInSortedOrder() {
        TrophyTestData data = new TrophyTestData();
        for (int i = 0; i <= TrophyDefinitions.MAX_TROPHIES; i++) {
            data.trophy(String.format("x:t%05d", i), trophyJson("minecraft:cake", ""));
        }
        TrophyDefinitions.Result result = data.load(Set.of(), Set.of());
        assertEquals(TrophyDefinitions.MAX_TROPHIES, result.snapshot().trophies().size());
        assertTrue(result.snapshot().trophies().containsKey(id("x:t00000")));
        assertFalse(result.snapshot().trophies().containsKey(id(String.format("x:t%05d", TrophyDefinitions.MAX_TROPHIES))));
        assertEquals(List.of("limit"), reasons(result.report()));
    }

    @Test
    void filesExaminedPerRootAreBoundedAndIssuesAreCapped() {
        TrophyTestData data = new TrophyTestData();
        for (int i = 0; i < TrophyDefinitions.MAX_FILES_PER_ROOT + 10; i++) {
            data.trophy(String.format("x:bad%05d", i), "{");
        }
        TrophyDefinitions.Result result = data.load(Set.of(), Set.of());
        assertEquals(TrophyLoadReport.MAX_ISSUES, result.report().issues().size());
        // Every examined file is one malformed issue, plus one limit issue for the files not examined.
        assertEquals(TrophyDefinitions.MAX_FILES_PER_ROOT + 1, result.report().issueCount());
    }

    @Test
    void collectionDropsMembersSkippedForAnAbsentModOrMissingIcon() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:b", trophyJson("minecraft:cake", ""))
                .trophy("x:c", trophyJson("minecraft:cake", "\"requires_mod\":\"notinstalled\""))
                .trophy("x:d", trophyJson("gone:item", ""))
                .collection("x:set", collection("\"x:a\",\"x:b\",\"x:c\",\"x:d\"", ""))
                .load(Set.of("notinstalled"), Set.of("gone:item"));
        TrophyCollection set = result.snapshot().collection(id("x:set")).orElseThrow();
        assertEquals(List.of(id("x:a"), id("x:b")), set.members());
        assertEquals(0, result.report().issueCount());
    }

    @Test
    void collectionWithAMemberThatNeverLoadedIsAnIssue() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:b", trophyJson("minecraft:cake", ""))
                .trophy("x:broken", "{")
                .collection("x:typo", collection("\"x:a\",\"x:b\",\"x:nope\"", ""))
                .collection("x:has_broken", collection("\"x:a\",\"x:b\",\"x:broken\"", ""))
                .load(Set.of(), Set.of());
        assertTrue(result.snapshot().collections().isEmpty());
        assertEquals(List.of("malformed", "unknown_member", "unknown_member"), reasons(result.report()));
    }

    @Test
    void collectionLeftWithOneMemberIsSkippedQuietly() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:c", trophyJson("minecraft:cake", "\"requires_mod\":\"notinstalled\""))
                .collection("x:set", collection("\"x:a\",\"x:c\"", ""))
                .load(Set.of("notinstalled"), Set.of());
        assertTrue(result.snapshot().collections().isEmpty());
        assertEquals(0, result.report().issueCount());
    }

    @Test
    void collectionIdClashingWithATrophyIsAnIssue() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:b", trophyJson("minecraft:cake", ""))
                .collection("x:a", collection("\"x:a\",\"x:b\"", ""))
                .load(Set.of(), Set.of());
        assertTrue(result.snapshot().collections().isEmpty());
        assertEquals(List.of("id_clash"), reasons(result.report()));
    }

    @Test
    void completionTrophyIsKeptOnlyIfLoaded() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:b", trophyJson("minecraft:cake", ""))
                .trophy("x:all", "{\"icon\":\"minecraft:cake\",\"tier\":\"gold\"}")
                .collection("x:with", collection("\"x:a\",\"x:b\"", "\"trophy\":\"x:all\""))
                .collection("x:without", collection("\"x:a\",\"x:b\"", "\"trophy\":\"x:missing\""))
                .load(Set.of(), Set.of());
        assertEquals(Optional.of(id("x:all")), result.snapshot().collection(id("x:with")).orElseThrow().trophy());
        assertEquals(Optional.empty(), result.snapshot().collection(id("x:without")).orElseThrow().trophy());
        assertEquals(0, result.report().issueCount());
    }

    @Test
    void collectionMemberBoundIsEnforced() {
        TrophyTestData data = new TrophyTestData();
        StringBuilder members = new StringBuilder();
        for (int i = 0; i <= TrophyDefinitions.MAX_MEMBERS; i++) {
            members.append(i == 0 ? "" : ",").append("\"x:m").append(i).append('"');
        }
        TrophyDefinitions.Result result = data.collection("x:huge", collection(members.toString(), ""))
                .load(Set.of(), Set.of());
        assertTrue(result.snapshot().collections().isEmpty());
        assertEquals(List.of("limit"), reasons(result.report()));
    }

    @Test
    void collectionWithAbsentModIsSkippedQuietly() {
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy("x:a", trophyJson("minecraft:cake", ""))
                .trophy("x:b", trophyJson("minecraft:cake", ""))
                .collection("x:set", collection("\"x:a\",\"x:b\"", "\"requires_mod\":\"notinstalled\""))
                .load(Set.of("notinstalled"), Set.of());
        assertTrue(result.snapshot().collections().isEmpty());
        assertEquals(1, result.report().skippedAbsentMod());
        assertEquals(0, result.report().issueCount());
    }

    @Test
    void definitionIdComesFromTheFilePath() {
        Identifier id = id("nerotrophies:nerospace/first_launch");
        TrophyDefinitions.Result result = new TrophyTestData()
                .trophy(id.toString(), trophyJson("minecraft:cake", ""))
                .load(Set.of(), Set.of());
        assertEquals(id, result.snapshot().trophy(id).orElseThrow().id());
    }
}
