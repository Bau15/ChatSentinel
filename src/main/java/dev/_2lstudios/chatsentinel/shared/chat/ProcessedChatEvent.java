package dev._2lstudios.chatsentinel.shared.chat;

public final class ProcessedChatEvent {
    private final String message;
    private final boolean cancelled;
    private final boolean hide;

    public ProcessedChatEvent(final String message, final boolean cancelled, final boolean hide) {
        this.message = message == null ? "" : message;
        this.cancelled = cancelled;
        this.hide = hide;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isHide() {
        return hide;
    }
}
