package za.co.neroland.nerolandcore.progression;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Persistent server-authoritative material milestone values across player/team/server scopes. */
@org.jetbrains.annotations.ApiStatus.Internal
public final class MaterialMilestoneState extends SavedData {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "material_milestones");
    public static final SavedDataType<MaterialMilestoneState> TYPE =
            new SavedDataType<>(ID, MaterialMilestoneState::new, codec(), null);

    private final Map<String, Set<String>> server = new LinkedHashMap<>();
    private final Map<UUID, Map<String, Set<String>>> players = new LinkedHashMap<>();
    private final Map<String, Map<String, Set<String>>> teams = new LinkedHashMap<>();

    public static MaterialMilestoneState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean containsServer(Identifier milestone, Identifier material) {
        return contains(this.server, milestone, material);
    }

    public boolean containsPlayer(UUID player, Identifier milestone, Identifier material) {
        return contains(this.players.get(player), milestone, material);
    }

    public boolean containsTeam(String team, Identifier milestone, Identifier material) {
        return contains(this.teams.get(team), milestone, material);
    }

    public boolean setServer(Identifier milestone, Identifier material, boolean present) {
        return set(this.server, milestone, material, present);
    }

    public boolean setPlayer(UUID player, Identifier milestone, Identifier material, boolean present) {
        Map<String, Set<String>> values = this.players.computeIfAbsent(player, ignored -> new LinkedHashMap<>());
        boolean changed = set(values, milestone, material, present);
        if (!present && values.isEmpty()) {
            this.players.remove(player);
        }
        return changed;
    }

    public boolean setTeam(String team, Identifier milestone, Identifier material, boolean present) {
        Map<String, Set<String>> values = this.teams.computeIfAbsent(team, ignored -> new LinkedHashMap<>());
        boolean changed = set(values, milestone, material, present);
        if (!present && values.isEmpty()) {
            this.teams.remove(team);
        }
        return changed;
    }

    public Map<Identifier, Set<Identifier>> playerValues(UUID player) {
        return export(this.players.get(player));
    }

    public Map<Identifier, Set<Identifier>> resolved(UUID player, String team) {
        Map<String, Set<String>> merged = copy(this.server);
        merge(merged, team == null ? null : this.teams.get(team));
        merge(merged, this.players.get(player));
        return export(merged);
    }

    public void forgetPlayer(UUID player) {
        if (this.players.remove(player) != null) {
            setDirty();
        }
    }

    private boolean set(Map<String, Set<String>> values, Identifier milestone, Identifier material,
            boolean present) {
        String milestoneKey = milestone.toString();
        Set<String> materials = values.computeIfAbsent(milestoneKey, ignored -> new LinkedHashSet<>());
        boolean changed = present ? materials.add(material.toString()) : materials.remove(material.toString());
        if (materials.isEmpty()) {
            values.remove(milestoneKey);
        }
        if (changed) {
            setDirty();
        }
        return changed;
    }

    private static boolean contains(Map<String, Set<String>> values, Identifier milestone, Identifier material) {
        return values != null && values.getOrDefault(milestone.toString(), Set.of()).contains(material.toString());
    }

    private static Map<String, Set<String>> copy(Map<String, Set<String>> source) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        merge(result, source);
        return result;
    }

    private static void merge(Map<String, Set<String>> target, Map<String, Set<String>> source) {
        if (source != null) {
            source.forEach((key, values) ->
                    target.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).addAll(values));
        }
    }

    private static Map<Identifier, Set<Identifier>> export(Map<String, Set<String>> values) {
        if (values == null) {
            return Map.of();
        }
        Map<Identifier, Set<Identifier>> result = new LinkedHashMap<>();
        values.forEach((milestone, materials) -> {
            Identifier milestoneId = Identifier.tryParse(milestone);
            if (milestoneId == null) {
                return;
            }
            Set<Identifier> ids = new LinkedHashSet<>();
            materials.forEach(material -> {
                Identifier id = Identifier.tryParse(material);
                if (id != null) {
                    ids.add(id);
                }
            });
            result.put(milestoneId, Set.copyOf(ids));
        });
        return Map.copyOf(result);
    }

    private record Bucket(String key, List<Value> values) {
        static final Codec<Bucket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("key").forGetter(Bucket::key),
                Value.CODEC.listOf().fieldOf("values").forGetter(Bucket::values)
        ).apply(instance, Bucket::new));
    }

    private record Value(String milestone, List<String> materials) {
        static final Codec<Value> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("milestone").forGetter(Value::milestone),
                Codec.STRING.listOf().fieldOf("materials").forGetter(Value::materials)
        ).apply(instance, Value::new));
    }

    private static Codec<MaterialMilestoneState> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Value.CODEC.listOf().optionalFieldOf("server", List.of()).forGetter(state -> encode(state.server)),
                Bucket.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(MaterialMilestoneState::players),
                Bucket.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(MaterialMilestoneState::teams)
        ).apply(instance, MaterialMilestoneState::decode));
    }

    private List<Bucket> players() {
        List<Bucket> result = new ArrayList<>();
        this.players.forEach((key, values) -> result.add(new Bucket(key.toString(), encode(values))));
        return result;
    }

    private List<Bucket> teams() {
        List<Bucket> result = new ArrayList<>();
        this.teams.forEach((key, values) -> result.add(new Bucket(key, encode(values))));
        return result;
    }

    private static List<Value> encode(Map<String, Set<String>> values) {
        List<Value> result = new ArrayList<>();
        values.forEach((key, materials) -> result.add(new Value(key, new ArrayList<>(materials))));
        return result;
    }

    private static Map<String, Set<String>> decodeValues(List<Value> values) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        values.forEach(value -> result.put(value.milestone(), new LinkedHashSet<>(value.materials())));
        return result;
    }

    private static MaterialMilestoneState decode(List<Value> server, List<Bucket> players, List<Bucket> teams) {
        MaterialMilestoneState state = new MaterialMilestoneState();
        state.server.putAll(decodeValues(server));
        players.forEach(bucket -> {
            try {
                state.players.put(UUID.fromString(bucket.key()), decodeValues(bucket.values()));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed UUID rows rather than failing the whole save.
            }
        });
        teams.forEach(bucket -> state.teams.put(bucket.key(), decodeValues(bucket.values())));
        return state;
    }
}
