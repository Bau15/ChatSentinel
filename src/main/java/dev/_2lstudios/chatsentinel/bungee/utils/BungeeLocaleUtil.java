package dev._2lstudios.chatsentinel.bungee.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;

public final class BungeeLocaleUtil {
    private BungeeLocaleUtil() {
    }

    public static String getLocale(final ProxiedPlayer player) {
        final Locale locale = player == null ? null : player.getLocale();
        if (locale == null) {
            return "en";
        }
        final String localeString = locale.toString();
        return localeString.length() > 1 ? localeString.substring(0, 2) : "en";
    }
}
