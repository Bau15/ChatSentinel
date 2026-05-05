package dev._2lstudios.chatsentinel.bukkit.platform;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.text.BukkitMessageSink;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.platform.CommandActor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BukkitCommandActor implements CommandActor {
    private final ChatSentinel plugin;
    private final CommandSender sender;
    private final BukkitMessageSink messageSink;

    public BukkitCommandActor(final ChatSentinel plugin, final CommandSender sender, final BukkitMessageSink messageSink) {
        this.plugin = plugin;
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
        return sender instanceof Player;
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
        if (!(sender instanceof Player)) {
            return null;
        }
        return new BukkitChatUser(plugin, (Player) sender, messageSink);
    }
}
