package dev._2lstudios.chatsentinel.bukkit.listeners;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitChatUser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleId;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyTrimSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class SocialSpyAnvilListener implements Listener {
    private final SocialSpyService socialSpyService;

    public SocialSpyAnvilListener(final SocialSpyService socialSpyService) {
        this.socialSpyService = socialSpyService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getView().getPlayer();
        final String renameText = resolveRenameText(event);
        if (renameText == null || renameText.trim().isEmpty()) {
            pendingRenames.remove(player.getUniqueId());
            return;
        }
        final ItemStack first = event.getInventory().getItem(0);
        final String oldName = displayNameOrType(first);
        if (renameText.equals(oldName)) {
            pendingRenames.remove(player.getUniqueId());
            return;
        }
        pendingRenames.put(player.getUniqueId(), new PendingRename(oldName, renameText));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getView().getTopInventory() == null || event.getView().getTopInventory().getType() != org.bukkit.event.inventory.InventoryType.ANVIL) {
            return;
        }
        if (event.getRawSlot() != 2) {
            return;
        }
        final ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == org.bukkit.Material.AIR) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        final PendingRename pending = pendingRenames.remove(player.getUniqueId());
        if (pending == null) {
            return;
        }
        final ChatSentinel plugin = ChatSentinel.getInstance();
        final SocialSpyTrimSettings trim = plugin.getModuleManager().getSocialSpyModule().getTrimSettings();
        final BukkitChatUser chatUser = new BukkitChatUser(plugin, player, plugin.getMessageSink());
        socialSpyService.publish(SocialSpyModuleId.ANVILS, chatUser, new String[][] {
                { "%old_name%", "%new_name%" },
                { trim.trim(pending.getOldName(), trim.getAnvilNameChars()), trim.trim(pending.getNewName(), trim.getAnvilNameChars()) }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        pendingRenames.remove(((Player) event.getPlayer()).getUniqueId());
    }

    private String resolveRenameText(final PrepareAnvilEvent event) {
        try {
            final Object inventory = event.getInventory();
            final java.lang.reflect.Method method = inventory.getClass().getMethod("getRenameText");
            final Object result = method.invoke(inventory);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (NoSuchMethodError | NoSuchMethodException | java.lang.reflect.InvocationTargetException | IllegalAccessException ignored) {
        }
        return "";
    }

    private String displayNameOrType(final ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) {
            return "";
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return item.getType().name().toLowerCase().replace("_", " ");
    }

    private static final class PendingRename {
        private final String oldName;
        private final String newName;

        PendingRename(final String oldName, final String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        String getOldName() {
            return oldName;
        }

        String getNewName() {
            return newName;
        }
    }

    private final java.util.Map<UUID, PendingRename> pendingRenames = new java.util.concurrent.ConcurrentHashMap<UUID, PendingRename>();
}