package dev._2lstudios.chatsentinel.bungee.commands;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.platform.BungeeCommandActor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public final class ServerMuteCommand extends Command implements TabExecutor {
    private final ChatSentinel plugin;

    public ServerMuteCommand(final ChatSentinel plugin) {
        super("servermute", null, "muteall", "muteserver");
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getCommandService().execute(new BungeeCommandActor(sender, plugin.getMessageSink()), "servermute", args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        return plugin.getCommandService().suggest(new BungeeCommandActor(sender, plugin.getMessageSink()), "servermute", args);
    }
}