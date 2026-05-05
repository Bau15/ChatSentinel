package dev._2lstudios.chatsentinel.shared.platform;

import java.util.UUID;

import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;

public interface ChatUser {
    UUID getUniqueId();

    String getName();

    String getLocale();

    String getServerName();

    boolean hasPermission(String permission);

    void sendMessage(String legacyMessage);

    void sendWarning(String legacyMessage, WarningDeliverySettings settings);
}
