package dev._2lstudios.chatsentinel.shared.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FilterExpressionFile {
    private final FilterSource source;
    private final List<String> expressions;

    public FilterExpressionFile(FilterSource source, List<String> expressions) {
        this.source = Objects.requireNonNull(source, "source");
        this.expressions = Collections.unmodifiableList(new ArrayList<String>(Objects.requireNonNull(expressions, "expressions")));
    }

    public FilterSource getSource() {
        return source;
    }

    public List<String> getExpressions() {
        return expressions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterExpressionFile)) {
            return false;
        }
        FilterExpressionFile that = (FilterExpressionFile) o;
        return source.equals(that.source) && expressions.equals(that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, expressions);
    }

    @Override
    public String toString() {
        return "FilterExpressionFile{" +
                "source=" + source +
                ", expressions=" + expressions +
                '}';
    }
}
