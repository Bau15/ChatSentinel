package dev._2lstudios.chatsentinel.bungee.listeners;

import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
    private GeneralModule generalModule;
    private ChatPlayerManager chatPlayerManager;

    public PostLoginListener(GeneralModule generalModule, ChatPlayerManager chatPlayerManager) {
        this.generalModule = generalModule;
        this.chatPlayerManager = chatPlayerManager;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ChatPlayer chatPlayer = chatPlayerManager.getPlayerOrCreate(event.getPlayer());

        if (chatPlayer != null) {
            // Reset the locale of the player if already exists
            chatPlayer.setLocale(null);

            // Add the nickname
            generalModule.addNickname(event.getPlayer().getName());
        }
    }
}
