package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleId;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyTrimSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public final class SocialSpyBookListener implements Listener {
    private final SocialSpyService socialSpyService;

    public SocialSpyBookListener(final SocialSpyService socialSpyService) {
        this.socialSpyService = socialSpyService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEditBook(final PlayerEditBookEvent event) {
        final ChatSentinel plugin = ChatSentinel.getInstance();
        final BookMeta meta = event.getNewBookMeta();
        final SocialSpyTrimSettings trim = plugin.getModuleManager().getSocialSpyModule().getTrimSettings();
        final String title = meta.hasTitle() ? meta.getTitle() : "<unsigned>";
        final List<String> pages = meta.getPages();
        final StringBuilder content = new StringBuilder();
        for (String page : pages) {
            if (content.length() > 0) {
                content.append(' ');
            }
            content.append(page == null ? "" : page.replace('\n', ' '));
        }
        final BukkitChatUser chatUser = new BukkitChatUser(plugin, event.getPlayer(), plugin.getMessageSink());
        socialSpyService.publish(SocialSpyModuleId.BOOKS, chatUser, new String[][] {
                { "%title%", "%content%", "%pages%", "%signing%" },
                { trim.trim(title, trim.getBookTitleChars()), trim.trim(content.toString(), trim.getBookContentChars()),
                        String.valueOf(pages.size()), String.valueOf(event.isSigning()) }
        });
    }
}
