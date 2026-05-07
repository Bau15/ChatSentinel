package dev._2lstudios.chatsentinel.bungee.commands;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.platform.BungeeCommandActor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public final class AutoCorrectCommand extends Command implements TabExecutor {
    private final ChatSentinel plugin;

    public AutoCorrectCommand(final ChatSentinel plugin) {
        super("autocorrect", null, "correction");
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        plugin.getCommandService().execute(new BungeeCommandActor(sender, plugin.getMessageSink()), "autocorrect", args);
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        return plugin.getCommandService().suggest(new BungeeCommandActor(sender, plugin.getMessageSink()), args);
    }
}