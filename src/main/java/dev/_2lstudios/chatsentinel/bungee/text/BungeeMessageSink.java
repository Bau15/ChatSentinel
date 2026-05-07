package dev._2lstudios.chatsentinel.bungee.text;

import java.util.logging.Logger;

import dev._2lstudios.chatsentinel.shared.text.LegacyText;
import dev._2lstudios.chatsentinel.shared.text.MessageSink;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeMessageSink implements MessageSink<ProxiedPlayer> {
	private final Logger logger;
	private boolean actionBarWarningLogged;

	public BungeeMessageSink(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void sendMessage(ProxiedPlayer player, String legacyMessage) {
		if (player == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		for (final String line : LegacyText.toSectionLines(legacyMessage)) {
			player.sendMessage(TextComponent.fromLegacyText(line));
		}
	}

	@Override
	public void sendActionBar(ProxiedPlayer player, String legacyMessage) {
		if (player == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		sendActionBarSafely(player, LegacyText.toSection(legacyMessage).replace('\n', ' '));
	}

	@Override
	public void sendWarning(ProxiedPlayer player, String legacyMessage, WarningDeliverySettings settings) {
		if (settings == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		final String sectionMessage = LegacyText.toSection(legacyMessage);
		if (settings.isMessageEnabled()) {
			sendMessage(player, sectionMessage);
		}
		if (settings.isActionBarEnabled() && !sendActionBarSafely(player, sectionMessage.replace('\n', ' ')) && !settings.isMessageEnabled()) {
			logActionBarUnsupported();
		}
	}

	private boolean sendActionBarSafely(ProxiedPlayer player, String legacyMessage) {
		try {
			player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(legacyMessage));
			return true;
		} catch (NoClassDefFoundError | NoSuchMethodError | UnsupportedOperationException exception) {
			return false;
		}
	}

	private void logActionBarUnsupported() {
		if (!actionBarWarningLogged && logger != null) {
			actionBarWarningLogged = true;
			logger.warning("Warning action-bar delivery is unavailable on this BungeeCord proxy; warning chat fallback is disabled.");
		}
	}
}
