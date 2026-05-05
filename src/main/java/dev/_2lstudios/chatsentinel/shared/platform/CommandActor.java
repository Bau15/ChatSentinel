package dev._2lstudios.chatsentinel.shared.platform;

public interface CommandActor {
    String getName();

    String getLocale();

    boolean isPlayer();

    boolean hasPermission(String permission);

    void sendMessage(String legacyMessage);

    ChatUser asUserOrNull();
}
