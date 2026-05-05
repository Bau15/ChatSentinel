package dev._2lstudios.chatsentinel.shared.filter;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class UserRegexAddService {
    public static final int MAX_RAW_REGEX_LENGTH = 512;
    public static final int MAX_PLAIN_TEXT_LENGTH = CommonRegexGenerator.DEFAULT_MAX_LENGTH;
    public static final String DEFAULT_MODULE_ID = "user";

    private final UserFilterWriter writer;
    private final CommonRegexGenerator regexGenerator;

    public UserRegexAddService(UserFilterWriter writer) {
        this(writer, new CommonRegexGenerator(MAX_PLAIN_TEXT_LENGTH));
    }

    public UserRegexAddService(UserFilterWriter writer, CommonRegexGenerator regexGenerator) {
        this.writer = Objects.requireNonNull(writer, "writer");
        this.regexGenerator = Objects.requireNonNull(regexGenerator, "regexGenerator");
    }

    public String addCommon(String moduleId, String plainText) throws IOException {
        String normalizedModuleId = normalizeModuleId(moduleId);
        String normalizedPlainText = requireText(plainText, MAX_PLAIN_TEXT_LENGTH, "plain text");
        String expression = regexGenerator.toCommonRegex(normalizedPlainText);
        writer.appendExpression(normalizedModuleId, expression);
        return expression;
    }

    public String addRaw(String moduleId, String rawRegex) throws IOException {
        String normalizedModuleId = normalizeModuleId(moduleId);
        String expression = requireText(rawRegex, MAX_RAW_REGEX_LENGTH, "raw regex");
        validateRegex(expression);
        writer.appendExpression(normalizedModuleId, expression);
        return expression;
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

    private static String requireText(String value, int maxLength, String name) {
        if (value == null) {
            throw new NullPointerException(name);
        }
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (trimmedValue.length() > maxLength) {
            throw new IllegalArgumentException(name + " length exceeds " + maxLength);
        }
        return trimmedValue;
    }

    private static void validateRegex(String expression) {
        try {
            Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException exception) {
            throw new IllegalArgumentException("Invalid regex: " + exception.getMessage(), exception);
        }
    }
}
