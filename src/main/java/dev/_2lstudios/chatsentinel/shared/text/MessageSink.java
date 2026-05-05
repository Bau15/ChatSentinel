package dev._2lstudios.chatsentinel.shared.text;

public interface MessageSink<P> {
	void sendMessage(P player, String legacyMessage);

	void sendActionBar(P player, String legacyMessage);

	void sendWarning(P player, String legacyMessage, WarningDeliverySettings settings);
}
