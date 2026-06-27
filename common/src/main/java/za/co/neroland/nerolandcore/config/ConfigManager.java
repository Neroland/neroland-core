package za.co.neroland.nerolandcore.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * The shared config service. Every Neroland mod registers a {@link ConfigSchema}
 * here instead of hand-rolling config I/O, guaranteeing one file format, one
 * reload command ({@code /neroland config reload}) and one sync path.
 *
 * <p>Persistence is a plain {@code <modId>.properties} file in the loader config
 * directory (reached through the {@link Services#PLATFORM} seam). Files are created
 * with defaults on first run and migrated in place when new keys are added; an
 * existing file's user edits are otherwise left untouched.
 *
 * <p><b>Server-authoritative values</b> are collected into a snapshot
 * ({@link #serverAuthoritativeSnapshot()}) the server pushes to clients, which
 * apply it through {@link #applyServerValues(Map)}. Privacy (POPIA/GDPR): the
 * snapshot carries only config keys and values — never player data.
 */
public final class ConfigManager {

    private static final Map<String, ConfigSchema> SCHEMAS = new LinkedHashMap<>();

    private ConfigManager() {
    }

    /** Register a schema and load it from disk (creating the file with defaults if absent). */
    public static synchronized void register(ConfigSchema schema) {
        SCHEMAS.put(schema.modId(), schema);
        loadSchema(schema);
    }

    /** Re-read every registered schema from disk. Backs {@code /neroland config reload}. */
    public static synchronized void reloadAll() {
        SCHEMAS.values().forEach(ConfigManager::loadSchema);
    }

    public static Collection<ConfigSchema> schemas() {
        return SCHEMAS.values();
    }

    private static void loadSchema(ConfigSchema schema) {
        final Path file;
        try {
            file = Services.PLATFORM.getConfigDir().resolve(schema.modId() + ".properties");
        } catch (RuntimeException e) {
            return; // no config dir available — keep defaults
        }

        Properties props = new Properties();
        boolean fileMissing = !Files.exists(file);
        if (!fileMissing) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not read {}.properties; using defaults.",
                        schema.modId(), e);
            }
        }

        boolean anyKeyMissing = false;
        for (ConfigValue<?> value : schema.values()) {
            String raw = props.getProperty(value.key());
            if (raw == null) {
                anyKeyMissing = true;
            }
            value.loadFrom(raw);
        }

        // Only touch disk when the file is new or gained keys — preserves user edits + comments otherwise.
        if (fileMissing || anyKeyMissing) {
            writeSchema(schema, file);
        }
    }

    private static void writeSchema(ConfigSchema schema, Path file) {
        Properties out = new Properties();
        StringBuilder comment = new StringBuilder(schema.header());
        for (ConfigValue<?> value : schema.values()) {
            out.setProperty(value.key(), value.asString());
            comment.append(System.lineSeparator())
                    .append(value.key()).append(": ").append(value.comment())
                    .append(" (default ").append(value.defaultAsString())
                    .append(value.serverAuthoritative() ? "; server-authoritative" : "")
                    .append(')');
        }
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream os = Files.newOutputStream(file)) {
                out.store(os, comment.toString());
            }
        } catch (IOException e) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not write {}.properties; using defaults.",
                    schema.modId(), e);
        }
    }

    /** Snapshot of every server-authoritative value, keyed {@code <modId>:<key>}, for client sync. */
    public static Map<String, String> serverAuthoritativeSnapshot() {
        Map<String, String> snapshot = new LinkedHashMap<>();
        for (ConfigSchema schema : SCHEMAS.values()) {
            for (ConfigValue<?> value : schema.values()) {
                if (value.serverAuthoritative()) {
                    snapshot.put(schema.modId() + ":" + value.key(), value.asString());
                }
            }
        }
        return snapshot;
    }

    /** Apply a server-pushed snapshot on the client (only server-authoritative values are touched). */
    public static void applyServerValues(Map<String, String> values) {
        for (ConfigSchema schema : SCHEMAS.values()) {
            for (ConfigValue<?> value : schema.values()) {
                if (value.serverAuthoritative()) {
                    String key = schema.modId() + ":" + value.key();
                    String raw = values.get(key);
                    if (raw != null) {
                        value.loadFrom(raw);
                    }
                }
            }
        }
    }
}
