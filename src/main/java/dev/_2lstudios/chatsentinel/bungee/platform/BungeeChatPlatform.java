package dev._2lstudios.chatsentinel.bungee.platform;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.text.BungeeMessageSink;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class BungeeChatPlatform implements ChatPlatform {
    private final ChatSentinel plugin;
    private final ProxyServer proxy;
    private final BungeeMessageSink messageSink;

    public BungeeChatPlatform(final ChatSentinel plugin, final ProxyServer proxy, final BungeeMessageSink messageSink) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.messageSink = messageSink;
    }

    @Override
    public Collection<ChatUser> getOnlineUsers() {
        final List<ChatUser> users = new ArrayList<ChatUser>();
        for (ProxiedPlayer player : proxy.getPlayers()) {
            users.add(new BungeeChatUser(player, messageSink));
        }
        return users;
    }

    @Override
    public Optional<ChatUser> findUser(final UUID uniqueId) {
        final ProxiedPlayer player = proxy.getPlayer(uniqueId);
        return player == null ? Optional.<ChatUser>empty() : Optional.<ChatUser>of(new BungeeChatUser(player, messageSink));
    }

    @Override
    public void sendConsoleMessage(final String legacyMessage) {
        proxy.getConsole().sendMessage(legacyMessage);
    }

    @Override
    public void dispatchConsoleCommand(final String command) {
        proxy.getPluginManager().dispatchCommand(proxy.getConsole(), command);
    }

    @Override
    public void runAsync(final Runnable runnable) {
        proxy.getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public String getPlatformName() {
        return "BungeeCord";
    }
}
