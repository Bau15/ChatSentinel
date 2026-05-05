package dev._2lstudios.chatsentinel.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import dev._2lstudios.chatsentinel.velocity.text.VelocityMessageSink;

import java.util.UUID;

public final class VelocityChatUser implements ChatUser {
    private final Player player;
    private final VelocityMessageSink messageSink;

    public VelocityChatUser(final Player player, final VelocityMessageSink messageSink) {
        this.player = player;
        this.messageSink = messageSink;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public String getLocale() {
        return "en";
    }

    @Override
    public String getServerName() {
        return player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "";
    }

    @Override
    public boolean hasPermission(final String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String legacyMessage) {
        messageSink.sendMessage(player, legacyMessage);
    }

    @Override
    public void sendWarning(final String legacyMessage, final WarningDeliverySettings settings) {
        messageSink.sendWarning(player, legacyMessage, settings);
    }
}
