package dev._2lstudios.chatsentinel.velocity.platform;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.platform.CommandActor;
import dev._2lstudios.chatsentinel.shared.text.LegacyText;
import dev._2lstudios.chatsentinel.velocity.text.VelocityMessageSink;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityCommandActor implements CommandActor {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final CommandSource source;
    private final VelocityMessageSink messageSink;

    public VelocityCommandActor(final CommandSource source, final VelocityMessageSink messageSink) {
        this.source = source;
        this.messageSink = messageSink;
    }

    @Override
    public String getName() {
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        }
        return "Console";
    }

    @Override
    public String getLocale() {
        final ChatUser user = asUserOrNull();
        return user == null ? "en" : user.getLocale();
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public boolean hasPermission(final String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String legacyMessage) {
        final ChatUser user = asUserOrNull();
        if (user != null) {
            user.sendMessage(legacyMessage);
            return;
        }
        for (final String line : LegacyText.toSectionLines(legacyMessage)) {
            source.sendMessage(LEGACY_SERIALIZER.deserialize(line));
        }
    }

    @Override
    public ChatUser asUserOrNull() {
        if (!(source instanceof Player)) {
            return null;
        }
        return new VelocityChatUser((Player) source, messageSink);
    }
}
