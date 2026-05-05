package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Objects;

public final class FilterSource {
    private final FilterKind kind;
    private final String moduleId;
    private final String relativePath;
    private final String displayName;

    public FilterSource(FilterKind kind, String moduleId, String relativePath, String displayName) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.moduleId = normalizeModuleId(Objects.requireNonNull(moduleId, "moduleId"));
        this.relativePath = Objects.requireNonNull(relativePath, "relativePath");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public FilterKind getKind() {
        return kind;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static String normalizeModuleId(String moduleId) {
        return moduleId.isEmpty() ? "default" : moduleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterSource)) {
            return false;
        }
        FilterSource that = (FilterSource) o;
        return kind == that.kind
                && moduleId.equals(that.moduleId)
                && relativePath.equals(that.relativePath)
                && displayName.equals(that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, moduleId, relativePath, displayName);
    }

    @Override
    public String toString() {
        return "FilterSource{" +
                "kind=" + kind +
                ", moduleId='" + moduleId + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
