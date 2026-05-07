package dev._2lstudios.chatsentinel.shared.chat;

import java.util.*;

public class ChatNotificationManager {
    private final LinkedHashSet<ChatPlayer> notifiedChatPlayers = new LinkedHashSet<ChatPlayer>();

    public synchronized void addPlayer(final ChatPlayer chatPlayer) {
        notifiedChatPlayers.add(chatPlayer);
    }

    public synchronized boolean containsPlayer(final ChatPlayer chatPlayer) {
        return notifiedChatPlayers.contains(chatPlayer);
    }

    public synchronized void removePlayer(final ChatPlayer chatPlayer) {
        notifiedChatPlayers.remove(chatPlayer);
    }

    public synchronized List<ChatPlayer> getAllPlayers() {
        return Collections.unmodifiableList(new ArrayList<ChatPlayer>(notifiedChatPlayers));
    }
}
