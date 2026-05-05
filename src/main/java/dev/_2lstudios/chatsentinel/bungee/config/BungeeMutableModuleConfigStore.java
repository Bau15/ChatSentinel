package dev._2lstudios.chatsentinel.bungee.config;

import dev._2lstudios.chatsentinel.bungee.ChatSentinel;
import dev._2lstudios.chatsentinel.bungee.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.config.MutableModuleConfigStore;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class BungeeMutableModuleConfigStore implements MutableModuleConfigStore {
    private final ChatSentinel plugin;
    private final ConfigUtil configUtil;

    public BungeeMutableModuleConfigStore(final ChatSentinel plugin, final ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void setBoolean(final String path, final boolean value) throws IOException {
        final Configuration config = configUtil.get("%datafolder%/config.yml");
        if (config == null) {
            throw new IOException("Unable to load config.yml");
        }
        config.set(path, value);
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), "config.yml"));
    }
}
