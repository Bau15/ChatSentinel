package dev._2lstudios.chatsentinel.shared.socialspy;

import java.util.Locale;

public final class SocialSpyCommandDefinition {
    private final String commandRoot;
    private final int targetArgumentIndex;
    private final int messageStartArgumentIndex;

    public SocialSpyCommandDefinition(final String commandRoot, final int targetArgumentIndex,
            final int messageStartArgumentIndex) {
        if (commandRoot == null || commandRoot.trim().isEmpty()) {
            throw new IllegalArgumentException("commandRoot must not be empty");
        }
        this.commandRoot = commandRoot.trim().toLowerCase(Locale.ROOT);
        this.targetArgumentIndex = targetArgumentIndex;
        this.messageStartArgumentIndex = messageStartArgumentIndex;
    }

    public String getCommandRoot() {
        return commandRoot;
    }

    public int getTargetArgumentIndex() {
        return targetArgumentIndex;
    }

    public int getMessageStartArgumentIndex() {
        return messageStartArgumentIndex;
    }
}
