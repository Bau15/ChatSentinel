package dev._2lstudios.chatsentinel.shared.socialspy;

public final class SocialSpyModuleSettings {
    private final String moduleId;
    private final boolean enabled;
    private final boolean defaultEnabled;
    private final String permission;
    private final String format;

    public SocialSpyModuleSettings(final String moduleId, final boolean enabled, final boolean defaultEnabled,
            final String permission, final String format) {
        this.moduleId = requireText(moduleId, "moduleId");
        this.enabled = enabled;
        this.defaultEnabled = defaultEnabled;
        this.permission = requireText(permission, "permission");
        this.format = requireText(format, "format");
    }

    public String getModuleId() {
        return moduleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public String getPermission() {
        return permission;
    }

    public String getFormat() {
        return format;
    }

    private static String requireText(final String value, final String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value;
    }
}
