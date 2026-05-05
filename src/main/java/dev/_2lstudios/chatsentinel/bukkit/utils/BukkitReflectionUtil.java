package dev._2lstudios.chatsentinel.bukkit.utils;

import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public final class BukkitReflectionUtil {
    private static final MethodHandle GET_LOCALE_PLAYER_METHOD = localePlayer();
    private static final MethodHandle GET_LOCALE_SPIGOT_METHOD = localeSpigot();
    private static final MethodHandle GET_HANDLE_METHOD = handleMethod();
    private static final MethodHandle DELETE_MESSAGE_METHOD = deleteMessageMethod();
    private static Field pingField;

    private BukkitReflectionUtil() {
    }

    private static MethodHandle localePlayer() {
        try {
            return MethodHandles.publicLookup().findVirtual(Player.class, "getLocale", MethodType.methodType(String.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            return null;
        }
    }

    private static MethodHandle localeSpigot() {
        try {
            return MethodHandles.publicLookup().findVirtual(Spigot.class, "getLocale", MethodType.methodType(String.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            return null;
        }
    }

    private static MethodHandle handleMethod() {
        try {
            final Method method = Player.class.getMethod("getHandle");
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException exception) {
            return null;
        }
    }

    private static MethodHandle deleteMessageMethod() {
        try {
            return MethodHandles.publicLookup().findVirtual(Player.class, "deleteMessage", MethodType.methodType(void.class, UUID.class));
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            return null;
        }
    }

    public static MethodHandle getLocalePlayerMethod() {
        return GET_LOCALE_PLAYER_METHOD;
    }

    public static MethodHandle getLocaleSpigotMethod() {
        return GET_LOCALE_SPIGOT_METHOD;
    }

    public static MethodHandle getHandleMethod() {
        return GET_HANDLE_METHOD;
    }

    public static boolean deleteMessage(final Player player, final UUID messageId) {
        if (player == null || messageId == null || DELETE_MESSAGE_METHOD == null) {
            return false;
        }
        try {
            DELETE_MESSAGE_METHOD.invoke(player, messageId);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static Field getPingField(final Object playerHandle) throws NoSuchFieldException {
        return pingField == null ? pingField = playerHandle.getClass().getField("ping") : pingField;
    }
}
