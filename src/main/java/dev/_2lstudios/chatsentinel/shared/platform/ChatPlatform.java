package dev._2lstudios.chatsentinel.shared.platform;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChatPlatform {
    Collection<ChatUser> getOnlineUsers();

    Optional<ChatUser> findUser(UUID uniqueId);

    void sendConsoleMessage(String legacyMessage);

    void dispatchConsoleCommand(String command);

    void runAsync(Runnable runnable);

    String getPlatformName();
}
