package dev._2lstudios.chatsentinel.bungee.platform;

import dev._2lstudios.chatsentinel.bungee.text.BungeeMessageSink;
import dev._2lstudios.chatsentinel.bungee.utils.BungeeLocaleUtil;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public final class BungeeChatUser implements ChatUser {
    private final ProxiedPlayer player;
    private final BungeeMessageSink messageSink;

    public BungeeChatUser(final ProxiedPlayer player, final BungeeMessageSink messageSink) {
        this.player = player;
        this.messageSink = messageSink;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public String getLocale() {
        return BungeeLocaleUtil.getLocale(player);
    }

    @Override
    public String getServerName() {
        return player.getServer() != null ? player.getServer().getInfo().getName() : "";
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
