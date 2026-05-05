package dev._2lstudios.chatsentinel.shared.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class FilterCompiler {
    private static final Pattern NEVER_MATCH = Pattern.compile("(?!)");

    public FilterCompilation compile(FilterKind kind, List<FilterExpressionFile> files) {
        return compile(kind, files, null);
    }

    public FilterCompilation compile(FilterKind kind, List<FilterExpressionFile> files, FilterCompileStatus status) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(files, "files");

        List<FilterExpressionFile> sortedFiles = new ArrayList<FilterExpressionFile>(files);
        Collections.sort(sortedFiles, new Comparator<FilterExpressionFile>() {
            @Override
            public int compare(FilterExpressionFile first, FilterExpressionFile second) {
                return first.getSource().getRelativePath().compareTo(second.getSource().getRelativePath());
            }
        });

        List<CompiledFilterFile> compiledFiles = new ArrayList<CompiledFilterFile>();
        List<FilterCompileError> errors = new ArrayList<FilterCompileError>();
        List<String> warnings = new ArrayList<String>();
        int expressionsTotal = 0;

        for (FilterExpressionFile file : sortedFiles) {
            FilePattern filePattern = buildPattern(file, errors, warnings);
            expressionsTotal += filePattern.expressionCount;
            try {
                Pattern pattern = filePattern.expressionCount == 0
                        ? NEVER_MATCH
                        : Pattern.compile(filePattern.pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                compiledFiles.add(new CompiledFilterFile(file.getSource(), pattern, filePattern.expressionCount));
            } catch (PatternSyntaxException exception) {
                errors.add(new FilterCompileError(file.getSource(), -1, filePattern.pattern, exception.getMessage()));
            }
            if (status != null) {
                status.fileCompiled();
            }
        }

        FilterCompileReport report = new FilterCompileReport(files.size(), compiledFiles.size(), expressionsTotal, errors, warnings);
        return new FilterCompilation(new CompiledFilterRegistry(kind, compiledFiles), report);
    }

    private FilePattern buildPattern(FilterExpressionFile file, List<FilterCompileError> errors, List<String> warnings) {
        StringBuilder patternBuilder = new StringBuilder();
        int expressionCount = 0;
        List<String> expressions = file.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            String expression = expressions.get(i);
            if (expression == null || expression.trim().isEmpty()) {
                warnings.add("Skipped blank expression in " + file.getSource().getRelativePath() + " at index " + i);
                continue;
            }
            try {
                Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (PatternSyntaxException exception) {
                errors.add(new FilterCompileError(file.getSource(), i, expression, exception.getMessage()));
                continue;
            }
            if (patternBuilder.length() <= 0) {
                patternBuilder.append('(').append(expression).append(')');
            } else {
                patternBuilder.append("|(").append(expression).append(')');
            }
            expressionCount++;
        }
        return new FilePattern(patternBuilder.toString(), expressionCount);
    }

    public static final class FilterCompilation {
        private final CompiledFilterRegistry registry;
        private final FilterCompileReport report;

        public FilterCompilation(CompiledFilterRegistry registry, FilterCompileReport report) {
            this.registry = Objects.requireNonNull(registry, "registry");
            this.report = Objects.requireNonNull(report, "report");
        }

        public CompiledFilterRegistry getRegistry() {
            return registry;
        }

        public FilterCompileReport getReport() {
            return report;
        }
    }

    private static final class FilePattern {
        private final String pattern;
        private final int expressionCount;

        private FilePattern(String pattern, int expressionCount) {
            this.pattern = pattern;
            this.expressionCount = expressionCount;
        }
    }
}
