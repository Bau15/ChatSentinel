package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.chat.ProcessedChatEvent;

public class ServerCommandListener implements Listener {
	private ChatPlayerManager chatPlayerManager;

	public ServerCommandListener(ChatPlayerManager chatPlayerManager, dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager chatNotificationManager) {
		this.chatPlayerManager = chatPlayerManager;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onServerCommand(PlayerCommandPreprocessEvent event) {
		// Get player
		Player player = event.getPlayer();

		// Check if player has bypass
		if (player.hasPermission("chatsentinel.bypass")) {
			return;
		}
		
		// Get event variables
		String message = event.getMessage();
		// Get chat player
		BukkitChatUser chatUser = new BukkitChatUser(ChatSentinel.getInstance(), player, ChatSentinel.getInstance().getMessageSink());
		ChatPlayer chatPlayer = chatPlayerManager.getPlayer(chatUser);

		// Process the event
		ProcessedChatEvent finalResult = ChatSentinel.getInstance().getChatEventProcessor().process(chatUser, message, true);

		// Apply modifiers to event
		if (finalResult.isCancelled()) {
			event.setCancelled(true);
		} else {
			event.setMessage(finalResult.getMessage());
		}

		// Set last message
		if (!event.isCancelled()) {
			chatPlayer.addLastCommand(System.currentTimeMillis());
		}
	}
}
