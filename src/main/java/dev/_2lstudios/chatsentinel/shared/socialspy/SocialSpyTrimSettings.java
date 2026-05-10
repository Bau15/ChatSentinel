package dev._2lstudios.chatsentinel.shared.socialspy;

public final class SocialSpyTrimSettings {
    private static final int MAX_LENGTH = 1024;

    private final int commandContentChars;
    private final int messageContentChars;
    private final int signLineChars;
    private final int bookTitleChars;
    private final int bookContentChars;
    private final int anvilNameChars;
    private final String appendEllipsis;

    public SocialSpyTrimSettings(final int commandContentChars, final int messageContentChars, final int signLineChars,
            final int bookTitleChars, final int bookContentChars, final int anvilNameChars, final String appendEllipsis) {
        this.commandContentChars = clamp(commandContentChars);
        this.messageContentChars = clamp(messageContentChars);
        this.signLineChars = clamp(signLineChars);
        this.bookTitleChars = clamp(bookTitleChars);
        this.bookContentChars = clamp(bookContentChars);
        this.anvilNameChars = clamp(anvilNameChars);
        this.appendEllipsis = appendEllipsis == null || appendEllipsis.isEmpty() ? "..." : appendEllipsis;
    }

    public int getCommandContentChars() {
        return commandContentChars;
    }

    public int getMessageContentChars() {
        return messageContentChars;
    }

    public int getSignLineChars() {
        return signLineChars;
    }

    public int getBookTitleChars() {
        return bookTitleChars;
    }

    public int getBookContentChars() {
        return bookContentChars;
    }

    public int getAnvilNameChars() {
        return anvilNameChars;
    }

    public String getAppendEllipsis() {
        return appendEllipsis;
    }

    public String trim(final String input, final int maxChars) {
        final String value = input == null ? "" : input;
        final int limit = clamp(maxChars);
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + appendEllipsis;
    }

    private static int clamp(final int value) {
        return Math.max(1, Math.min(MAX_LENGTH, value));
    }
}
