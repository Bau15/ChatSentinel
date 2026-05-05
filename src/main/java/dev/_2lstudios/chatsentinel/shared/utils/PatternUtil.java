package dev._2lstudios.chatsentinel.shared.utils;

import java.util.Collection;
import java.util.regex.Pattern;

public class PatternUtil {
    private static final Pattern NEVER_MATCH = Pattern.compile("(?!)");

    public static Pattern compile(String[] patterns) {
        return compileSafe(patterns);
    }

    public static Pattern compileSafe(String[] patterns) {
        StringBuilder patternBuilder = new StringBuilder();

        for (String entry : patterns) {
            if (entry == null) {
                continue;
            }

            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            if (patternBuilder.length() <= 0) {
                patternBuilder.append("(" + entry);
            } else {
                patternBuilder.append(")|(" + entry);
            }
        }

        if (patternBuilder.length() <= 0) {
            return NEVER_MATCH;
        }

        patternBuilder.append(")");

        return Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    public static Pattern compile(Collection<String> patterns) {
        return compileSafe(patterns);
    }

    public static Pattern compileSafe(Collection<String> patterns) {
        StringBuilder patternBuilder = new StringBuilder();

        for (String entry : patterns) {
            if (entry == null) {
                continue;
            }

            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            if (patternBuilder.length() <= 0) {
                patternBuilder.append("(" + entry);
            } else {
                patternBuilder.append(")|(" + entry);
            }
        }

        if (patternBuilder.length() <= 0) {
            return NEVER_MATCH;
        }

        patternBuilder.append(")");

        return Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
