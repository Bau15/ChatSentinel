package dev._2lstudios.chatsentinel.bungee.platform;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.text.BungeeMessageSink;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.LegacyText;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
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
        for (final String line : LegacyText.toSectionLines(legacyMessage)) {
            proxy.getConsole().sendMessage(TextComponent.fromLegacyText(line));
        }
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

    @Override
    public void refreshOnlinePlayers(final ChatPlayerManager chatPlayerManager,
            final ChatNotificationManager chatNotificationManager,
            final GeneralModule generalModule) {
        for (ProxiedPlayer player : proxy.getPlayers()) {
            final BungeeChatUser user = new BungeeChatUser(player, messageSink);
            final ChatPlayer chatPlayer = chatPlayerManager.getPlayer(user);
            chatPlayer.setLocale(null);
            chatPlayer.markMovementGatePassed();
            if (player.hasPermission("chatsentinel.notify")) {
                chatNotificationManager.addPlayer(chatPlayer);
            } else if (chatNotificationManager.containsPlayer(chatPlayer)) {
                chatNotificationManager.removePlayer(chatPlayer);
            }
            generalModule.addNickname(player.getName());
        }
        generalModule.compileNicknamesPattern();
    }
}
