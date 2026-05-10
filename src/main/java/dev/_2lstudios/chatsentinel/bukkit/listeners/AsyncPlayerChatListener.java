package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.chat.ProcessedChatEvent;
import dev._2lstudios.chatsentinel.shared.modules.ChatSnapshotModule;
import dev._2lstudios.chatsentinel.bukkit.utils.FoliaAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class AsyncPlayerChatListener implements Listener {
	private ChatPlayerManager chatPlayerManager;
	private final ChatSentinel plugin;

	public AsyncPlayerChatListener(ChatSentinel plugin, ChatPlayerManager chatPlayerManager) {
		this.plugin = plugin;
		this.chatPlayerManager = chatPlayerManager;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		// Get player
		Player player = event.getPlayer();
		
		// Get event variables
		String message = event.getMessage();
		java.util.Collection<Player> recipents = event.getRecipients();
		
		// Do not check uncheckable commands
		if (message.startsWith("/") && !ChatSentinel.getInstance().getModuleManager().getGeneralModule().isCommand(message)) {
			return;
		}

// Get chat player
		BukkitChatUser chatUser = new BukkitChatUser(plugin, player, plugin.getMessageSink());
		ChatPlayer chatPlayer = chatPlayerManager.getPlayer(chatUser);

		// Process the event
		ProcessedChatEvent finalResult = plugin.getChatEventProcessor().process(chatUser, message, true);

		// Apply modifiers to event
		if (finalResult.isHide()) {
			event.setCancelled(true);
			chatUser.sendMessage(renderBukkitLine(event, player, finalResult.getMessage()));
		} else if (finalResult.isCancelled()) {
			event.setCancelled(true);
		} else {
			event.setMessage(finalResult.getMessage());
		}

		// Set last message
		if (!event.isCancelled()) {
			chatPlayer.addLastMessage(finalResult.getMessage(), System.currentTimeMillis());
			if (!message.startsWith("/")) {
				final String renderedLine = renderBukkitLine(event, player, finalResult.getMessage());
				final Optional<ChatSnapshotModule.Entry> entry = plugin.getModuleManager().getChatSnapshotModule().record(
						player.getUniqueId(), player.getName(), finalResult.getMessage(), renderedLine, recipientIds(recipents));
				if (entry.isPresent() && plugin.getModuleManager().getChatSnapshotModule().isLiveDeleteClickEnabled()) {
					event.setCancelled(true);
					sendLiveDeleteBroadcast(recipents, renderedLine, entry.get());
				}
			}
		}
	}

	private String renderBukkitLine(final AsyncPlayerChatEvent event, final Player player, final String message) {
		try {
			return String.format(event.getFormat(), player.getDisplayName(), message);
		} catch (RuntimeException exception) {
			return "<" + player.getName() + "> " + message;
		}
	}

	private void sendLiveDeleteBroadcast(final Collection<Player> recipients, final String renderedLine,
			final ChatSnapshotModule.Entry entry) {
		final ChatSnapshotModule snapshotModule = plugin.getModuleManager().getChatSnapshotModule();
		for (final Player recipient : recipients) {
			if (recipient == null) {
				continue;
			}
			FoliaAPI.runTaskForEntity(plugin, recipient, new Runnable() {
				@Override
				public void run() {
					if (!recipient.isOnline()) {
						return;
					}
					if (recipient.hasPermission(snapshotModule.getLiveDeletePermission())) {
						plugin.getMessageSink().sendClickablePrefixMessage(recipient, snapshotModule.getLiveDeletePrefix(),
								snapshotModule.getLiveDeleteHover(), snapshotModule.buildLiveDeleteCommand(entry.getId()),
								renderedLine);
						return;
					}
					plugin.getMessageSink().sendMessage(recipient, renderedLine);
				}
			}, null, 0L);
		}
	}

	private Collection<UUID> recipientIds(final Collection<Player> recipients) {
		Collection<UUID> ids = new ArrayList<UUID>();
		for (Player recipient : recipients) {
			ids.add(recipient.getUniqueId());
		}
		return ids;
	}
}
