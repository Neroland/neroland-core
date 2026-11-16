package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A map label: either a lang key with up to {@link #MAX_ARGS} plain-text arguments, or a short literal
 * (Core 1.19.0). Core holds no client types, so this is not a {@code Component}; the consumer resolves it.
 *
 * <p>Literal text and every argument are {@linkplain #sanitise sanitised}: control characters, the
 * formatting sign {@code §} together with the code after it, line breaks and bidirectional controls
 * (U+202A–U+202E, U+2066–U+2069) are stripped, then the text is clamped in code points. A lang key must match {@code [a-z0-9_.-]{1,128}},
 * otherwise {@link IllegalArgumentException}. Arguments beyond {@link #MAX_ARGS} are dropped. A literal
 * carries no arguments.
 *
 * <p><b>Privacy.</b> A literal is for a name the viewer is already entitled to read (their station, their
 * colony). Never a player name that is not the viewer's.
 *
 * @param text    the lang key, or the literal text
 * @param args    arguments for a translatable label (empty for a literal)
 * @param literal {@code true} when {@code text} is literal text rather than a lang key
 */
public record MapLabel(String text, List<String> args, boolean literal) {

    /** Longest literal, in code points. */
    public static final int MAX_LITERAL = 48;
    /** Most arguments on a translatable label; extra arguments are dropped. */
    public static final int MAX_ARGS = 4;
    /** Longest argument, in code points. */
    public static final int MAX_ARG = 48;
    /** Longest lang key. */
    public static final int MAX_KEY = 128;

    static final Pattern KEY = Pattern.compile("[a-z0-9_.-]{1," + MAX_KEY + "}");

    public MapLabel {
        if (text == null) {
            throw new IllegalArgumentException("A map label needs text or a lang key");
        }
        if (literal) {
            text = sanitise(text, MAX_LITERAL);
            args = List.of();
        } else {
            requireKey(text, "map label key");
            List<String> clean = new ArrayList<>();
            if (args != null) {
                for (String arg : args) {
                    if (clean.size() == MAX_ARGS) {
                        break;
                    }
                    clean.add(sanitise(arg == null ? "" : arg, MAX_ARG));
                }
            }
            args = List.copyOf(clean);
        }
    }

    /** A translatable label: {@code key} must match {@code [a-z0-9_.-]{1,128}}. */
    public static MapLabel translatable(String key, String... args) {
        return new MapLabel(key, args == null ? List.of() : java.util.Arrays.asList(args), false);
    }

    /** A literal label, sanitised and clamped to {@link #MAX_LITERAL} code points. */
    public static MapLabel literal(String text) {
        return new MapLabel(text == null ? "" : text, List.of(), true);
    }

    /**
     * Strip control characters, {@code §} with the formatting code after it, line breaks and bidirectional
     * controls, then clamp to {@code maxCodePoints} code points. Never returns {@code null}.
     */
    public static String sanitise(String raw, int maxCodePoints) {
        if (raw == null || raw.isEmpty() || maxCodePoints <= 0) {
            return "";
        }
        StringBuilder out = new StringBuilder(Math.min(raw.length(), maxCodePoints * 2));
        int kept = 0;
        for (int i = 0; i < raw.length() && kept < maxCodePoints;) {
            int cp = raw.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\u00A7') {
                if (i < raw.length()) {
                    i += Character.charCount(raw.codePointAt(i)); // the formatting code that follows it
                }
                continue;
            }
            if (isStripped(cp)) {
                continue;
            }
            out.appendCodePoint(cp);
            kept++;
        }
        return out.toString();
    }

    private static boolean isStripped(int cp) {
        return Character.isISOControl(cp)
                || cp == 0x2028 || cp == 0x2029
                || (cp >= 0x202A && cp <= 0x202E)
                || (cp >= 0x2066 && cp <= 0x2069)
                || cp == 0x200E || cp == 0x200F || cp == 0x061C;
    }

    static void requireKey(String key, String what) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid " + what + ": must match [a-z0-9_.-]{1," + MAX_KEY + "}");
        }
    }
}
