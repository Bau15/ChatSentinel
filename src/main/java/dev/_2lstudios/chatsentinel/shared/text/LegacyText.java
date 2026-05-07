package dev._2lstudios.chatsentinel.shared.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyText {
    private static final char COLOR_CHAR = '\u00A7';
    private static final String VALID_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private LegacyText() {
    }

    public static String toSection(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder builder = null;
        final int length = input.length();
        for (int i = 0; i < length; i++) {
            final char current = input.charAt(i);
            if (current == '&' && i + 1 < length && isLegacyCode(input.charAt(i + 1))) {
                if (builder == null) {
                    builder = new StringBuilder(length);
                    builder.append(input, 0, i);
                }
                builder.append(COLOR_CHAR);
                builder.append(input.charAt(i + 1));
                i++;
                continue;
            }
            if (builder != null) {
                builder.append(current);
            }
        }
        return builder == null ? input : builder.toString();
    }

    public static List<String> toSectionLines(final String input) {
        final String section = toSection(input);
        if (section.isEmpty()) {
            return Collections.emptyList();
        }
        final String[] split = section.split("\\r?\\n", -1);
        final List<String> lines = new ArrayList<String>(split.length);
        for (final String line : split) {
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static boolean isLegacyCode(final char code) {
        return VALID_CODES.indexOf(code) >= 0;
    }
}
