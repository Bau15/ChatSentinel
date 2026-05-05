package dev._2lstudios.chatsentinel.velocity.filter;

import dev._2lstudios.chatsentinel.shared.filter.PlainYamlUserFilterWriter;
import dev._2lstudios.chatsentinel.shared.filter.UserFilterWriter;

import java.io.IOException;
import java.nio.file.Path;

public final class VelocityUserFilterWriter implements UserFilterWriter {
    private final PlainYamlUserFilterWriter writer;

    public VelocityUserFilterWriter(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new NullPointerException("dataDirectory");
        }
        this.writer = new PlainYamlUserFilterWriter(dataDirectory.resolve("blacklist"));
    }

    @Override
    public void appendExpression(String moduleId, String expression) throws IOException {
        writer.appendExpression(moduleId, expression);
    }
}
