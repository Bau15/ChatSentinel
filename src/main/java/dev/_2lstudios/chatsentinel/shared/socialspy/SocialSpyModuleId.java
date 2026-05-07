package dev._2lstudios.chatsentinel.shared.socialspy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SocialSpyModuleId {
    public static final String MESSAGES = "messages";
    public static final String SIGNS = "signs";
    public static final String BOOKS = "books";
    public static final String COMMANDS = "commands";

    private static final List<String> IDS = Collections.unmodifiableList(Arrays.asList(MESSAGES, SIGNS, BOOKS, COMMANDS));

    private SocialSpyModuleId() {
    }

    public static List<String> ids() {
        return IDS;
    }

    public static String normalize(final String input) {
        final String id = input == null ? "" : input.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if ("message".equals(id) || "msg".equals(id) || "dm".equals(id) || "pm".equals(id)
                || "private".equals(id) || "private-messages".equals(id)) {
            return MESSAGES;
        }
        if ("sign".equals(id)) {
            return SIGNS;
        }
        if ("book".equals(id)) {
            return BOOKS;
        }
        if ("command".equals(id) || "cmd".equals(id) || "cmds".equals(id)) {
            return COMMANDS;
        }
        return id;
    }

    public static boolean isValid(final String input) {
        return IDS.contains(normalize(input));
    }
}
