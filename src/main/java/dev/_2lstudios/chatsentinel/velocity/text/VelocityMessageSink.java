package dev._2lstudios.chatsentinel.velocity.text;

import com.velocitypowered.api.proxy.Player;

import dev._2lstudios.chatsentinel.shared.text.MessageSink;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityMessageSink implements MessageSink<Player> {
	private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

	@Override
	public void sendMessage(Player player, String legacyMessage) {
		if (player == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		player.sendMessage(deserialize(legacyMessage));
	}

	@Override
	public void sendActionBar(Player player, String legacyMessage) {
		if (player == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		player.sendActionBar(deserialize(legacyMessage));
	}

	@Override
	public void sendWarning(Player player, String legacyMessage, WarningDeliverySettings settings) {
		if (player == null || settings == null || legacyMessage == null || legacyMessage.isEmpty()) {
			return;
		}
		Component component = deserialize(legacyMessage);
		if (settings.isMessageEnabled()) {
			player.sendMessage(component);
		}
		if (settings.isActionBarEnabled()) {
			player.sendActionBar(component);
		}
	}

	private Component deserialize(String legacyMessage) {
		return LEGACY_SERIALIZER.deserialize(legacyMessage);
	}
}
