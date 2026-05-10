package dev._2lstudios.chatsentinel.bukkit.gui;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleId;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SocialSpyMenu implements Listener {
    private static final String MENU_TITLE = "Spying Menu";
    private static final int SLOT_MESSAGES = 0;
    private static final int SLOT_SIGNS = 1;
    private static final int SLOT_BOOKS = 2;
    private static final int SLOT_ANVILS = 3;
    private static final int SLOT_COMMANDS = 4;
    private static final int SLOT_ENABLE_ALL = 6;
    private static final int SLOT_DISABLE_ALL = 7;
    private static final int SLOT_RESET = 8;
    private static final List<String> MODULE_SLOTS = Arrays.asList("messages", "signs", "books", "anvils", "commands");

    private final ChatSentinel plugin;
    private final SocialSpyService socialSpyService;

    public SocialSpyMenu(final ChatSentinel plugin, final SocialSpyService socialSpyService) {
        this.plugin = plugin;
        this.socialSpyService = socialSpyService;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(final Player player) {
        final Inventory inventory = Bukkit.createInventory(null, 9, MENU_TITLE);
        fillMenu(inventory, player);
        player.openInventory(inventory);
    }

    private void fillMenu(final Inventory inventory, final Player player) {
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createFiller());
        }
        inventory.setItem(SLOT_MESSAGES, createModuleItem(player, SocialSpyModuleId.MESSAGES));
        inventory.setItem(SLOT_SIGNS, createModuleItem(player, SocialSpyModuleId.SIGNS));
        inventory.setItem(SLOT_BOOKS, createModuleItem(player, SocialSpyModuleId.BOOKS));
        inventory.setItem(SLOT_ANVILS, createModuleItem(player, SocialSpyModuleId.ANVILS));
        inventory.setItem(SLOT_COMMANDS, createModuleItem(player, SocialSpyModuleId.COMMANDS));
        inventory.setItem(SLOT_ENABLE_ALL, createEnableAllItem());
        inventory.setItem(SLOT_DISABLE_ALL, createDisableAllItem());
        inventory.setItem(SLOT_RESET, createResetItem());
    }

    private ItemStack createModuleItem(final Player player, final String moduleId) {
        final boolean enabled = socialSpyService.isActiveFor(new BukkitChatUserProxy(player), moduleId);
        final Material material = enabled ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + capitalize(moduleId));
        meta.setLore(Arrays.asList(
                "§7Module: §f" + moduleId,
                "§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"),
                "§7",
                "§eClick to toggle"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEnableAllItem() {
        final ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aEnable All");
        meta.setLore(Arrays.asList("§7Click to enable all modules"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDisableAllItem() {
        final ItemStack item = new ItemStack(Material.RED_CONCRETE);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cDisable All");
        meta.setLore(Arrays.asList("§7Click to disable all modules"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createResetItem() {
        final ItemStack item = new ItemStack(Material.BARRIER);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cReset Defaults");
        meta.setLore(Arrays.asList("§7Click to reset to defaults"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFiller() {
        final ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private String capitalize(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getView().getTitle() == null || !event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= 9) {
            return;
        }
        final ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }
        if (slot == SLOT_ENABLE_ALL) {
            socialSpyService.toggleAllPermitted(new BukkitChatUserProxy(player));
            fillMenu(event.getInventory(), player);
            return;
        }
        if (slot == SLOT_DISABLE_ALL) {
            final List<String> modules = socialSpyService.status(new BukkitChatUserProxy(player)).contains("messages")
                    ? SocialSpyModuleId.ids() : SocialSpyModuleId.ids();
            for (final String moduleId : modules) {
                if (socialSpyService.hasModulePermission(new BukkitChatUserProxy(player), moduleId)) {
                    socialSpyService.setEnabled(new BukkitChatUserProxy(player), moduleId, false);
                }
            }
            fillMenu(event.getInventory(), player);
            return;
        }
        if (slot == SLOT_RESET) {
            socialSpyService.resetAll(new BukkitChatUserProxy(player));
            fillMenu(event.getInventory(), player);
            return;
        }
        final String moduleId = moduleIdForSlot(slot);
        if (moduleId != null && socialSpyService.hasModulePermission(new BukkitChatUserProxy(player), moduleId)) {
            socialSpyService.toggle(new BukkitChatUserProxy(player), moduleId);
            fillMenu(event.getInventory(), player);
        }
    }

    private String moduleIdForSlot(final int slot) {
        switch (slot) {
            case SLOT_MESSAGES:
                return SocialSpyModuleId.MESSAGES;
            case SLOT_SIGNS:
                return SocialSpyModuleId.SIGNS;
            case SLOT_BOOKS:
                return SocialSpyModuleId.BOOKS;
            case SLOT_ANVILS:
                return SocialSpyModuleId.ANVILS;
            case SLOT_COMMANDS:
                return SocialSpyModuleId.COMMANDS;
            default:
                return null;
        }
    }

    private static final class BukkitChatUserProxy implements ChatUser {
        private final Player player;

        BukkitChatUserProxy(final Player player) {
            this.player = player;
        }

        @Override
        public UUID getUniqueId() {
            return player.getUniqueId();
        }

        @Override
        public String getName() {
            return player.getName();
        }

        @Override
        public String getLocale() {
            return "en";
        }

        @Override
        public String getServerName() {
            return "server";
        }

        @Override
        public boolean hasPermission(final String permission) {
            return player.hasPermission(permission);
        }

        @Override
        public void sendMessage(final String message) {
            player.sendMessage(message);
        }

        @Override
        public void sendWarning(final String message, final dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings settings) {
            player.sendMessage(message);
        }
    }
}