package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Objects;

public final class FilterCompileError {
    private final FilterSource source;
    private final int expressionIndex;
    private final String expression;
    private final String errorMessage;

    public FilterCompileError(FilterSource source, int expressionIndex, String expression, String errorMessage) {
        this.source = Objects.requireNonNull(source, "source");
        this.expressionIndex = expressionIndex;
        this.expression = Objects.requireNonNull(expression, "expression");
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage");
    }

    public FilterSource getSource() {
        return source;
    }

    public int getExpressionIndex() {
        return expressionIndex;
    }

    public String getExpression() {
        return expression;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterCompileError)) {
            return false;
        }
        FilterCompileError that = (FilterCompileError) o;
        return expressionIndex == that.expressionIndex
                && source.equals(that.source)
                && expression.equals(that.expression)
                && errorMessage.equals(that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, expressionIndex, expression, errorMessage);
    }

    @Override
    public String toString() {
        return "FilterCompileError{" +
                "source=" + source +
                ", expressionIndex=" + expressionIndex +
                ", expression='" + expression + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
