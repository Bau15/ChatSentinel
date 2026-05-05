package dev._2lstudios.chatsentinel.shared.alerts;

public interface AlertBus extends AutoCloseable {
	void publish(AlertPayload payload);

	@Override
	void close();
}
