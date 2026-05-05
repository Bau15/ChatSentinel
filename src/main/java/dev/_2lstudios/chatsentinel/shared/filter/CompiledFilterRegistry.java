package dev._2lstudios.chatsentinel.shared.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompiledFilterRegistry {
    private final FilterKind kind;
    private final List<CompiledFilterFile> files;

    public CompiledFilterRegistry(FilterKind kind, List<CompiledFilterFile> files) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.files = Collections.unmodifiableList(new ArrayList<CompiledFilterFile>(Objects.requireNonNull(files, "files")));
    }

    public Optional<FilterMatch> findFirst(String input) {
        Objects.requireNonNull(input, "input");
        for (CompiledFilterFile file : files) {
            Optional<FilterMatch> match = file.findFirst(input);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    public int fileCount() {
        return files.size();
    }

    public int expressionCount() {
        int count = 0;
        for (CompiledFilterFile file : files) {
            count += file.getExpressionCount();
        }
        return count;
    }

    public List<CompiledFilterFile> files() {
        return files;
    }

    public FilterKind getKind() {
        return kind;
    }
}
