package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleId;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyTrimSettings;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public final class SocialSpySignListener implements Listener {
    private final SocialSpyService socialSpyService;

    public SocialSpySignListener(final SocialSpyService socialSpyService) {
        this.socialSpyService = socialSpyService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event) {
        final ChatSentinel plugin = ChatSentinel.getInstance();
        final BukkitChatUser chatUser = new BukkitChatUser(plugin, event.getPlayer(), plugin.getMessageSink());
        final SocialSpyTrimSettings trim = plugin.getModuleManager().getSocialSpyModule().getTrimSettings();
        final String[] lines = event.getLines();
        final StringBuilder content = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                content.append(" | ");
            }
            content.append(trim.trim(lines[i], trim.getSignLineChars()));
        }
        final Location location = event.getBlock().getLocation();
        socialSpyService.publish(SocialSpyModuleId.SIGNS, chatUser, new String[][] {
                { "%world%", "%x%", "%y%", "%z%", "%content%" },
                { location.getWorld() == null ? "" : location.getWorld().getName(), String.valueOf(location.getBlockX()),
                        String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ()), content.toString() }
        });
    }
}
