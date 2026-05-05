package dev._2lstudios.chatsentinel.bukkit.filter;

import dev._2lstudios.chatsentinel.shared.filter.PlainYamlUserFilterWriter;
import dev._2lstudios.chatsentinel.shared.filter.UserFilterWriter;

import java.io.File;
import java.io.IOException;

public final class BukkitUserFilterWriter implements UserFilterWriter {
    private final PlainYamlUserFilterWriter writer;

    public BukkitUserFilterWriter(File dataFolder) {
        if (dataFolder == null) {
            throw new NullPointerException("dataFolder");
        }
        this.writer = new PlainYamlUserFilterWriter(new File(dataFolder, "blacklist").toPath());
    }

    @Override
    public void appendExpression(String moduleId, String expression) throws IOException {
        writer.appendExpression(moduleId, expression);
    }
}
