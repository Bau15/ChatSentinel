package dev._2lstudios.chatsentinel.shared.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FilterCompileReport {
    private final int filesTotal;
    private final int filesCompiled;
    private final int expressionsTotal;
    private final List<FilterCompileError> errors;
    private final List<String> warnings;

    public FilterCompileReport(int filesTotal, int filesCompiled, int expressionsTotal,
                               List<FilterCompileError> errors, List<String> warnings) {
        this.filesTotal = filesTotal;
        this.filesCompiled = filesCompiled;
        this.expressionsTotal = expressionsTotal;
        this.errors = Collections.unmodifiableList(new ArrayList<FilterCompileError>(Objects.requireNonNull(errors, "errors")));
        this.warnings = Collections.unmodifiableList(new ArrayList<String>(Objects.requireNonNull(warnings, "warnings")));
    }

    public int getFilesTotal() {
        return filesTotal;
    }

    public int getFilesCompiled() {
        return filesCompiled;
    }

    public int getExpressionsTotal() {
        return expressionsTotal;
    }

    public List<FilterCompileError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static FilterCompileReport combine(FilterCompileReport first, FilterCompileReport second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        List<FilterCompileError> errors = new ArrayList<FilterCompileError>(first.getErrors());
        errors.addAll(second.getErrors());
        List<String> warnings = new ArrayList<String>(first.getWarnings());
        warnings.addAll(second.getWarnings());
        return new FilterCompileReport(
                first.getFilesTotal() + second.getFilesTotal(),
                first.getFilesCompiled() + second.getFilesCompiled(),
                first.getExpressionsTotal() + second.getExpressionsTotal(),
                errors,
                warnings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterCompileReport)) {
            return false;
        }
        FilterCompileReport that = (FilterCompileReport) o;
        return filesTotal == that.filesTotal
                && filesCompiled == that.filesCompiled
                && expressionsTotal == that.expressionsTotal
                && errors.equals(that.errors)
                && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filesTotal, filesCompiled, expressionsTotal, errors, warnings);
    }

    @Override
    public String toString() {
        return "FilterCompileReport{" +
                "filesTotal=" + filesTotal +
                ", filesCompiled=" + filesCompiled +
                ", expressionsTotal=" + expressionsTotal +
                ", errors=" + errors +
                ", warnings=" + warnings +
                '}';
    }
}
