package dev._2lstudios.chatsentinel.bukkit.config;

import dev._2lstudios.chatsentinel.bukkit.ChatSentinel;
import dev._2lstudios.chatsentinel.bukkit.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.config.MutableModuleConfigStore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class BukkitMutableModuleConfigStore implements MutableModuleConfigStore {
    private final ChatSentinel plugin;
    private final ConfigUtil configUtil;

    public BukkitMutableModuleConfigStore(final ChatSentinel plugin, final ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public void setBoolean(final String path, final boolean value) throws IOException {
        final YamlConfiguration config = configUtil.get("%datafolder%/config.yml");
        config.set(path, value);
        config.save(new File(plugin.getDataFolder(), "config.yml"));
    }
}
