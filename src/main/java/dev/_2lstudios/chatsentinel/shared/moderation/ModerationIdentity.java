package dev._2lstudios.chatsentinel.shared.moderation;

import java.util.Objects;

import dev._2lstudios.chatsentinel.shared.filter.FilterModuleSettings;
import dev._2lstudios.chatsentinel.shared.filter.FilterSource;
import dev._2lstudios.chatsentinel.shared.modules.ModerationModule;

public final class ModerationIdentity {
    private final String key;
    private final String moduleName;
    private final String displayName;

    public ModerationIdentity(String key, String moduleName, String displayName) {
        this.key = Objects.requireNonNull(key, "key");
        this.moduleName = Objects.requireNonNull(moduleName, "moduleName");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public static ModerationIdentity fromModule(ModerationModule module) {
        Objects.requireNonNull(module, "module");
        String customName = module.getCustomName();
        String displayName = customName == null || customName.isEmpty() ? module.getName() : customName;
        return new ModerationIdentity(module.getName(), module.getName(), displayName);
    }

    public static ModerationIdentity blacklist(FilterSource source, FilterModuleSettings settings) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(settings, "settings");
        String displayName = settings.getCustomName().isEmpty() ? source.getDisplayName() : settings.getCustomName();
        return new ModerationIdentity(source.getModuleId(), "Blacklist", displayName);
    }

    public String getKey() {
        return key;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModerationIdentity)) {
            return false;
        }
        ModerationIdentity that = (ModerationIdentity) o;
        return key.equals(that.key) && moduleName.equals(that.moduleName) && displayName.equals(that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, moduleName, displayName);
    }

    @Override
    public String toString() {
        return "ModerationIdentity{" +
                "key='" + key + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
