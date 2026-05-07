package dev._2lstudios.chatsentinel.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.LegacyText;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import dev._2lstudios.chatsentinel.velocity.text.VelocityMessageSink;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class VelocityChatPlatform implements ChatPlatform {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final ChatSentinel plugin;
    private final ProxyServer server;
    private final VelocityMessageSink messageSink;

    public VelocityChatPlatform(final ChatSentinel plugin, final ProxyServer server, final VelocityMessageSink messageSink) {
        this.plugin = plugin;
        this.server = server;
        this.messageSink = messageSink;
    }

    @Override
    public Collection<ChatUser> getOnlineUsers() {
        final List<ChatUser> users = new ArrayList<ChatUser>();
        for (Player player : server.getAllPlayers()) {
            users.add(new VelocityChatUser(player, messageSink));
        }
        return users;
    }

    @Override
    public Optional<ChatUser> findUser(final UUID uniqueId) {
        final Optional<Player> player = server.getPlayer(uniqueId);
        return player.isPresent()
                ? Optional.<ChatUser>of(new VelocityChatUser(player.get(), messageSink))
                : Optional.<ChatUser>empty();
    }

    @Override
    public void sendConsoleMessage(final String legacyMessage) {
        for (final String line : LegacyText.toSectionLines(legacyMessage)) {
            server.getConsoleCommandSource().sendMessage(LEGACY_SERIALIZER.deserialize(line));
        }
    }

    @Override
    public void dispatchConsoleCommand(final String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public void runAsync(final Runnable runnable) {
        server.getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public String getPlatformName() {
        return "Velocity";
    }

    @Override
    public void refreshOnlinePlayers(final ChatPlayerManager chatPlayerManager,
            final ChatNotificationManager chatNotificationManager,
            final GeneralModule generalModule) {
        for (Player player : server.getAllPlayers()) {
            final VelocityChatUser user = new VelocityChatUser(player, messageSink);
            final ChatPlayer chatPlayer = chatPlayerManager.getPlayer(user);
            chatPlayer.setLocale(null);
            chatPlayer.markMovementGatePassed();
            if (player.hasPermission("chatsentinel.notify")) {
                chatNotificationManager.addPlayer(chatPlayer);
            } else if (chatNotificationManager.containsPlayer(chatPlayer)) {
                chatNotificationManager.removePlayer(chatPlayer);
            }
            generalModule.addNickname(player.getUsername());
        }
        generalModule.compileNicknamesPattern();
    }
}
