package za.co.neroland.nerolandcore.config;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A single typed config entry: a key, a default, a parser/formatter for the
 * on-disk string form, and a sanitiser (range clamp or predicate) applied to any
 * value before it is stored. Created through {@link ConfigSchema}'s typed builders
 * — downstream code holds the returned handle and reads {@link #get()}.
 *
 * <p>Values flagged {@link #serverAuthoritative()} are owned by the server and
 * pushed to clients (see {@link ConfigManager#serverAuthoritativeSnapshot()}); a
 * client never trusts its own local copy of a server-authoritative value while
 * connected to a server.
 *
 * @param <T> the value type (Boolean, Integer, Long, Double, String)
 */
public final class ConfigValue<T> {

    private final String key;
    private final String comment;
    private final T defaultValue;
    private final boolean serverAuthoritative;
    private final Function<String, T> parser;
    private final Function<T, String> formatter;
    private final UnaryOperator<T> sanitizer;

    private volatile T value;

    ConfigValue(String key, String comment, T defaultValue, boolean serverAuthoritative,
            Function<String, T> parser, Function<T, String> formatter, UnaryOperator<T> sanitizer) {
        this.key = key;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.serverAuthoritative = serverAuthoritative;
        this.parser = parser;
        this.formatter = formatter;
        this.sanitizer = sanitizer;
        this.value = sanitizer.apply(defaultValue);
    }

    /** The current effective value (sanitised). */
    public T get() {
        return value;
    }

    public String key() {
        return key;
    }

    public String comment() {
        return comment;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public boolean serverAuthoritative() {
        return serverAuthoritative;
    }

    /** The current value in its on-disk string form. */
    public String asString() {
        return formatter.apply(value);
    }

    public String defaultAsString() {
        return formatter.apply(defaultValue);
    }

    /** Parse and store a raw string (null/blank/invalid falls back to the default). */
    void loadFrom(String raw) {
        if (raw == null || raw.isBlank()) {
            this.value = sanitizer.apply(defaultValue);
            return;
        }
        try {
            this.value = sanitizer.apply(parser.apply(raw.trim()));
        } catch (RuntimeException e) {
            this.value = sanitizer.apply(defaultValue);
        }
    }

    void resetToDefault() {
        this.value = sanitizer.apply(defaultValue);
    }
}
