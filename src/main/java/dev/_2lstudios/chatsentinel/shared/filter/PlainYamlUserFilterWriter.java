package dev._2lstudios.chatsentinel.shared.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class PlainYamlUserFilterWriter implements UserFilterWriter {
    private static final String DEFAULT_MODULE_ID = "user";

    private final Path blacklistFolder;

    public PlainYamlUserFilterWriter(Path blacklistFolder) {
        if (blacklistFolder == null) {
            throw new NullPointerException("blacklistFolder");
        }
        this.blacklistFolder = blacklistFolder;
    }

    @Override
    public void appendExpression(String moduleId, String expression) throws IOException {
        String normalizedModuleId = normalizeModuleId(moduleId);
        String normalizedExpression = normalizeExpression(expression);
        Path file = blacklistFolder.resolve(normalizedModuleId).resolve("custom.yml");
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        boolean newFile = Files.notExists(file) || Files.size(file) == 0L;
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        try {
            if (newFile) {
                writer.write("expressions:");
                writer.newLine();
            }
            writer.write("  - '");
            writer.write(escapeYamlSingleQuoted(normalizedExpression));
            writer.write("'");
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private static String normalizeModuleId(String moduleId) {
        if (moduleId == null || moduleId.trim().isEmpty()) {
            return DEFAULT_MODULE_ID;
        }
        String normalizedModuleId = moduleId.trim();
        if (!normalizedModuleId.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Module id must contain only letters, numbers, underscores, or dashes");
        }
        return normalizedModuleId;
    }

    private static String normalizeExpression(String expression) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }
        String normalizedExpression = expression.trim();
        if (normalizedExpression.isEmpty()) {
            throw new IllegalArgumentException("expression must not be blank");
        }
        return normalizedExpression;
    }

    private static String escapeYamlSingleQuoted(String value) {
        return value.replace("'", "''");
    }
}
