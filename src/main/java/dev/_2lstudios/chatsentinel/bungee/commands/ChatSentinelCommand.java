package dev._2lstudios.chatsentinel.bungee.commands;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.platform.BungeeCommandActor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public final class ChatSentinelCommand extends Command implements TabExecutor {
    private final ChatSentinel plugin;

    public ChatSentinelCommand(final ChatSentinel plugin) {
        super("chatsentinel");
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getCommandService().execute(new BungeeCommandActor(sender, plugin.getMessageSink()), args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        return plugin.getCommandService().suggest(new BungeeCommandActor(sender, plugin.getMessageSink()), args);
    }
}
