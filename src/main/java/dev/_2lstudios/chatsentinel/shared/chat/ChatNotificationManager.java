package dev._2lstudios.chatsentinel.shared.chat;

import java.util.*;

public class ChatNotificationManager {
    private final LinkedHashSet<ChatPlayer> notifiedChatPlayers = new LinkedHashSet<>();

    public void addPlayer(ChatPlayer chatPlayer) {
        notifiedChatPlayers.add(chatPlayer);
    }

    public boolean containsPlayer(ChatPlayer chatPlayer) {
        return notifiedChatPlayers.contains(chatPlayer);
    }

    public void removePlayer(ChatPlayer chatPlayer) {
        notifiedChatPlayers.remove(chatPlayer);
    }

    public List<ChatPlayer> getAllPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(notifiedChatPlayers));
    }
}
