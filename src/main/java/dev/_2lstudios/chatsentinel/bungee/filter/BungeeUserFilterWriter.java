package dev._2lstudios.chatsentinel.bungee.filter;

import dev._2lstudios.chatsentinel.shared.filter.PlainYamlUserFilterWriter;
import dev._2lstudios.chatsentinel.shared.filter.UserFilterWriter;

import java.io.File;
import java.io.IOException;

public final class BungeeUserFilterWriter implements UserFilterWriter {
    private final PlainYamlUserFilterWriter writer;

    public BungeeUserFilterWriter(File dataFolder) {
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
