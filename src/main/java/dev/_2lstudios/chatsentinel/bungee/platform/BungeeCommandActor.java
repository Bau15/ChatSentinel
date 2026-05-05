package dev._2lstudios.chatsentinel.bungee.platform;

import dev._2lstudios.chatsentinel.bungee.text.BungeeMessageSink;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.platform.CommandActor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeCommandActor implements CommandActor {
    private final CommandSender sender;
    private final BungeeMessageSink messageSink;

    public BungeeCommandActor(final CommandSender sender, final BungeeMessageSink messageSink) {
        this.sender = sender;
        this.messageSink = messageSink;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public String getLocale() {
        final ChatUser user = asUserOrNull();
        return user == null ? "en" : user.getLocale();
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String legacyMessage) {
        final ChatUser user = asUserOrNull();
        if (user != null) {
            user.sendMessage(legacyMessage);
            return;
        }
        sender.sendMessage(legacyMessage);
    }

    @Override
    public ChatUser asUserOrNull() {
        if (!(sender instanceof ProxiedPlayer)) {
            return null;
        }
        return new BungeeChatUser((ProxiedPlayer) sender, messageSink);
    }
}
