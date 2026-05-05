package dev._2lstudios.chatsentinel.shared.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;

import dev._2lstudios.chatsentinel.shared.platform.ChatUser;

public class ChatPlayerManager {
    private final ConcurrentMap<UUID, ChatPlayer> chatPlayers = new ConcurrentHashMap<UUID, ChatPlayer>();

    public ChatPlayer getPlayer(final UUID uuid) {
        return chatPlayers.computeIfAbsent(uuid, ChatPlayer::new);
    }

    public ChatPlayer getPlayer(final ChatUser user) {
        return getPlayer(user.getUniqueId());
    }

    public List<ChatPlayer> getAllPlayers() {
        return Collections.unmodifiableList(new ArrayList<ChatPlayer>(chatPlayers.values()));
    }
}
