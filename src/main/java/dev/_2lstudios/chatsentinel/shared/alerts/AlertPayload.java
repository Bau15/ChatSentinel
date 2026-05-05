package dev._2lstudios.chatsentinel.shared.alerts;

public final class AlertPayload {
	private final String instanceId;
	private final String notificationMessage;
	private final String spyMessage;

	public AlertPayload(String instanceId, String notificationMessage, String spyMessage) {
		this.instanceId = clean(instanceId);
		this.notificationMessage = clean(notificationMessage);
		this.spyMessage = clean(spyMessage);
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getNotificationMessage() {
		return notificationMessage;
	}

	public String getSpyMessage() {
		return spyMessage;
	}

	public boolean hasAlert() {
		return !notificationMessage.isEmpty() || !spyMessage.isEmpty();
	}

	private static String clean(String value) {
		return value == null ? "" : value;
	}
}
