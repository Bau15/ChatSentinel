package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CompiledFilterFile {
    private final FilterSource source;
    private final Pattern pattern;
    private final int expressionCount;

    public CompiledFilterFile(FilterSource source, Pattern pattern, int expressionCount) {
        this.source = Objects.requireNonNull(source, "source");
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.expressionCount = expressionCount;
    }

    public FilterSource getSource() {
        return source;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public int getExpressionCount() {
        return expressionCount;
    }

    public Optional<FilterMatch> findFirst(String input) {
        Objects.requireNonNull(input, "input");
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new FilterMatch(source, matcher.group(), matcher.start(), matcher.end()));
    }
}
