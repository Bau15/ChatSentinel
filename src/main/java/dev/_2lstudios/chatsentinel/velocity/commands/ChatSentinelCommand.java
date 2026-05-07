package dev._2lstudios.chatsentinel.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import dev._2lstudios.chatsentinel.velocity.platform.VelocityCommandActor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ChatSentinelCommand implements SimpleCommand {
    private final ChatSentinel plugin;

    public ChatSentinelCommand(final ChatSentinel plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        plugin.getCommandService().execute(new VelocityCommandActor(invocation.source(), plugin.getMessageSink()), invocation.alias(), invocation.arguments());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        final String alias = invocation.alias();
        final List<String> results = plugin.getCommandService().suggest(
                new VelocityCommandActor(invocation.source(), plugin.getMessageSink()), alias, invocation.arguments());
        return CompletableFuture.completedFuture(results);
    }
}
