package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Objects;

public final class FilterMatch {
    private final FilterSource source;
    private final String matchedText;
    private final int start;
    private final int end;

    public FilterMatch(FilterSource source, String matchedText, int start, int end) {
        this.source = Objects.requireNonNull(source, "source");
        this.matchedText = Objects.requireNonNull(matchedText, "matchedText");
        this.start = start;
        this.end = end;
    }

    public FilterSource getSource() {
        return source;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterMatch)) {
            return false;
        }
        FilterMatch that = (FilterMatch) o;
        return start == that.start && end == that.end && source.equals(that.source) && matchedText.equals(that.matchedText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, matchedText, start, end);
    }

    @Override
    public String toString() {
        return "FilterMatch{" +
                "source=" + source +
                ", matchedText='" + matchedText + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
