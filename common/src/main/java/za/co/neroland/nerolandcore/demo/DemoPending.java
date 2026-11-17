package za.co.neroland.nerolandcore.demo;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

/**
 * Hands the picker's selection from the client to the integrated server that the client is about to start
 * (Core 1.20.0). Singleplayer only: both run in the same process, so a single in-memory slot is enough. The
 * slot holds the new world's name and the selected module ids, nothing else, and is cleared when taken or
 * replaced. It is never written to disk.
 */
@ApiStatus.Internal
public final class DemoPending {

    /** One pending demo world. */
    public record Request(String levelName, List<String> moduleIds) {

        public Request {
            moduleIds = moduleIds == null ? List.of() : List.copyOf(moduleIds);
        }
    }

    private static Request pending;

    private DemoPending() {
    }

    /** Remembers the selection for the world about to be created (replacing any earlier one). */
    public static synchronized void offer(String levelName, List<String> moduleIds) {
        pending = levelName == null ? null : new Request(levelName, moduleIds);
    }

    /** The request for a world named {@code levelName}, removed from the slot; {@code null} if none matches. */
    public static synchronized Request take(String levelName) {
        Request r = pending;
        if (r == null || levelName == null || !r.levelName().equals(levelName)) {
            return null;
        }
        pending = null;
        return r;
    }

    /** Drops any pending request (the world creation was cancelled or failed). */
    public static synchronized void clear() {
        pending = null;
    }
}
