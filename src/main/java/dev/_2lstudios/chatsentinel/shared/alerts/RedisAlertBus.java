package dev._2lstudios.chatsentinel.shared.alerts;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RedisAlertBus implements AlertBus {
	private static final long FAILURE_LOG_COOLDOWN_MILLIS = 60000L;

	private final URI uri;
	private final String channel;
	private final String instanceId;
	private final boolean publishAlerts;
	private final boolean receiveAlerts;
	private final Consumer<AlertPayload> remoteAlertHandler;
	private final Logger logger;
	private final ExecutorService listenerExecutor;
	private final ExecutorService publisherExecutor;
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final AtomicLong nextFailureLogTime = new AtomicLong(0L);
	private volatile Jedis subscriber;
	private volatile JedisPubSub subscription;

	public RedisAlertBus(String uri, String channel, String instanceId, boolean publishAlerts, boolean receiveAlerts,
			Consumer<AlertPayload> remoteAlertHandler, Logger logger) {
		this.uri = URI.create(uri);
		this.channel = channel;
		this.instanceId = instanceId;
		this.publishAlerts = publishAlerts;
		this.receiveAlerts = receiveAlerts;
		this.remoteAlertHandler = remoteAlertHandler;
		this.logger = logger;
		this.listenerExecutor = createExecutor();
		this.publisherExecutor = createExecutor();

		if (receiveAlerts) {
			listenerExecutor.execute(this::listen);
		}
	}

	@Override
	public void publish(AlertPayload payload) {
		if (!publishAlerts || payload == null || !payload.hasAlert() || closed.get()) {
			return;
		}
		final AlertPayload outboundPayload = payload.getInstanceId().isEmpty()
				? new AlertPayload(instanceId, payload.getNotificationMessage(), payload.getSpyMessage())
				: payload;

		publisherExecutor.execute(() -> {
			try (Jedis jedis = new Jedis(uri)) {
				jedis.publish(channel, AlertPayloadCodec.encode(outboundPayload));
			} catch (RuntimeException exception) {
				logFailure("Redis alert publish failed; local alerts continue.", exception);
			}
		});
	}

	@Override
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		JedisPubSub activeSubscription = subscription;
		if (activeSubscription != null) {
			activeSubscription.unsubscribe();
		}

		Jedis activeSubscriber = subscriber;
		if (activeSubscriber != null) {
			activeSubscriber.close();
		}

		listenerExecutor.shutdownNow();
		publisherExecutor.shutdownNow();
	}

	private void listen() {
		while (!closed.get()) {
			try (Jedis jedis = new Jedis(uri)) {
				subscriber = jedis;
				subscription = new JedisPubSub() {
					@Override
					public void onMessage(String subscribedChannel, String message) {
						if (!channel.equals(subscribedChannel)) {
							return;
						}

						Optional<AlertPayload> decoded = AlertPayloadCodec.decode(message);
						if (!decoded.isPresent() || instanceId.equals(decoded.get().getInstanceId())) {
							return;
						}

						remoteAlertHandler.accept(decoded.get());
					}
				};
				jedis.subscribe(subscription, channel);
			} catch (RuntimeException exception) {
				if (!closed.get()) {
					logFailure("Redis alert listener failed; local alerts continue.", exception);
					pauseBeforeReconnect();
				}
			} finally {
				subscriber = null;
				subscription = null;
			}
		}
	}

	private void pauseBeforeReconnect() {
		try {
			TimeUnit.SECONDS.sleep(5L);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
	}

	private void logFailure(String message, RuntimeException exception) {
		long now = System.currentTimeMillis();
		long nextLog = nextFailureLogTime.get();
		if (now < nextLog || !nextFailureLogTime.compareAndSet(nextLog, now + FAILURE_LOG_COOLDOWN_MILLIS)) {
			return;
		}

		logger.log(Level.WARNING, message, exception);
	}

	private static ExecutorService createExecutor() {
		return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(256), new ThreadPoolExecutor.DiscardPolicy());
	}
}
