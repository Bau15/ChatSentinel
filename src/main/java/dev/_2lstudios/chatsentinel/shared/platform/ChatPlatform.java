package dev._2lstudios.chatsentinel.shared.platform;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.modules.ChatSnapshotModule;

public interface ChatPlatform {
    Collection<ChatUser> getOnlineUsers();

    Optional<ChatUser> findUser(UUID uniqueId);

    void sendConsoleMessage(String legacyMessage);

    void dispatchConsoleCommand(String command);

    void runAsync(Runnable runnable);

    String getPlatformName();

    void refreshOnlinePlayers(ChatPlayerManager chatPlayerManager,
            ChatNotificationManager chatNotificationManager,
            GeneralModule generalModule);

    default void replayChatSnapshot(final ChatSnapshotModule snapshotModule) {
        if (snapshotModule == null) {
            return;
        }
        for (final ChatUser user : getOnlineUsers()) {
            if (user == null) {
                continue;
            }
            user.sendMessage(snapshotModule.buildReplayPayload(user.getUniqueId()));
        }
    }
}
