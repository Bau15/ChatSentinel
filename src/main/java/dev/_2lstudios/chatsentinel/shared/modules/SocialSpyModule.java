package dev._2lstudios.chatsentinel.shared.modules;

import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyCommandDefinition;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyCommandParser;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleId;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyModuleSettings;
import dev._2lstudios.chatsentinel.shared.socialspy.SocialSpyTrimSettings;
import dev._2lstudios.chatsentinel.shared.utils.PlaceholderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SocialSpyModule {
    public static final String DEFAULT_ROOT_PERMISSION = "chatsentinel.socialspy";
    public static final boolean DEFAULT_INCLUDE_SELF = false;

    private boolean enabled = true;
    private String rootPermission = DEFAULT_ROOT_PERMISSION;
    private boolean includeSelf = DEFAULT_INCLUDE_SELF;
    private boolean showServer = true;
    private String unavailablePlatformMessage = "&cThis SocialSpy module is not available on this platform.";
    private String invalidModuleMessage = "&cUnknown SocialSpy module. Available: &f%modules%";
    private String enabledMessage = "&aSocialSpy enabled for &f%module%&a.";
    private String disabledMessage = "&cSocialSpy disabled for &f%module%&c.";
    private String enabledAllMessage = "&aSocialSpy enabled for all permitted modules.";
    private String disabledAllMessage = "&cSocialSpy disabled for all permitted modules.";
    private String statusHeader = "&eSocialSpy status:";
    private String statusLine = "&7- &f%module%&7: %state%";
    private String stateEnabled = "&aenabled";
    private String stateDisabled = "&cdisabled";
    private String noPermission = "&cYou do not have permission to use this SocialSpy module.";
    private SocialSpyTrimSettings trimSettings = new SocialSpyTrimSettings(160, 160, 80, 40, 50, "...");
    private Map<String, SocialSpyModuleSettings> settingsById = defaultSettings();
    private SocialSpyCommandParser commandParser = new SocialSpyCommandParser(defaultCommandDefinitions(), defaultIgnoredRoots());

    public void loadData(final boolean enabled, final String rootPermission, final boolean includeSelf,
            final boolean showServer, final String unavailablePlatformMessage, final String invalidModuleMessage,
            final String enabledMessage, final String disabledMessage, final String enabledAllMessage,
            final String disabledAllMessage, final String statusHeader, final String statusLine,
            final String stateEnabled, final String stateDisabled, final String noPermission,
            final SocialSpyTrimSettings trimSettings, final List<SocialSpyModuleSettings> moduleSettings,
            final List<String> commandPatterns, final List<String> ignoredCommandRoots) {
        this.enabled = enabled;
        this.rootPermission = textOrDefault(rootPermission, DEFAULT_ROOT_PERMISSION);
        this.includeSelf = includeSelf;
        this.showServer = showServer;
        this.unavailablePlatformMessage = textOrDefault(unavailablePlatformMessage, this.unavailablePlatformMessage);
        this.invalidModuleMessage = textOrDefault(invalidModuleMessage, this.invalidModuleMessage);
        this.enabledMessage = textOrDefault(enabledMessage, this.enabledMessage);
        this.disabledMessage = textOrDefault(disabledMessage, this.disabledMessage);
        this.enabledAllMessage = textOrDefault(enabledAllMessage, this.enabledAllMessage);
        this.disabledAllMessage = textOrDefault(disabledAllMessage, this.disabledAllMessage);
        this.statusHeader = textOrDefault(statusHeader, this.statusHeader);
        this.statusLine = textOrDefault(statusLine, this.statusLine);
        this.stateEnabled = textOrDefault(stateEnabled, this.stateEnabled);
        this.stateDisabled = textOrDefault(stateDisabled, this.stateDisabled);
        this.noPermission = textOrDefault(noPermission, this.noPermission);
        this.trimSettings = trimSettings == null ? this.trimSettings : trimSettings;
        this.settingsById = mergeSettings(moduleSettings);
        this.commandParser = new SocialSpyCommandParser(parseDefinitions(commandPatterns), mergeIgnoredRoots(ignoredCommandRoots));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIncludeSelf() {
        return includeSelf;
    }

    public boolean isShowServer() {
        return showServer;
    }

    public String getRootPermission() {
        return rootPermission;
    }

    public SocialSpyTrimSettings getTrimSettings() {
        return trimSettings;
    }

    public SocialSpyModuleSettings getSettings(final String moduleId) {
        final String id = SocialSpyModuleId.normalize(moduleId);
        final SocialSpyModuleSettings settings = settingsById.get(id);
        if (settings != null) {
            return settings;
        }
        return defaultSettings().get(SocialSpyModuleId.MESSAGES);
    }

    public List<String> getModuleIds() {
        return SocialSpyModuleId.ids();
    }

    public boolean isModuleEnabled(final String moduleId) {
        return getSettings(moduleId).isEnabled();
    }

    public boolean isDefaultEnabled(final String moduleId) {
        return getSettings(moduleId).isDefaultEnabled();
    }

    public String getPermission(final String moduleId) {
        return getSettings(moduleId).getPermission();
    }

    public String format(final String moduleId, final String[][] placeholders) {
        return PlaceholderUtil.replacePlaceholders(getSettings(moduleId).getFormat(), placeholders);
    }

    public SocialSpyCommandParser getCommandParser() {
        return commandParser;
    }

    public String getInvalidModuleMessage(final String modules) {
        return replace(invalidModuleMessage, "%modules%", modules);
    }

    public String getEnabledMessage(final String module) {
        return replace(enabledMessage, "%module%", module);
    }

    public String getDisabledMessage(final String module) {
        return replace(disabledMessage, "%module%", module);
    }

    public String getEnabledAllMessage() {
        return PlaceholderUtil.replacePlaceholders(enabledAllMessage);
    }

    public String getDisabledAllMessage() {
        return PlaceholderUtil.replacePlaceholders(disabledAllMessage);
    }

    public String getStatusHeader() {
        return PlaceholderUtil.replacePlaceholders(statusHeader);
    }

    public String getStatusLine(final String module, final boolean enabled) {
        final String state = enabled ? stateEnabled : stateDisabled;
        return PlaceholderUtil.replacePlaceholders(statusLine, new String[][] {
                { "%module%", "%state%" },
                { module, PlaceholderUtil.replacePlaceholders(state) }
        });
    }

    public String getNoPermission() {
        return PlaceholderUtil.replacePlaceholders(noPermission);
    }

    public String getUnavailablePlatformMessage() {
        return PlaceholderUtil.replacePlaceholders(unavailablePlatformMessage);
    }

    private String replace(final String message, final String key, final String value) {
        return PlaceholderUtil.replacePlaceholders(message, new String[][] { { key }, { value == null ? "" : value } });
    }

    private Map<String, SocialSpyModuleSettings> mergeSettings(final List<SocialSpyModuleSettings> moduleSettings) {
        final Map<String, SocialSpyModuleSettings> result = defaultSettings();
        if (moduleSettings != null) {
            for (SocialSpyModuleSettings settings : moduleSettings) {
                if (settings != null && SocialSpyModuleId.isValid(settings.getModuleId())) {
                    result.put(SocialSpyModuleId.normalize(settings.getModuleId()), settings);
                }
            }
        }
        return result;
    }

    private static List<SocialSpyCommandDefinition> parseDefinitions(final List<String> commandPatterns) {
        final List<String> patterns = commandPatterns == null || commandPatterns.isEmpty()
                ? defaultPatternStrings()
                : commandPatterns;
        final List<SocialSpyCommandDefinition> result = new ArrayList<SocialSpyCommandDefinition>();
        for (String pattern : patterns) {
            if (pattern == null) {
                continue;
            }
            final String[] parts = pattern.split(":");
            if (parts.length != 3) {
                continue;
            }
            try {
                result.add(new SocialSpyCommandDefinition(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } catch (NumberFormatException ignored) {
                // Bad custom pattern should not break whole module
            }
        }
        return result;
    }

    private static List<String> mergeIgnoredRoots(final List<String> ignoredCommandRoots) {
        final Set<String> roots = new HashSet<String>(defaultIgnoredRoots());
        if (ignoredCommandRoots != null) {
            roots.addAll(ignoredCommandRoots);
        }
        return new ArrayList<String>(roots);
    }

    private static Map<String, SocialSpyModuleSettings> defaultSettings() {
        final Map<String, SocialSpyModuleSettings> result = new HashMap<String, SocialSpyModuleSettings>();
        result.put(SocialSpyModuleId.MESSAGES, new SocialSpyModuleSettings(SocialSpyModuleId.MESSAGES, true, true,
                "chatsentinel.socialspy.messages", "&8[&dSpy&8] &e%player% &7messaged &f%target%&8: &f%message%"));
        result.put(SocialSpyModuleId.SIGNS, new SocialSpyModuleSettings(SocialSpyModuleId.SIGNS, true, true,
                "chatsentinel.socialspy.signs", "&8[&dSpy&8] &e%player% &7placed/edited sign at &f%world% %x% %y% %z%&8: &f%content%"));
        result.put(SocialSpyModuleId.BOOKS, new SocialSpyModuleSettings(SocialSpyModuleId.BOOKS, true, true,
                "chatsentinel.socialspy.books", "&8[&dSpy&8] &e%player% &7wrote a book &f%title%&8: &f%content%"));
        result.put(SocialSpyModuleId.COMMANDS, new SocialSpyModuleSettings(SocialSpyModuleId.COMMANDS, true, false,
                "chatsentinel.socialspy.commands", "&8[&dSpy&8] &e%player% &7ran command&8: &f/%command%"));
        return result;
    }

    private static List<SocialSpyCommandDefinition> defaultCommandDefinitions() {
        return parseDefinitions(defaultPatternStrings());
    }

    private static List<String> defaultPatternStrings() {
        final List<String> patterns = new ArrayList<String>();
        Collections.addAll(patterns, "msg:1:2", "tell:1:2", "w:1:2", "whisper:1:2", "pm:1:2", "m:1:2",
                "message:1:2", "t:1:2", "reply:-1:1", "r:-1:1");
        return patterns;
    }

    private static List<String> defaultIgnoredRoots() {
        final List<String> roots = new ArrayList<String>();
        Collections.addAll(roots, "login", "l", "register", "reg", "changepassword", "cp", "email", "2fa",
                "captcha", "chatsentinel", "socialspy", "sspy", "autocorrect", "correction", "servermute",
                "muteall", "muteserver");
        return roots;
    }

    private static String textOrDefault(final String value, final String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
