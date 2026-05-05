package dev._2lstudios.chatsentinel.shared.text;

public final class WarningDeliverySettings {
	private final boolean messageEnabled;
	private final boolean actionBarEnabled;

	public WarningDeliverySettings(boolean messageEnabled, boolean actionBarEnabled) {
		this.messageEnabled = messageEnabled;
		this.actionBarEnabled = actionBarEnabled;
	}

	public boolean isMessageEnabled() {
		return messageEnabled;
	}

	public boolean isActionBarEnabled() {
		return actionBarEnabled;
	}
}
