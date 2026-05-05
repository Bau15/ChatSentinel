package dev._2lstudios.chatsentinel.shared.moderation;

import java.util.Arrays;
import java.util.Objects;

public final class ModerationActionSettings {
    private final int maxWarns;
    private final String warnNotification;
    private final boolean webhookEnabled;
    private final String[] commands;

    public ModerationActionSettings(int maxWarns, String warnNotification, boolean webhookEnabled, String[] commands) {
        this.maxWarns = maxWarns;
        this.warnNotification = warnNotification == null ? "" : warnNotification;
        this.webhookEnabled = webhookEnabled;
        this.commands = commands == null ? new String[0] : commands.clone();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModerationActionSettings)) {
            return false;
        }
        ModerationActionSettings that = (ModerationActionSettings) o;
        return maxWarns == that.maxWarns
                && webhookEnabled == that.webhookEnabled
                && warnNotification.equals(that.warnNotification)
                && Arrays.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(maxWarns, warnNotification, webhookEnabled);
        result = 31 * result + Arrays.hashCode(commands);
        return result;
    }

    @Override
    public String toString() {
        return "ModerationActionSettings{" +
                "maxWarns=" + maxWarns +
                ", warnNotification='" + warnNotification + '\'' +
                ", webhookEnabled=" + webhookEnabled +
                ", commands=" + Arrays.toString(commands) +
                '}';
    }
}
