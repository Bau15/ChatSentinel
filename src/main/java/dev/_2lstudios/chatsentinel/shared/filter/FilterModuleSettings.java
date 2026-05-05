package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Arrays;
import java.util.Objects;

public final class FilterModuleSettings {
    private final String moduleId;
    private final boolean enabled;
    private final String customName;
    private final int maxWarns;
    private final String warnNotification;
    private final boolean webhookEnabled;
    private final String[] commands;
    private final boolean fakeMessage;
    private final boolean censorshipEnabled;
    private final String censorshipReplacement;
    private final boolean blockRawMessage;

    public FilterModuleSettings(String moduleId, boolean enabled, String customName, int maxWarns, String warnNotification,
                                boolean webhookEnabled, String[] commands, boolean fakeMessage, boolean censorshipEnabled,
                                String censorshipReplacement, boolean blockRawMessage) {
        this.moduleId = normalizeModuleId(Objects.requireNonNull(moduleId, "moduleId"));
        this.enabled = enabled;
        this.customName = defaultString(customName);
        this.maxWarns = maxWarns;
        this.warnNotification = defaultString(warnNotification);
        this.webhookEnabled = webhookEnabled;
        this.commands = commands == null ? new String[0] : commands.clone();
        this.fakeMessage = fakeMessage;
        this.censorshipEnabled = censorshipEnabled;
        this.censorshipReplacement = defaultString(censorshipReplacement);
        this.blockRawMessage = blockRawMessage;
    }

    public static FilterModuleSettings defaultBlacklist(String moduleId, boolean enabled, String customName, boolean fakeMessage,
                                                        boolean censorshipEnabled, String censorshipReplacement, int maxWarns,
                                                        String warnNotification, boolean webhookEnabled, String[] commands,
                                                        boolean blockRawMessage) {
        return new FilterModuleSettings(moduleId, enabled, customName, maxWarns, warnNotification, webhookEnabled, commands,
                fakeMessage, censorshipEnabled, censorshipReplacement, blockRawMessage);
    }

    public static FilterModuleSettings disabled(String moduleId) {
        return new FilterModuleSettings(moduleId, false, "", 0, "", false, new String[0], false, false, "", false);
    }

    public String getModuleId() {
        return moduleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCustomName() {
        return customName;
    }

    public int getMaxWarns() {
        return maxWarns;
    }

    public String getWarnNotification() {
        return warnNotification;
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public String[] getCommands() {
        return commands.clone();
    }

    public boolean isFakeMessage() {
        return fakeMessage;
    }

    public boolean isCensorshipEnabled() {
        return censorshipEnabled;
    }

    public String getCensorshipReplacement() {
        return censorshipReplacement;
    }

    public boolean isBlockRawMessage() {
        return blockRawMessage;
    }

    private static String normalizeModuleId(String moduleId) {
        return moduleId.isEmpty() ? "default" : moduleId;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterModuleSettings)) {
            return false;
        }
        FilterModuleSettings that = (FilterModuleSettings) o;
        return enabled == that.enabled
                && maxWarns == that.maxWarns
                && webhookEnabled == that.webhookEnabled
                && fakeMessage == that.fakeMessage
                && censorshipEnabled == that.censorshipEnabled
                && blockRawMessage == that.blockRawMessage
                && moduleId.equals(that.moduleId)
                && customName.equals(that.customName)
                && warnNotification.equals(that.warnNotification)
                && Arrays.equals(commands, that.commands)
                && censorshipReplacement.equals(that.censorshipReplacement);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(moduleId, enabled, customName, maxWarns, warnNotification, webhookEnabled, fakeMessage,
                censorshipEnabled, censorshipReplacement, blockRawMessage);
        result = 31 * result + Arrays.hashCode(commands);
        return result;
    }

    @Override
    public String toString() {
        return "FilterModuleSettings{" +
                "moduleId='" + moduleId + '\'' +
                ", enabled=" + enabled +
                ", customName='" + customName + '\'' +
                ", maxWarns=" + maxWarns +
                ", warnNotification='" + warnNotification + '\'' +
                ", webhookEnabled=" + webhookEnabled +
                ", commands=" + Arrays.toString(commands) +
                ", fakeMessage=" + fakeMessage +
                ", censorshipEnabled=" + censorshipEnabled +
                ", censorshipReplacement='" + censorshipReplacement + '\'' +
                ", blockRawMessage=" + blockRawMessage +
                '}';
    }
}
