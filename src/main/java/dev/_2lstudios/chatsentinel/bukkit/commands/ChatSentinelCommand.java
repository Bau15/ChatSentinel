package dev._2lstudios.chatsentinel.bukkit.commands;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.platform.BukkitCommandActor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class ChatSentinelCommand implements CommandExecutor, TabCompleter {
    private final ChatSentinel plugin;

    public ChatSentinelCommand(final ChatSentinel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String normalizedLabel = label == null ? "" : label.trim().toLowerCase();
        if ((normalizedLabel.equals("socialspy") || normalizedLabel.equals("sspy"))
                && args.length == 1 && "menu".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can open the SocialSpy menu.");
                return true;
            }
            final Player player = (Player) sender;
            if (!player.hasPermission("chatsentinel.socialspy")) {
                player.sendMessage("You do not have permission to use SocialSpy.");
                return true;
            }
            plugin.getSocialSpyMenu().open(player);
            return true;
        }
        plugin.getCommandService().execute(new BukkitCommandActor(plugin, sender, plugin.getMessageSink()), label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return plugin.getCommandService().suggest(new BukkitCommandActor(plugin, sender, plugin.getMessageSink()), alias, args);
    }
}
