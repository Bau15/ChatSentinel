package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PlayerTeleportListener implements Listener {
    private final ChatPlayerManager chatPlayerManager;

    public PlayerTeleportListener(ChatPlayerManager chatPlayerManager) {
        this.chatPlayerManager = chatPlayerManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!ChatSentinel.getInstance().getModuleManager().getNoMoveChatModule().isAllowTeleport()) {
            return;
        }
        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(new BukkitChatUser(ChatSentinel.getInstance(), player,
                ChatSentinel.getInstance().getMessageSink()));
        chatPlayer.markMovementGatePassed();
    }
}
