package dev._2lstudios.chatsentinel.shared.commands;

public final class CommandResult {
    private final boolean handled;

    private CommandResult(final boolean handled) {
        this.handled = handled;
    }

    public static CommandResult handled() {
        return new CommandResult(true);
    }

    public boolean isHandled() {
        return handled;
    }
}
