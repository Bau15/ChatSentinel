package dev._2lstudios.chatsentinel.velocity.listeners;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import dev._2lstudios.chatsentinel.velocity.platform.VelocityChatUser;

public final class SocialSpyCommandListener {
    private final ChatSentinel plugin;
    private final SocialSpyService socialSpyService;

    public SocialSpyCommandListener(final ChatSentinel plugin, final SocialSpyService socialSpyService) {
        this.plugin = plugin;
        this.socialSpyService = socialSpyService;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommandExecute(final CommandExecuteEvent event) {
        final CommandSource source = event.getCommandSource();
        if (!(source instanceof Player)) {
            return;
        }
        final Player player = (Player) source;
        socialSpyService.publishCommand(new VelocityChatUser(player, plugin.getMessageSink()), event.getCommand());
    }
}
