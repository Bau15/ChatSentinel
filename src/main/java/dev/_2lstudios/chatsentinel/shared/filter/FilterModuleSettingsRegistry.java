package dev._2lstudios.chatsentinel.shared.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class FilterModuleSettingsRegistry {
    private final Map<String, FilterModuleSettings> settingsByModuleId;
    private final FilterModuleSettings fallback;

    public FilterModuleSettingsRegistry(Map<String, FilterModuleSettings> settingsByModuleId, FilterModuleSettings fallback) {
        this.settingsByModuleId = Collections.unmodifiableMap(new HashMap<String, FilterModuleSettings>(Objects.requireNonNull(settingsByModuleId, "settingsByModuleId")));
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    public FilterModuleSettings resolve(String moduleId) {
        if (moduleId == null || moduleId.isEmpty()) {
            return fallback;
        }
        FilterModuleSettings settings = settingsByModuleId.get(moduleId);
        return settings == null ? fallback : settings;
    }

    public Collection<FilterModuleSettings> all() {
        return settingsByModuleId.values();
    }

    public Set<String> moduleIds() {
        return settingsByModuleId.keySet();
    }
}
