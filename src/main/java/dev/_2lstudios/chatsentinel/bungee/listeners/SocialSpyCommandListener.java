package dev._2lstudios.chatsentinel.bungee.listeners;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.platform.BungeeChatUser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class SocialSpyCommandListener implements Listener {
    private final ChatSentinel plugin;
    private final SocialSpyService socialSpyService;

    public SocialSpyCommandListener(final ChatSentinel plugin, final SocialSpyService socialSpyService) {
        this.plugin = plugin;
        this.socialSpyService = socialSpyService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatEvent(final ChatEvent event) {
        if (event.isCancelled() || !event.isCommand()) {
            return;
        }
        final Connection sender = event.getSender();
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        socialSpyService.publishCommand(new BungeeChatUser(player, plugin.getMessageSink()), event.getMessage());
    }
}
