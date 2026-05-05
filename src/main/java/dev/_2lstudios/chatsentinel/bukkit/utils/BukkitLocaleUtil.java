package dev._2lstudios.chatsentinel.bukkit.utils;

import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;

public final class BukkitLocaleUtil {
    private BukkitLocaleUtil() {
    }

    public static String getLocale(final Player player) {
        String locale = null;
        if (player != null && player.isOnline()) {
            MethodHandle getLocaleMethod = BukkitReflectionUtil.getLocalePlayerMethod();
            try {
                if (getLocaleMethod != null) {
                    locale = getLocaleMethod.invoke(player).toString();
                } else {
                    getLocaleMethod = BukkitReflectionUtil.getLocaleSpigotMethod();
                    if (getLocaleMethod != null) {
                        locale = getLocaleMethod.invoke(player.spigot()).toString();
                    }
                }
            } catch (Throwable throwable) {
                return "en";
            }
        }
        return locale != null && locale.length() > 1 ? locale.substring(0, 2) : "en";
    }
}
