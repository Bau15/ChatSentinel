package dev._2lstudios.chatsentinel.shared.modules;

import dev._2lstudios.chatsentinel.shared.utils.PlaceholderUtil;

public class SpyModule {
    private static final String DEFAULT_PERMISSION = "chatsentinel.spy";
    private static final String DEFAULT_FORMAT = "&8[&bCS Spy&8] &e%player% &7failed &6%module% &8file=&f%source_file% &8match=&c%matched_text% &8msg=&f%message%";

    private boolean enabled = true;
    private String permission = DEFAULT_PERMISSION;
    private String format = DEFAULT_FORMAT;

    public void loadData(boolean enabled, String permission, String format) {
        this.enabled = enabled;
        this.permission = permission == null || permission.trim().isEmpty() ? DEFAULT_PERMISSION : permission;
        this.format = format == null || format.trim().isEmpty() ? DEFAULT_FORMAT : format;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPermission() {
        return permission;
    }

    public String format(String[][] placeholders) {
        return PlaceholderUtil.replacePlaceholders(format, placeholders);
    }
}
