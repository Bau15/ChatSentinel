package dev._2lstudios.chatsentinel.shared.socialspy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SocialSpyCommandParser {
    private final Map<String, SocialSpyCommandDefinition> privateMessageDefinitions;
    private final Set<String> ignoredCommandRoots;

    public SocialSpyCommandParser(final List<SocialSpyCommandDefinition> privateMessageDefinitions,
            final List<String> ignoredCommandRoots) {
        this.privateMessageDefinitions = new HashMap<String, SocialSpyCommandDefinition>();
        if (privateMessageDefinitions != null) {
            for (SocialSpyCommandDefinition definition : privateMessageDefinitions) {
                if (definition != null) {
                    this.privateMessageDefinitions.put(definition.getCommandRoot(), definition);
                }
            }
        }
        this.ignoredCommandRoots = new HashSet<String>();
        if (ignoredCommandRoots != null) {
            for (String root : ignoredCommandRoots) {
                if (root != null && !root.trim().isEmpty()) {
                    this.ignoredCommandRoots.add(normalizeRoot(root));
                }
            }
        }
    }

    public SocialSpyCommandSpyResult parse(final String commandText) {
        String command = commandText == null ? "" : commandText.trim();
        if (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        if (command.isEmpty()) {
            return SocialSpyCommandSpyResult.none(command);
        }

        final String[] tokens = command.split("\\s+");
        if (tokens.length == 0) {
            return SocialSpyCommandSpyResult.none(command);
        }

        final String root = normalizeRoot(tokens[0]);
        if (ignoredCommandRoots.contains(root)) {
            return SocialSpyCommandSpyResult.none(command);
        }

        final SocialSpyCommandDefinition definition = privateMessageDefinitions.get(root);
        if (definition == null) {
            return SocialSpyCommandSpyResult.generalCommand(root, command);
        }

        if (definition.getMessageStartArgumentIndex() >= tokens.length) {
            return SocialSpyCommandSpyResult.none(command);
        }
        if (definition.getTargetArgumentIndex() >= tokens.length) {
            return SocialSpyCommandSpyResult.none(command);
        }

        final String content = join(tokens, definition.getMessageStartArgumentIndex());
        if (content.trim().isEmpty()) {
            return SocialSpyCommandSpyResult.none(command);
        }

        final String target = definition.getTargetArgumentIndex() == -1
                ? "<reply>"
                : tokens[definition.getTargetArgumentIndex()];
        return SocialSpyCommandSpyResult.privateMessage(root, target, content, command);
    }

    private static String normalizeRoot(final String input) {
        final String value = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        final int namespaceIndex = value.indexOf(':');
        return namespaceIndex >= 0 && namespaceIndex + 1 < value.length() ? value.substring(namespaceIndex + 1) : value;
    }

    private static String join(final String[] tokens, final int startIndex) {
        final StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(tokens[i]);
        }
        return builder.toString();
    }
}
