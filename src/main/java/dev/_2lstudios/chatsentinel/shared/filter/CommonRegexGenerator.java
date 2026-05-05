package dev._2lstudios.chatsentinel.shared.filter;

import java.util.regex.Pattern;

public final class CommonRegexGenerator {
    public static final int DEFAULT_MAX_LENGTH = 64;
    private static final String SEPARATOR = "[\\W_]*";
    private final int maxInputLength;

    public CommonRegexGenerator() {
        this(DEFAULT_MAX_LENGTH);
    }

    public CommonRegexGenerator(int maxInputLength) {
        this.maxInputLength = maxInputLength;
    }

    public String toCommonRegex(String plainText) {
        if (plainText == null) {
            throw new NullPointerException("plainText");
        }
        String trimmedText = plainText.trim();
        if (trimmedText.isEmpty()) {
            throw new IllegalArgumentException("Input must not be blank");
        }
        if (trimmedText.length() > maxInputLength) {
            throw new IllegalArgumentException("Input length exceeds " + maxInputLength);
        }
        StringBuilder regex = new StringBuilder();
        boolean previousWasToken = false;
        for (int i = 0; i < trimmedText.length(); i++) {
            char character = trimmedText.charAt(i);
            if (Character.isWhitespace(character)) {
                continue;
            }
            if (previousWasToken) {
                regex.append(SEPARATOR);
            }
            regex.append(mapCharacter(character));
            previousWasToken = true;
        }
        if (regex.length() == 0) {
            throw new IllegalArgumentException("Input must contain visible characters");
        }
        return regex.toString();
    }

    public String toLiteralRegex(String literalText) {
        if (literalText == null) {
            throw new NullPointerException("literalText");
        }
        return Pattern.quote(literalText);
    }

    private String mapCharacter(char character) {
        switch (Character.toLowerCase(character)) {
            case 'a':
                return "[a@4]";
            case 'e':
                return "[e3]";
            case 'i':
                return "[i1!|]";
            case 'o':
                return "[o0]";
            case 's':
                return "[s5$]";
            case 't':
                return "[t7]";
            default:
                return Pattern.quote(String.valueOf(character));
        }
    }
}
