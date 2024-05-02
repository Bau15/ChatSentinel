package dev._2lstudios.chatsentinel.bukkit.listeners;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;

public class AsyncPlayerChatListener implements Listener {
	private ChatPlayerManager chatPlayerManager;

	public AsyncPlayerChatListener(ChatPlayerManager chatPlayerManager) {
		this.chatPlayerManager = chatPlayerManager;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		// Get player
		Player player = event.getPlayer();

		// Check if player has bypass
		if (player.hasPermission("chatsentinel.bypass")) {
			return;
		}
		
		// Get event variables
		String message = event.getMessage();
		Collection<Player> recipents = event.getRecipients();
		
		// Do not check uncheckable commands
		if (message.startsWith("/") && !ChatSentinel.getInstance().getModuleManager().getGeneralModule().isCommand(message)) {
			return;
		}

		// Get chat player
		ChatPlayer chatPlayer = chatPlayerManager.getPlayer(player);

		// Process the event
		ChatEventResult finalResult = ChatSentinel.getInstance().processEvent(chatPlayer, player, message);

		// Apply modifiers to event
		if (finalResult.isHide()) {
			recipents.removeIf(player1 -> player1 != player);
		} else if (finalResult.isCancelled()) {
			event.setCancelled(true);
		} else {
			event.setMessage(finalResult.getMessage());
		}

		// Set last message
		if (!event.isCancelled()) {
			chatPlayer.addLastMessage(finalResult.getMessage(), System.currentTimeMillis());
		}
	}
}
