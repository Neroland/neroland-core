package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

/**
 * A reusable <b>map-layer conformance harness</b> (Core 1.19.0) any {@link MapLayerProvider} runs from its own
 * test suite. Like {@code ErasureConformance} it lives in the <b>main</b> source set, because downstream mods
 * depend on Core's published artifact, not on its test classes, and it throws {@link AssertionError} from
 * {@link #verify()} so Core's main source set gains no JUnit dependency.
 *
 * <p>The provider runs through the same guarded path as {@link MapLayerRegistry#query} without being
 * registered. You supply an entitled viewer and a stranger standing <b>beside</b> them (both online, same
 * dimension, at most 8 blocks apart), a present and an absent dimension, near bounds and far bounds (at
 * least 5 000 blocks from both viewers). Pure-store providers can build viewers with
 * {@link MapViewer#forTest} and run in a plain JVM test. The harness derives the rest itself: offline
 * copies of both viewers, the stranger moved to the centre of the far bounds, and the entitled viewer moved
 * to the absent dimension.
 *
 * <h2>What a run checks</h2>
 * <table>
 *   <caption>Per declared scope, queried with the near bounds in the present dimension</caption>
 *   <tr><th>Scope</th><th>Stranger beside</th><th>Stranger far / offline</th><th>Entitled offline / elsewhere</th></tr>
 *   <tr><td>OWNER, MEMBERS, DISCOVERED</td><td>nothing</td><td>nothing</td><td>not checked</td></tr>
 *   <tr><td>PROXIMITY</td><td>only ids the entitled viewer also sees</td><td>nothing</td><td>nothing</td></tr>
 *   <tr><td>PUBLIC</td><td>exactly the entitled id set</td><td>same set</td><td>same set (offline)</td></tr>
 * </table>
 * <p>Plus: layer declarations are valid and there is at least one; every declared layer has
 * {@link #expectEntitledSees} or {@link #expectEmpty} and the entitled viewer sees at least the expected
 * count; the absent dimension yields nothing; the far bounds yield nothing on every {@code PROXIMITY} layer;
 * every {@code PROXIMITY} item returned to any viewer is within the layer's radius of that viewer (markers by
 * point, areas by the nearest point of their bounds); caps and the chunk/vertex budgets hold; a reject of
 * kind {@code UNKNOWN_LAYER}, {@code WRONG_DIMENSION} or {@code DUPLICATE_ID} fails; a provider exception
 * fails; and (G5) no marker id, area id, label text or argument, detail line, area label or badge carries
 * either viewer's UUID, any {@link #forbiddenIds} (dashed or undashed) or any {@link #forbiddenText}, case
 * insensitively.
 *
 * <p><b>Seed at least one entry owned by a third player and pass that player's name and id here</b> (with
 * {@link #forbiddenText} and {@link #forbiddenIds}), so the stranger and identity checks prove something.
 * Seed nothing the stranger owns, is a member of or has discovered: on {@code OWNER}, {@code MEMBERS} and
 * {@code DISCOVERED} layers the stranger must receive nothing at all.
 *
 * <p>Failure text names layers and checks only, never an item id, label or UUID. Not thread-safe; not for
 * production.
 */
public final class MapLayerConformance {

    private static final int BESIDE = 8;
    private static final int FAR = 5_000;

    private final MapLayerProvider provider;
    private MapViewer entitled;
    private MapViewer strangerBeside;
    private ResourceKey<Level> present;
    private ResourceKey<Level> absent;
    private MapBounds bounds;
    private MapBounds farBounds;
    private final List<String> forbiddenText = new ArrayList<>();
    private final List<UUID> forbiddenIds = new ArrayList<>();
    private final Map<Identifier, Integer> expectSees = new LinkedHashMap<>();
    private final Set<Identifier> expectEmpty = new LinkedHashSet<>();

    private MapLayerConformance(MapLayerProvider provider) {
        this.provider = provider;
    }

