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

/**
 * Server-authoritative, persistent store of which gates are open, in each scope:
 * a server-wide set, a per-player map (keyed by UUID), and a per-team map (keyed by
 * scoreboard team name). Stored on the overworld so it is always loaded, via the
 * same {@link SavedDataType} codec pattern Core uses elsewhere.
 *
 * <p>Privacy (POPIA/GDPR): player rows are keyed by UUID and hold only gameplay
 * gate ids — no names, IPs, chat or location. Use
 * {@link #forgetPlayer(UUID)} to purge a player (the shared erasure hook).
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ProgressionState extends SavedData {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "progression");

    public static final SavedDataType<ProgressionState> TYPE =
            new SavedDataType<>(ID, ProgressionState::new, codec(), null);

    private final Set<String> server = new LinkedHashSet<>();
    private final Map<UUID, Set<String>> players = new LinkedHashMap<>();
    private final Map<String, Set<String>> teams = new LinkedHashMap<>();

    public ProgressionState() {
    }

    /** The one store, on the overworld so it is always loaded. */
    public static ProgressionState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    // --- server scope -------------------------------------------------------
    public boolean isServerOpen(Identifier gate) {
        return server.contains(gate.toString());
    }

    public boolean setServer(Identifier gate, boolean open) {
        boolean changed = open ? server.add(gate.toString()) : server.remove(gate.toString());
        if (changed) {
            setDirty();
        }
        return changed;
    }

    // --- player scope -------------------------------------------------------
    public boolean isPlayerOpen(UUID player, Identifier gate) {
        Set<String> set = players.get(player);
        return set != null && set.contains(gate.toString());
    }

    public boolean setPlayer(UUID player, Identifier gate, boolean open) {
        Set<String> set = players.computeIfAbsent(player, ignored -> new LinkedHashSet<>());
        boolean changed = open ? set.add(gate.toString()) : set.remove(gate.toString());
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public Set<String> playerGates(UUID player) {
        return Set.copyOf(players.getOrDefault(player, Set.of()));
    }

    // --- team scope ---------------------------------------------------------
    public boolean isTeamOpen(String team, Identifier gate) {
        Set<String> set = teams.get(team);
        return set != null && set.contains(gate.toString());
    }

    public boolean setTeam(String team, Identifier gate, boolean open) {
        Set<String> set = teams.computeIfAbsent(team, ignored -> new LinkedHashSet<>());
        boolean changed = open ? set.add(gate.toString()) : set.remove(gate.toString());
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public Set<String> teamGates(String team) {
        return Set.copyOf(teams.getOrDefault(team, Set.of()));
    }

    public Set<String> serverGates() {
        return Set.copyOf(server);
    }

    /** POPIA/GDPR erasure: drop everything stored for a player. */
    public void forgetPlayer(UUID player) {
        if (players.remove(player) != null) {
            setDirty();
        }
    }

    // --- persistence --------------------------------------------------------
    private record Entry(String key, List<String> gates) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("key").forGetter(Entry::key),
                Codec.STRING.listOf().fieldOf("gates").forGetter(Entry::gates)
        ).apply(inst, Entry::new));
    }

    private static Codec<ProgressionState> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.listOf().optionalFieldOf("server", List.of())
                        .forGetter(s -> new ArrayList<>(s.server)),
                Entry.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(ProgressionState::playerEntries),
                Entry.CODEC.listOf().optionalFieldOf("teams", List.of()).forGetter(ProgressionState::teamEntries)
        ).apply(inst, ProgressionState::fromData));
    }

    private List<Entry> playerEntries() {
        List<Entry> out = new ArrayList<>();
        players.forEach((uuid, gates) -> out.add(new Entry(uuid.toString(), new ArrayList<>(gates))));
        return out;
    }

    private List<Entry> teamEntries() {
        List<Entry> out = new ArrayList<>();
        teams.forEach((team, gates) -> out.add(new Entry(team, new ArrayList<>(gates))));
        return out;
    }

    private static ProgressionState fromData(List<String> server, List<Entry> players, List<Entry> teams) {
        ProgressionState state = new ProgressionState();
        state.server.addAll(server);
        for (Entry entry : players) {
            try {
                state.players.put(UUID.fromString(entry.key()), new LinkedHashSet<>(entry.gates()));
            } catch (IllegalArgumentException ignored) {
                // skip malformed UUID rows
            }
        }
        for (Entry entry : teams) {
            state.teams.put(entry.key(), new LinkedHashSet<>(entry.gates()));
        }
        return state;
    }
}
