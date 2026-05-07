package dev._2lstudios.chatsentinel.shared.modules;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class CorrectionModule {
    private boolean enabled = true;
    private String customName = "Correction";
    private boolean notifyPlayer = true;
    private boolean applyToNormalCommands = false;
    private boolean preserveCapitalization = true;
    private boolean ignorePlayerNames = true;
    private int maxCorrectionsPerMessage = 8;
    private String bypassPermission = "chatsentinel.bypass.correction";
    private Map<String, String> replacements = Collections.emptyMap();
    private Set<String> ignoredWords = Collections.emptySet();
    private Supplier<Collection<String>> onlineNamesSupplier = new Supplier<Collection<String>>() {
        @Override
        public Collection<String> get() {
            return Collections.emptyList();
        }
    };

    public void loadData(
            final boolean enabled,
            final String customName,
            final boolean notifyPlayer,
            final boolean applyToNormalCommands,
            final boolean preserveCapitalization,
            final boolean ignorePlayerNames,
            final int maxCorrectionsPerMessage,
            final String bypassPermission,
            final Map<String, String> replacements,
            final Collection<String> ignoredWords,
            final Supplier<Collection<String>> onlineNamesSupplier) {
        this.enabled = enabled;
        this.customName = customName == null || customName.trim().isEmpty() ? "Correction" : customName;
        this.notifyPlayer = notifyPlayer;
        this.applyToNormalCommands = applyToNormalCommands;
        this.preserveCapitalization = preserveCapitalization;
        this.ignorePlayerNames = ignorePlayerNames;
        this.maxCorrectionsPerMessage = maxCorrectionsPerMessage < 1 ? 8 : maxCorrectionsPerMessage;
        this.bypassPermission = bypassPermission == null ? "" : bypassPermission.trim();

        if (onlineNamesSupplier != null) {
            this.onlineNamesSupplier = onlineNamesSupplier;
        }

        final Map<String, String> normalizedReplacements = new HashMap<>();
        final Set<String> normalizedIgnored = new HashSet<>();

        if (ignoredWords != null) {
            for (String word : ignoredWords) {
                if (word != null && !word.trim().isEmpty()) {
                    normalizedIgnored.add(word.trim().toLowerCase(Locale.ROOT));
                }
            }
        }

        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key == null || key.trim().isEmpty() || value == null || value.trim().isEmpty()) {
                    continue;
                }
                final String normalizedKey = normalizeToken(key);
                if (normalizedIgnored.contains(normalizedKey)) {
                    continue;
                }
                normalizedReplacements.put(normalizedKey, value);
            }
        }

        this.replacements = Collections.unmodifiableMap(normalizedReplacements);
        this.ignoredWords = Collections.unmodifiableSet(normalizedIgnored);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean isNotifyPlayer() {
        return notifyPlayer;
    }

    public boolean isApplyToNormalCommands() {
        return applyToNormalCommands;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public boolean hasReplacements() {
        return !replacements.isEmpty();
    }

    public CorrectionResult correct(final String message) {
        if (!enabled || message == null || message.isEmpty() || replacements.isEmpty() || maxCorrectionsPerMessage <= 0) {
            return new CorrectionResult(message, 0);
        }

        StringBuilder builder = null;
        int copyFrom = 0;
        int tokenStart = -1;
        int corrections = 0;
        final int length = message.length();

        for (int i = 0; i <= length; i++) {
            final boolean atEnd = i == length;
            final boolean tokenChar = !atEnd && isTokenChar(message.charAt(i));

            if (tokenChar) {
                if (tokenStart < 0) {
                    tokenStart = i;
                }
                continue;
            }

            if (tokenStart >= 0) {
                final int tokenEnd = i;
                final String token = message.substring(tokenStart, tokenEnd);
                final String lookup = normalizeToken(token);

                if (!isIgnored(lookup) && !(ignorePlayerNames && isOnlinePlayerName(token))) {
                    final String replacement = replacements.get(lookup);
                    if (replacement != null && !lookup.equals(normalizeToken(replacement))) {
                        if (builder == null) {
                            builder = new StringBuilder(length + 16);
                        }
                        builder.append(message, copyFrom, tokenStart);
                        builder.append(applyCapitalization(token, replacement));
                        copyFrom = tokenEnd;
                        corrections++;
                        if (corrections >= maxCorrectionsPerMessage) {
                            tokenStart = -1;
                            break;
                        }
                    }
                }

                tokenStart = -1;
            }
        }

        if (builder == null) {
            return new CorrectionResult(message, 0);
        }

        builder.append(message, copyFrom, length);
        return new CorrectionResult(builder.toString(), corrections);
    }

    private boolean isTokenChar(final char c) {
        return Character.isLetter(c);
    }

    private String normalizeToken(final String token) {
        return token == null ? "" : token.toLowerCase(Locale.ROOT);
    }

    private boolean isIgnored(final String token) {
        return ignoredWords.contains(token);
    }

    private boolean isOnlinePlayerName(final String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        final Collection<String> names = onlineNamesSupplier.get();
        if (names == null || names.isEmpty()) {
            return false;
        }
        final String lower = token.toLowerCase(Locale.ROOT);
        for (String name : names) {
            if (name != null && name.equalsIgnoreCase(lower)) {
                return true;
            }
        }
        return false;
    }

    private String applyCapitalization(final String original, final String replacement) {
        if (!preserveCapitalization) {
            return replacement;
        }
        if (original.length() > 1 && isAllUppercase(original)) {
            return replacement.toUpperCase(Locale.ROOT);
        }
        if (isCapitalized(original)) {
            return capitalizeFirst(replacement);
        }
        return replacement;
    }

    private boolean isAllUppercase(final String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isCapitalized(final String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (!Character.isUpperCase(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String capitalizeFirst(final String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}