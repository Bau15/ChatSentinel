package dev._2lstudios.chatsentinel.shared.alerts;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;

public final class AlertPayloadCodec {
	private static final String INSTANCE_ID = "instanceId";
	private static final String NOTIFICATION_MESSAGE = "notificationMessage";
	private static final String SPY_MESSAGE = "spyMessage";

	private AlertPayloadCodec() {
	}

	public static String encode(AlertPayload payload) {
		if (payload == null) {
			return "";
		}

		Properties properties = new Properties();
		properties.setProperty(INSTANCE_ID, payload.getInstanceId());
		properties.setProperty(NOTIFICATION_MESSAGE, payload.getNotificationMessage());
		properties.setProperty(SPY_MESSAGE, payload.getSpyMessage());

		try {
			StringWriter writer = new StringWriter();
			properties.store(writer, null);
			return writer.toString();
		} catch (IOException ignored) {
			return "";
		}
	}

	public static Optional<AlertPayload> decode(String encoded) {
		if (encoded == null || encoded.trim().isEmpty()) {
			return Optional.empty();
		}

		Properties properties = new Properties();
		try {
			properties.load(new StringReader(encoded));
		} catch (IOException ignored) {
			return Optional.empty();
		}

		AlertPayload payload = new AlertPayload(
				properties.getProperty(INSTANCE_ID, ""),
				properties.getProperty(NOTIFICATION_MESSAGE, ""),
				properties.getProperty(SPY_MESSAGE, ""));
		return payload.hasAlert() ? Optional.of(payload) : Optional.<AlertPayload>empty();
	}
}
