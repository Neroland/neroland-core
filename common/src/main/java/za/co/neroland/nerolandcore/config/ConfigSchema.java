package za.co.neroland.nerolandcore.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * A per-mod set of typed config values. A mod creates a schema, declares its
 * values through the typed builders (each returns a {@link ConfigValue} handle to
 * read later), and registers it with {@link ConfigManager#register(ConfigSchema)}.
 * Core then owns persistence: one {@code <modId>.properties} file, defaults,
 * validation, hot-reload, and server-authoritative sync.
 *
 * <p>Builders preserve declaration order (it drives the file layout and the
 * {@code /neroland config list} output).
 */
public final class ConfigSchema {

    private final String modId;
    private final String header;
    private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();

    private ConfigSchema(String modId, String header) {
        this.modId = modId;
        this.header = header;
    }

    /**
     * @param modId  the owning mod id; also the config file name ({@code <modId>.properties})
     * @param header a one-line description written at the top of the file
     */
    public static ConfigSchema create(String modId, String header) {
        return new ConfigSchema(modId, header);
    }

    public ConfigValue<Boolean> bool(String key, boolean def, boolean serverAuthoritative, String comment) {
        return add(new ConfigValue<>(key, comment, def, serverAuthoritative,
                Boolean::parseBoolean, b -> Boolean.toString(b), UnaryOperator.identity()));
    }

    public ConfigValue<Integer> intRange(String key, int def, int min, int max,
            boolean serverAuthoritative, String comment) {
        return add(new ConfigValue<>(key, comment, def, serverAuthoritative,
                Integer::parseInt, i -> Integer.toString(i), i -> Math.max(min, Math.min(max, i))));
    }

    public ConfigValue<Long> longRange(String key, long def, long min, long max,
            boolean serverAuthoritative, String comment) {
        return add(new ConfigValue<>(key, comment, def, serverAuthoritative,
                Long::parseLong, l -> Long.toString(l), l -> Math.max(min, Math.min(max, l))));
    }

    public ConfigValue<Double> doubleRange(String key, double def, double min, double max,
            boolean serverAuthoritative, String comment) {
        return add(new ConfigValue<>(key, comment, def, serverAuthoritative,
                Double::parseDouble, d -> Double.toString(d), d -> Math.max(min, Math.min(max, d))));
    }

    public ConfigValue<String> string(String key, String def, boolean serverAuthoritative, String comment) {
        return add(new ConfigValue<>(key, comment, def, serverAuthoritative,
                s -> s, s -> s, UnaryOperator.identity()));
    }

    private <T> ConfigValue<T> add(ConfigValue<T> value) {
        values.put(value.key(), value);
        return value;
    }

    public String modId() {
        return modId;
    }

    public String header() {
        return header;
    }

    public Collection<ConfigValue<?>> values() {
        return values.values();
    }

    public ConfigValue<?> value(String key) {
        return values.get(key);
    }
}
