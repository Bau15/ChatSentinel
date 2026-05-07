package dev._2lstudios.chatsentinel.shared.chat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev._2lstudios.chatsentinel.shared.modules.ModerationModule;

public class ChatPlayer {
    private final int historySize = 3;
    private final UUID uuid;
    private final Map<String, Integer> warns;
    private final Deque<String> lastMessages;
    private String locale = null;
    private long lastMessageTime;
    private long lastCommandTime;
    private boolean notify = false;
    private boolean movedSinceJoin;
    private boolean movementGatePassed;
    private boolean movementGateHasOrigin;
    private String movementGateWorld;
    private double movementGateX;
    private double movementGateY;
    private double movementGateZ;
    private boolean spy;
	private boolean correctionEnabled = true;

    public ChatPlayer(UUID uuid) {
        this.uuid = uuid;
        this.warns = new HashMap<>();
        this.lastMessages = new ArrayDeque<>(historySize);
        this.lastMessageTime = 0;
        this.lastCommandTime = 0;
    }

    public synchronized int getWarns(ModerationModule moderationModule) {
        return getWarns(moderationModule.getIdentityKey());
    }

    public synchronized int addWarn(ModerationModule moderationModule) {
        return addWarn(moderationModule.getIdentityKey());
    }

    public synchronized int getWarns(String identityKey) {
        return this.warns.getOrDefault(identityKey, 0);
    }

    public synchronized int addWarn(String identityKey) {
        int warns = this.warns.getOrDefault(identityKey, 0) + 1;

        this.warns.put(identityKey, warns);

        return warns;
    }

    public synchronized void clearWarns(String identityKey) {
        this.warns.remove(identityKey);
    }

    public String removeDigits(String str) {
        StringBuilder result = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            char character = str.charAt(i);
            if (!Character.isDigit(character)) {
                result.append(character);
            }
        }

        return result.toString();
    }

    public synchronized boolean isLastMessage(String message) {
        // Check if message is null
        if (message != null) {
            // Remove digits from message
            message = removeDigits(message);

            // Get the length of the message
            int length = message.length();

            // Iterate over last messages
            for (String lastMessage : lastMessages) {
                // Check if equals the last message
                if (message.equals(lastMessage)) {
                    return true;
                }
                // Check if equals last message length
                if (length > 16 && length == lastMessage.length()) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized long getLastMessageTime() {
        return this.lastMessageTime;
    }

    public synchronized long getLastCommandTime() {
        return this.lastCommandTime;
    }

    public synchronized void addLastMessage(String lastMessage, long lastMessageTime) {
        if (lastMessages.size() >= historySize) {
            lastMessages.removeLast();
        }
        lastMessages.offerFirst(removeDigits(lastMessage));
        this.lastMessageTime = lastMessageTime;
    }

    public synchronized void addLastCommand(long lastCommandTime) {
        this.lastCommandTime = lastCommandTime;
    }

    public synchronized void clearWarns() {
        this.warns.clear();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public synchronized String getLocale() {
        return hasLocale() ? locale : "en";
    }

    public synchronized void setLocale(String locale) {
        this.locale = locale;
    }

    public synchronized boolean hasLocale() {
        return this.locale != null;
    }

    public synchronized void setNotify(boolean notify) {
        this.notify = notify;
    }

    public synchronized boolean isNotify() {
        return notify;
    }

    public synchronized boolean isMovedSinceJoin() {
        return movedSinceJoin;
    }

    public synchronized void setMovedSinceJoin(boolean movedSinceJoin) {
        if (movedSinceJoin) {
            markMovementGatePassed();
            return;
        }
        this.movedSinceJoin = false;
        this.movementGatePassed = false;
    }

    public synchronized void resetMovementGate(final String world, final double x, final double y, final double z) {
        this.movementGateWorld = world == null ? "" : world;
        this.movementGateX = x;
        this.movementGateY = y;
        this.movementGateZ = z;
        this.movementGateHasOrigin = true;
        this.movementGatePassed = false;
        this.movedSinceJoin = false;
    }

    public synchronized void markMovementGatePassed() {
        this.movementGatePassed = true;
        this.movedSinceJoin = true;
    }

    public synchronized boolean observeMovement(final String world, final double x, final double y, final double z,
            final double minDistanceSquared) {
        if (movementGatePassed) {
            return true;
        }
        if (!movementGateHasOrigin) {
            resetMovementGate(world, x, y, z);
            return false;
        }
        final String currentWorld = world == null ? "" : world;
        if (!currentWorld.equals(movementGateWorld)) {
            markMovementGatePassed();
            return true;
        }
        final double dx = x - movementGateX;
        final double dy = y - movementGateY;
        final double dz = z - movementGateZ;
        if ((dx * dx) + (dy * dy) + (dz * dz) >= minDistanceSquared) {
            markMovementGatePassed();
            return true;
        }
        return false;
    }

    public synchronized boolean hasMovementGatePassed() {
        return movementGatePassed || movedSinceJoin;
    }

public synchronized boolean isSpy() {
		return spy;
	}

	public synchronized void setSpy(boolean spy) {
		this.spy = spy;
	}

	public synchronized boolean isCorrectionEnabled() {
		return correctionEnabled;
	}

	public synchronized void setCorrectionEnabled(boolean correctionEnabled) {
		this.correctionEnabled = correctionEnabled;
	}

	public synchronized boolean toggleCorrectionEnabled() {
		this.correctionEnabled = !this.correctionEnabled;
		return this.correctionEnabled;
	}
}
