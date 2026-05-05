package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import dev._2lstudios.chatsentinel.shared.modules.NoMoveChatModule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final ChatPlayerManager chatPlayerManager;
    private final NoMoveChatModule noMoveChatModule;

    public PlayerMoveListener(ChatPlayerManager chatPlayerManager, NoMoveChatModule noMoveChatModule) {
        this.chatPlayerManager = chatPlayerManager;
        this.noMoveChatModule = noMoveChatModule;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getPlayer(new BukkitChatUser(ChatSentinel.getInstance(), player,
                ChatSentinel.getInstance().getMessageSink()));
        chatPlayer.observeMovement(to.getWorld() == null ? "" : to.getWorld().getName(), to.getX(), to.getY(), to.getZ(),
                noMoveChatModule.getMinDistanceSquared());
    }
}
