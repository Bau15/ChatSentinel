package dev._2lstudios.chatsentinel.shared.socialspy;

public final class SocialSpyCommandSpyResult {
    private final boolean spy;
    private final String moduleId;
    private final String commandRoot;
    private final String target;
    private final String content;
    private final String fullCommandWithoutSlash;

    private SocialSpyCommandSpyResult(final boolean spy, final String moduleId, final String commandRoot,
            final String target, final String content, final String fullCommandWithoutSlash) {
        this.spy = spy;
        this.moduleId = safe(moduleId);
        this.commandRoot = safe(commandRoot);
        this.target = safe(target);
        this.content = safe(content);
        this.fullCommandWithoutSlash = safe(fullCommandWithoutSlash);
    }

    public static SocialSpyCommandSpyResult none(final String fullCommandWithoutSlash) {
        return new SocialSpyCommandSpyResult(false, "", "", "", "", fullCommandWithoutSlash);
    }

    public static SocialSpyCommandSpyResult privateMessage(final String commandRoot, final String target,
            final String content, final String fullCommandWithoutSlash) {
        return new SocialSpyCommandSpyResult(true, SocialSpyModuleId.MESSAGES, commandRoot, target, content,
                fullCommandWithoutSlash);
    }

    public static SocialSpyCommandSpyResult generalCommand(final String commandRoot, final String fullCommandWithoutSlash) {
        return new SocialSpyCommandSpyResult(true, SocialSpyModuleId.COMMANDS, commandRoot, "", "",
                fullCommandWithoutSlash);
    }

    public boolean isSpy() {
        return spy;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getCommandRoot() {
        return commandRoot;
    }

    public String getTarget() {
        return target;
    }

    public String getContent() {
        return content;
    }

    public String getFullCommandWithoutSlash() {
        return fullCommandWithoutSlash;
    }

    private static String safe(final String value) {
        return value == null ? "" : value;
    }
}
