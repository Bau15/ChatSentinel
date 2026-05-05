package dev._2lstudios.chatsentinel.velocity.config;

import dev._2lstudios.chatsentinel.shared.config.MutableModuleConfigStore;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public final class VelocityMutableModuleConfigStore implements MutableModuleConfigStore {
    private final ChatSentinel plugin;

    public VelocityMutableModuleConfigStore(final ChatSentinel plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBoolean(final String path, final boolean value) throws IOException {
        final Path configPath = plugin.getDataDirectory().resolve("config.yml");
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
        final CommentedConfigurationNode root = loader.load();
        root.node((Object[]) path.split("\\.")).set(value);
        loader.save(root);
    }
}