    /** A harness for {@code provider}. */
    public static MapLayerConformance create(MapLayerProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Conformance needs a provider");
        }
        return new MapLayerConformance(provider);
    }

    /** The entitled viewer and a stranger standing beside them (online, same dimension, at most 8 blocks). */
    public MapLayerConformance viewers(MapViewer entitledViewer, MapViewer stranger) {
        this.entitled = entitledViewer;
        this.strangerBeside = stranger;
        return this;
    }

    /** A dimension the provider has seeded data in, and one it knows nothing about. */
    public MapLayerConformance dimensions(ResourceKey<Level> presentDimension, ResourceKey<Level> absentDimension) {
        this.present = presentDimension;
        this.absent = absentDimension;
        return this;
    }

    /** Query bounds near the entitled viewer, covering the seeded data. */
    public MapLayerConformance bounds(MapBounds near) {
        this.bounds = near;
        return this;
    }

    /** Query bounds at least 5 000 blocks from both viewers (G1). */
    public MapLayerConformance farBounds(MapBounds far) {
        this.farBounds = far;
        return this;
    }

    /** Text that must never appear in any id, label or detail (for example every seeded player's name). */
    public MapLayerConformance forbiddenText(String... text) {
        if (text != null) {
            for (String t : text) {
                if (t != null && !t.isBlank()) {
                    forbiddenText.add(t);
                }
            }
        }
        return this;
    }

    /** Player ids that must never appear, dashed or undashed (the owner and a seeded third player). */
    public MapLayerConformance forbiddenIds(UUID... ids) {
        if (ids != null) {
            for (UUID id : ids) {
                if (id != null) {
                    forbiddenIds.add(id);
                }
            }
        }
        return this;
    }

    /** The entitled viewer must see at least {@code atLeast} items on {@code layer}. */
    public MapLayerConformance expectEntitledSees(Identifier layer, int atLeast) {
        expectSees.put(layer, Math.max(1, atLeast));
        return this;
    }

    /** Explicitly opt {@code layer} out of the "entitled sees something" check; noted in the report. */
    public MapLayerConformance expectEmpty(Identifier layer) {
        expectEmpty.add(layer);
        return this;
    }

    /** Run every check and return the report. */
    public Report run() {
        List<String> failures = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        List<MapLayerInfo> layers;
        try {
            layers = MapLayerRegistry.validate(provider);
        } catch (IllegalArgumentException | NullPointerException bad) {
            failures.add("layer declarations are invalid: " + bad.getMessage());
            return new Report(failures, notes);
        } catch (RuntimeException threw) {
            failures.add("layers() threw " + threw.getClass().getSimpleName());
            return new Report(failures, notes);
        }
        if (layers.isEmpty()) {
            failures.add("the provider declares no layers");
            return new Report(failures, notes);
        }
        if (!setUp(failures)) {
            return new Report(failures, notes);
        }
        for (MapLayerInfo info : layers) {
            boolean sees = expectSees.containsKey(info.id());
            boolean empty = expectEmpty.contains(info.id());
            if (!sees && !empty) {
                failures.add("layer " + info.id() + ": no expectEntitledSees or expectEmpty given");
            } else if (sees && empty) {
                failures.add("layer " + info.id() + ": both expectEntitledSees and expectEmpty given");
            } else if (empty) {
                notes.add("layer " + info.id() + ": declared empty by the test (expectEmpty); its checks prove little");
            }
        }
        Set<Identifier> declared = new LinkedHashSet<>();
        layers.forEach(l -> declared.add(l.id()));
        for (Identifier id : expectSees.keySet()) {
            if (!declared.contains(id)) {
                failures.add("expectation names an undeclared layer " + id);
            }
        }
        for (Identifier id : expectEmpty) {
            if (!declared.contains(id)) {
                failures.add("expectation names an undeclared layer " + id);
            }
        }

        MinecraftServer server = entitled.server();
        BlockPos entitledPos = entitled.position().orElseThrow();
        BlockPos strangerPos = strangerBeside.position().orElseThrow();
        MapViewer entitledOffline = MapViewer.forTest(entitled.id(), server, null, null);
        MapViewer strangerOffline = MapViewer.forTest(strangerBeside.id(), server, null, null);
        MapViewer strangerFar = MapViewer.forTest(strangerBeside.id(), server, present,
                new BlockPos(farBounds.centreX(), strangerPos.getY(), farBounds.centreZ()));
        MapViewer entitledElsewhere = MapViewer.forTest(entitled.id(), server, absent, entitledPos);

        Map<Identifier, MapLayerInfo> byId = new LinkedHashMap<>();
        layers.forEach(l -> byId.put(l.id(), l));

        MapQueryResult e = query("entitled", entitled, present, bounds, byId, failures);
        MapQueryResult sb = query("stranger beside", strangerBeside, present, bounds, byId, failures);
        MapQueryResult sf = query("stranger far", strangerFar, present, bounds, byId, failures);
        MapQueryResult so = query("stranger offline", strangerOffline, present, bounds, byId, failures);
        MapQueryResult eo = query("entitled offline", entitledOffline, present, bounds, byId, failures);
        MapQueryResult ex = query("entitled in another dimension", entitledElsewhere, present, bounds, byId, failures);
        MapQueryResult ea = query("entitled, absent dimension", entitled, absent, bounds, byId, failures);
        MapQueryResult sa = query("stranger, absent dimension", strangerBeside, absent, bounds, byId, failures);
        MapQueryResult ef = query("entitled, far bounds", entitled, present, farBounds, byId, failures);

        for (MapLayerInfo info : layers) {
            Identifier id = info.id();
            String tag = "layer " + id + " (" + info.scope() + ")";
            Set<String> mine = ids(e, id);
            Integer want = expectSees.get(id);
            if (want != null && mine.size() < want) {
                failures.add(tag + ": the entitled viewer saw " + mine.size() + " item(s), expected at least " + want);
            }
            requireNone(tag, "absent dimension (entitled)", ids(ea, id), failures);
            requireNone(tag, "absent dimension (stranger)", ids(sa, id), failures);
            switch (info.scope()) {
                case OWNER, MEMBERS, DISCOVERED -> {
                    requireNone(tag, "stranger beside", ids(sb, id), failures);
                    requireNone(tag, "stranger far", ids(sf, id), failures);
                    requireNone(tag, "stranger offline", ids(so, id), failures);
                }
                case PROXIMITY -> {
                    Set<String> extra = new LinkedHashSet<>(ids(sb, id));
                    extra.removeAll(mine);
                    if (!extra.isEmpty()) {
                        failures.add(tag + ": stranger beside saw " + extra.size()
                                + " item(s) the entitled viewer did not");
                    }
                    requireNone(tag, "stranger far", ids(sf, id), failures);
                    requireNone(tag, "stranger offline", ids(so, id), failures);
                    requireNone(tag, "entitled offline", ids(eo, id), failures);
                    requireNone(tag, "entitled in another dimension", ids(ex, id), failures);
                    requireNone(tag, "far bounds (G1)", ids(ef, id), failures);
                }
                case PUBLIC -> {
                    requireSame(tag, "stranger beside", mine, ids(sb, id), failures);
                    requireSame(tag, "stranger far", mine, ids(sf, id), failures);
                    requireSame(tag, "stranger offline", mine, ids(so, id), failures);
                    requireSame(tag, "entitled offline", mine, ids(eo, id), failures);
                }
            }
        }

        // G5 on dimension badges, for every viewer the harness built.
        for (MapViewer v : List.of(entitled, strangerBeside, entitledOffline, strangerOffline)) {
            for (MapDimensionBadge badge : MapLayerRegistry.badges(provider, v)) {
                if (leaks(badge.id().toString()) || leaks(badge.label())) {
                    failures.add("dimension badges: an id or label contains a player id or forbidden text (G5)");
                    break;
                }
            }
        }
        return new Report(failures, notes);
    }

    /** Run and throw an {@link AssertionError} listing every failure. */
    public void verify() {
        run().assertPassed();
    }

    private boolean setUp(List<String> failures) {
        if (entitled == null || strangerBeside == null) {
            failures.add("viewers(entitled, strangerBeside) not given");
        }
        if (present == null || absent == null || present.equals(absent)) {
            failures.add("dimensions(present, absent) not given, or the same");
        }
        if (bounds == null || farBounds == null) {
            failures.add("bounds(..) and farBounds(..) are both required");
        }
        if (!failures.isEmpty()) {
            return false;
        }
        if (entitled.id().equals(strangerBeside.id())) {
            failures.add("the entitled viewer and the stranger have the same id");
        }
        if (!entitled.online() || !strangerBeside.online()) {
            failures.add("both viewers must be online (dimension and position)");
            return false;
        }
        if (!present.equals(entitled.dimension().orElse(null)) || !present.equals(strangerBeside.dimension().orElse(null))) {
            failures.add("both viewers must stand in the present dimension");
        }
        BlockPos s = strangerBeside.position().orElseThrow();
        if (!entitled.within(present, s.getX(), s.getZ(), BESIDE)) {
            failures.add("the stranger must stand within " + BESIDE + " blocks of the entitled viewer");
        }
        if (entitled.within(present, farBounds, FAR - 1) || strangerBeside.within(present, farBounds, FAR - 1)) {
            failures.add("farBounds must be at least " + FAR + " blocks from both viewers");
        }
        return failures.isEmpty();
    }

    private MapQueryResult query(String who, MapViewer viewer, ResourceKey<Level> dimension, MapBounds queryBounds,
            Map<Identifier, MapLayerInfo> byId, List<String> failures) {
        MapQueryResult r = MapLayerRegistry.run(provider, List.copyOf(byId.values()), viewer, dimension, queryBounds);
        String at = "[" + who + "] ";
        if (r.failed()) {
            failures.add(at + "the provider threw");
            return r;
        }
        for (MapLayerSink.Reject bad : List.of(MapLayerSink.Reject.UNKNOWN_LAYER, MapLayerSink.Reject.WRONG_DIMENSION,
                MapLayerSink.Reject.DUPLICATE_ID)) {
            if (r.rejected(bad) > 0) {
                failures.add(at + r.rejected(bad) + " item(s) refused as " + bad);
            }
        }
        Map<Identifier, int[]> totals = new LinkedHashMap<>();
        boolean leaked = false;
        for (MapMarker m : r.markers()) {
            totals.computeIfAbsent(m.layer(), k -> new int[3])[0]++;
            MapLayerInfo info = byId.get(m.layer());
            if (info != null && info.scope() == MapScope.PROXIMITY
                    && !viewer.within(dimension, m.x(), m.z(), info.proximityRadius())) {
                failures.add(at + "layer " + m.layer() + ": a marker outside the proximity radius of this viewer (G1)");
            }
            leaked |= leaks(m.id()) || leaks(m.label());
            for (MapLabel d : m.details()) {
                leaked |= leaks(d);
            }
        }
        for (MapArea a : r.areas()) {
            int[] t = totals.computeIfAbsent(a.layer(), k -> new int[3]);
            t[0]++;
            if (a instanceof MapArea.Chunks c) {
                t[1] += c.size();
            } else if (a instanceof MapArea.Polygon p) {
                t[2] += p.vertices();
            }
            MapLayerInfo info = byId.get(a.layer());
            if (info != null && info.scope() == MapScope.PROXIMITY
                    && !viewer.within(dimension, a.bounds(), info.proximityRadius())) {
                failures.add(at + "layer " + a.layer() + ": an area outside the proximity radius of this viewer (G1)");
            }
            leaked |= leaks(a.id()) || leaks(a.style().label());
        }
        if (leaked) {
            failures.add(at + "an id, label or detail contains a player id or forbidden text (G5)");
        }
        for (Map.Entry<Identifier, int[]> t : totals.entrySet()) {
            MapLayerInfo info = byId.get(t.getKey());
            if (info != null && t.getValue()[0] > info.maxPerQuery()) {
                failures.add(at + "layer " + t.getKey() + ": cap exceeded");
            }
            if (t.getValue()[1] > MapLayerSink.MAX_CHUNKS_PER_LAYER || t.getValue()[2] > MapLayerSink.MAX_VERTICES_PER_LAYER) {
                failures.add(at + "layer " + t.getKey() + ": payload budget exceeded");
            }
        }
        return r;
    }

    private static Set<String> ids(MapQueryResult r, Identifier layer) {
        Set<String> out = new LinkedHashSet<>();
        for (MapMarker m : r.markers()) {
            if (m.layer().equals(layer)) {
                out.add(m.id());
            }
        }
        for (MapArea a : r.areas()) {
            if (a.layer().equals(layer)) {
                out.add(a.id());
            }
        }
        return out;
    }

    private static void requireNone(String tag, String who, Set<String> got, List<String> failures) {
        if (!got.isEmpty()) {
            failures.add(tag + ": " + who + " received " + got.size() + " item(s); must be none");
        }
    }

    private static void requireSame(String tag, String who, Set<String> expected, Set<String> got,
            List<String> failures) {
        if (!expected.equals(got)) {
            failures.add(tag + ": " + who + " did not receive exactly the entitled viewer's set (PUBLIC is identical"
                    + " for every viewer)");
        }
    }

    private boolean leaks(MapLabel label) {
        if (label == null) {
            return false;
        }
        if (leaks(label.text())) {
            return true;
        }
        for (String arg : label.args()) {
            if (leaks(arg)) {
                return true;
            }
        }
        return false;
    }

    private boolean leaks(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String hay = text.toLowerCase(Locale.ROOT);
        for (String needle : needles()) {
            if (hay.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private List<String> needles() {
        List<String> out = new ArrayList<>();
        List<UUID> ids = new ArrayList<>(forbiddenIds);
        if (entitled != null) {
            ids.add(entitled.id());
        }
        if (strangerBeside != null) {
            ids.add(strangerBeside.id());
        }
        for (UUID id : ids) {
            String dashed = id.toString().toLowerCase(Locale.ROOT);
            out.add(dashed);
            out.add(dashed.replace("-", ""));
        }
        for (String t : forbiddenText) {
            out.add(t.toLowerCase(Locale.ROOT));
        }
        return out;
    }

    /**
     * The outcome of a run. Failures and notes name layers and checks only, never player identity.
     *
     * @param failures every failed check; empty when the provider conforms
     * @param notes    informational lines (for example layers the test declared empty)
     */
    public record Report(List<String> failures, List<String> notes) {

        public Report {
            failures = List.copyOf(failures);
            notes = List.copyOf(notes);
        }

        /** {@code true} when nothing failed. */
        public boolean passed() {
            return failures.isEmpty();
        }

        /** Throw an {@link AssertionError} listing every failure, if any. */
        public void assertPassed() {
            if (!passed()) {
                throw new AssertionError("Map layer conformance failed:\n - " + String.join("\n - ", failures));
            }
        }
    }
}
