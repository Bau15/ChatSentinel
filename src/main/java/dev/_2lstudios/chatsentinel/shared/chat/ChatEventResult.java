package dev._2lstudios.chatsentinel.shared.chat;

import java.util.Objects;
import java.util.Optional;

import dev._2lstudios.chatsentinel.shared.moderation.ModerationViolation;

public class ChatEventResult {
    private String message;
    private boolean cancelled;
    private boolean hide;
    private boolean notify = true;
    private Optional<ModerationViolation> violation;
    private Optional<String> playerMessage;

    public ChatEventResult(String message, boolean cancelled, boolean hide) {
        this.message = message;
        this.cancelled = cancelled;
        this.hide = hide;
        this.violation = Optional.empty();
        this.playerMessage = Optional.empty();
    }

    public ChatEventResult(String message, boolean cancelled) {
        this(message, cancelled, false);
    }

    public Optional<String> getPlayerMessage() {
        return playerMessage;
    }

    public void setPlayerMessage(final String playerMessage) {
        this.playerMessage = Optional.of(Objects.requireNonNull(playerMessage, "playerMessage"));
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(final boolean notify) {
        this.notify = notify;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public Optional<ModerationViolation> getViolation() {
        return violation;
    }

    public void setViolation(ModerationViolation violation) {
        this.violation = Optional.of(Objects.requireNonNull(violation, "violation"));
    }
}
