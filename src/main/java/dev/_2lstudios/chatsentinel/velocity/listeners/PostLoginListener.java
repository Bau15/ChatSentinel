package dev._2lstudios.chatsentinel.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.velocity.platform.VelocityChatUser;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;

public class PostLoginListener {
    private final ChatSentinel plugin;
    private final GeneralModule generalModule;
    private final ChatPlayerManager chatPlayerManager;
    private final ChatNotificationManager chatNotificationManager;

    public PostLoginListener(ChatSentinel plugin, GeneralModule generalModule, ChatPlayerManager chatPlayerManager, ChatNotificationManager chatNotificationManager) {
        this.plugin = plugin;
        this.generalModule = generalModule;
        this.chatPlayerManager = chatPlayerManager;
        this.chatNotificationManager = chatNotificationManager;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(new VelocityChatUser(player, plugin.getMessageSink()));

        if (chatPlayer != null) {
            // Reset the locale of the player if already exists
            chatPlayer.setLocale(null);
            chatPlayer.markMovementGatePassed();

            // Set notifications
            if (player.hasPermission("chatsentinel.notify")) {
                chatNotificationManager.addPlayer(chatPlayer);
            }

            // Add the nickname
            generalModule.addNickname(player.getUsername());
        }
    }
}
