package dev._2lstudios.chatsentinel.velocity.modules;

import dev._2lstudios.chatsentinel.shared.filter.CompiledFilterRegistry;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileReport;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileStatus;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompiler;
import dev._2lstudios.chatsentinel.shared.filter.FilterExpressionFile;
import dev._2lstudios.chatsentinel.shared.filter.FilterKind;
import dev._2lstudios.chatsentinel.shared.filter.FilterModuleSettings;
import dev._2lstudios.chatsentinel.shared.filter.FilterModuleSettingsRegistry;
import dev._2lstudios.chatsentinel.velocity.ChatSentinel;
import dev._2lstudios.chatsentinel.velocity.filter.VelocityFilterFileLoader;
import dev._2lstudios.chatsentinel.velocity.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.modules.AllowedCharactersModule;
import dev._2lstudios.chatsentinel.shared.modules.ChatSnapshotModule;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VelocityModuleManager extends ModuleManager {
	private static final String SIGNED_CHAT_POLICY_LEGACY_DENY = "legacy-deny";
	private static final String SIGNED_CHAT_POLICY_WARN_ONLY = "warn-only";

	private final ConfigUtil configUtil;
	private final Path dataDirectory;
	private final ChatSentinel plugin;
	private String signedChatPolicy = SIGNED_CHAT_POLICY_LEGACY_DENY;
	private boolean signedChatWarningOnStartup = true;

	public VelocityModuleManager(ConfigUtil configUtil) {
		this.configUtil = configUtil;
		this.plugin = resolvePlugin(configUtil);
		this.dataDirectory = plugin.getDataDirectory();
		reloadData();
	}

	@Override
	public void reloadData(FilterCompileStatus status) {
		configUtil.create("config.yml");
		configUtil.create("messages.yml");
		configUtil.create("blacklist.yml");
		configUtil.create("whitelist.yml");

		CommentedConfigurationNode configYml = configUtil.get("config.yml");
		CommentedConfigurationNode messagesYml = configUtil.get("messages.yml");
		CommentedConfigurationNode whitelistYml = configUtil.get("whitelist.yml");
		Map<String, Map<String, String>> locales = new HashMap<>();
		loadVelocitySettings(configYml);

		for (Object lang : messagesYml.node("langs").childrenMap().keySet()) {
			ConfigurationNode langSection = messagesYml.node("langs", lang);
			Map<String, String> messages = new HashMap<>();

			for (Object key : langSection.childrenMap().keySet()) {
				String value = langSection.node(key).getString();

				messages.put((String) key, value);
			}

			locales.put((String) lang, messages);
		}

		boolean hasCapitalizationConfig = !configYml.node("capitalization").virtual();
		Object capitalizationKey = hasCapitalizationConfig ? "capitalization" : "caps";
		getCapitalizationModule().loadData(configYml.node(capitalizationKey, "enabled").getBoolean(true),
				configYml.node(capitalizationKey, "custom-module-name").getString("Capitalization"),
				configYml.node(hasCapitalizationConfig ? "capitalization" : "caps", hasCapitalizationConfig ? "correct" : "replace").getBoolean(true),
				configYml.node(hasCapitalizationConfig ? "capitalization" : "caps", hasCapitalizationConfig ? "max-uppercase" : "max").getInt(8),
				configYml.node(capitalizationKey, "warn", "max").getInt(-1),
				configYml.node(capitalizationKey, "warn", "notification").getString(""),
				configYml.node(capitalizationKey, "warn", "webhook-notification").getBoolean(true),
				configYml.node(capitalizationKey, "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				configYml.node(capitalizationKey, "whitelist-player-names").getBoolean(true),
				configYml.node(capitalizationKey, "whitelist").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				() -> plugin.getServer().getAllPlayers().stream()
						.map(player -> player.getUsername())
						.collect(Collectors.toList()),
				configYml.node(capitalizationKey, "bypass-permission").getString("chatsentinel.bypass.capitalization"));
		getCooldownModule().loadData(configYml.node("cooldown", "enabled").getBoolean(),
				configYml.node("cooldown", "time", "repeat-global").getInt(),
				configYml.node("cooldown", "time", "repeat").getInt(),
				configYml.node("cooldown", "time", "normal").getInt(),
				configYml.node("cooldown", "time", "command").getInt());
		getFloodModule().loadData(configYml.node("flood", "enabled").getBoolean(),
				configYml.node("flood", "custom-module-name").getString(),
				configYml.node("flood", "replace").getBoolean(),
				configYml.node("flood", "warn", "max").getInt(), configYml.node("flood", "pattern").getString(),
				configYml.node("flood", "warn", "notification").getString(),
				configYml.node("flood", "warn", "webhook-notification").getBoolean(),
				configYml.node("flood", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		getMessagesModule().loadData(messagesYml.node("default").getString(), locales);
		getServerMuteModule().loadData(configYml.node("server-mute", "enabled").getBoolean(true),
				configYml.node("server-mute", "muted").getBoolean(false),
				configYml.node("server-mute", "bypass-permission").getString("chatsentinel.mute.bypass"));
		getNoMoveChatModule().loadData(configYml.node("no-move-chat", "enabled").getBoolean(false),
				configYml.node("no-move-chat", "bypass-permission").getString("chatsentinel.bypass.no-move-chat"),
				configYml.node("no-move-chat", "min-distance-blocks").getDouble(5.0D),
				configYml.node("no-move-chat", "allow-teleport").getBoolean(true));
		getChatSnapshotModule().loadData(configYml.node("chat-snapshot", "enabled").getBoolean(true),
				configYml.node("chat-snapshot", "history-size").getInt(ChatSnapshotModule.DEFAULT_HISTORY_SIZE),
				configYml.node("chat-snapshot", "clear-lines").getInt(ChatSnapshotModule.DEFAULT_CLEAR_LINES),
				configYml.node("chat-snapshot", "proxy-replay-format").getString(ChatSnapshotModule.DEFAULT_PROXY_REPLAY_FORMAT));
		getGeneralModule().loadData(configYml.node("general", "sanitize").getBoolean(true),
				configYml.node("general", "sanitize-names").getBoolean(true),
				configYml.node("general", "filter-other").getBoolean(false),
				configYml.node("general", "commands").childrenList().stream()
						.map(ConfigurationNode::getString)
						.collect(Collectors.toList()));
		boolean hasAllowedCharactersConfig = !configYml.node("allowed-characters").virtual();
		getAllowedCharactersModule().loadData(
				hasAllowedCharactersConfig
						? configYml.node("allowed-characters", "enabled").getBoolean(false)
						: configYml.node("general", "filter-other").getBoolean(false),
				configYml.node("allowed-characters", "mode").getString(AllowedCharactersModule.DEFAULT_MODE),
				configYml.node("allowed-characters", "allowed-regex").getString(AllowedCharactersModule.DEFAULT_ALLOWED_REGEX),
				configYml.node("allowed-characters", "replacement").getString(AllowedCharactersModule.DEFAULT_REPLACEMENT));
		getWhitelistModule().loadData(configYml.node("whitelist", "enabled").getBoolean(),
				whitelistYml.node("servers").childrenList().stream()
						.map(ConfigurationNode::getString)
						.collect(Collectors.toList()),
				whitelistYml.node("expressions").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		boolean censorshipEnabled = configYml.node("blacklist", "censorship", "enabled").getBoolean(false);
		String censorshipReplacement = configYml.node("blacklist", "censorship", "replacement").getString("***");
		FilterModuleSettings defaultBlacklistSettings = FilterModuleSettings.defaultBlacklist("default",
				configYml.node("blacklist", "enabled").getBoolean(),
				configYml.node("blacklist", "custom-module-name").getString(),
				configYml.node("blacklist", "fake_message").getBoolean(),
				censorshipEnabled,
				censorshipReplacement,
				configYml.node("blacklist", "warn", "max").getInt(),
				configYml.node("blacklist", "warn", "notification").getString(),
				configYml.node("blacklist", "warn", "webhook-notification").getBoolean(),
				configYml.node("blacklist", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				configYml.node("blacklist", "block_raw_message").getBoolean());
		Map<String, FilterModuleSettings> blacklistSettings = buildBlacklistSettings(configYml, defaultBlacklistSettings);
		FilterModuleSettingsRegistry settingsRegistry = new FilterModuleSettingsRegistry(blacklistSettings, defaultBlacklistSettings);
		CompiledFilterRegistry blacklistRegistry;
		CompiledFilterRegistry whitelistRegistry;
		VelocityFilterFileLoader filterFileLoader = new VelocityFilterFileLoader();
		FilterCompiler compiler = new FilterCompiler();
		List<FilterExpressionFile> blacklistFiles = filterFileLoader.load(FilterKind.BLACKLIST, dataDirectory, configUtil);
		List<FilterExpressionFile> whitelistFiles = filterFileLoader.load(FilterKind.WHITELIST, dataDirectory, configUtil);
		if (status != null) {
			status.start(blacklistFiles.size() + whitelistFiles.size());
		}
		FilterCompiler.FilterCompilation blacklistCompilation = compiler.compile(FilterKind.BLACKLIST, blacklistFiles, status);
		FilterCompiler.FilterCompilation whitelistCompilation = compiler.compile(FilterKind.WHITELIST, whitelistFiles, status);
		FilterCompileReport compileReport = FilterCompileReport.combine(blacklistCompilation.getReport(), whitelistCompilation.getReport());
		setLastCompileReport(compileReport);
		if (status != null) {
			status.done(compileReport);
		}
		loadFilterCompileNotification(
				configYml.node("filter-compile", "notify-admins").getBoolean(true),
				configYml.node("filter-compile", "notify-permission").getString("chatsentinel.admin"));
		loadWarningDelivery(configYml.node("warnings", "delivery", "message").getBoolean(true),
				configYml.node("warnings", "delivery", "action-bar").getBoolean(true));
		getSpyModule().loadData(configYml.node("spy", "enabled").getBoolean(true),
				configYml.node("spy", "permission").getString("chatsentinel.spy"),
				configYml.node("spy", "format").getString("&8[&bCS Spy&8] &e%player% &7failed &6%module% &8file=&f%source_file% &8match=&c%matched_text% &8msg=&f%message%"));
		blacklistRegistry = blacklistCompilation.getRegistry();
		whitelistRegistry = configYml.node("whitelist", "enabled").getBoolean()
				? whitelistCompilation.getRegistry()
				: compiler.compile(FilterKind.WHITELIST, Collections.<FilterExpressionFile>emptyList()).getRegistry();
		getBlacklistModule().loadData(blacklistRegistry, whitelistRegistry, settingsRegistry);
		getSyntaxModule().loadData(configYml.node("syntax", "enabled").getBoolean(),
				configYml.node("syntax", "custom-module-name").getString(),
				configYml.node("syntax", "warn", "max").getInt(),
				configYml.node("syntax", "warn", "notification").getString(),
				configYml.node("syntax", "warn", "webhook-notification").getBoolean(),
				configYml.node("syntax", "whitelist").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new),
				configYml.node("syntax", "punishments").childrenList().stream()
						.map(ConfigurationNode::getString)
						.toArray(String[]::new));
		getDiscordWebhookModule().loadData(configYml.node("discord-webhook", "enabled").getBoolean(),
				configYml.node("discord-webhook", "webhook-url").getString(),
				configYml.node("discord-webhook", "sender", "username").getString(),
				configYml.node("discord-webhook", "sender", "avatar-url").getString(),
				configYml.node("discord-webhook", "author", "name").getString(),
				configYml.node("discord-webhook", "author", "url").getString(),
				configYml.node("discord-webhook", "author", "icon-url").getString(),
				configYml.node("discord-webhook", "title").getString(),
				configYml.node("discord-webhook", "color").getString(),
				configYml.node("discord-webhook", "description").getString(),
				configYml.node("discord-webhook", "field-names", "message").getString(),
				configYml.node("discord-webhook", "field-names", "server").getString(),
				configYml.node("discord-webhook", "field-names", "source-file").getString(),
				configYml.node("discord-webhook", "field-names", "source-module").getString(),
				configYml.node("discord-webhook", "field-names", "matched-text").getString(),
				configYml.node("discord-webhook", "footer", "text").getString(),
				configYml.node("discord-webhook", "footer", "icon-url").getString(),
				configYml.node("discord-webhook", "thumbnail-url").getString());
	}

	public String getSignedChatPolicy() {
		return signedChatPolicy;
	}

	public boolean isSignedChatWarnOnly() {
		return SIGNED_CHAT_POLICY_WARN_ONLY.equals(signedChatPolicy);
	}

	public boolean isSignedChatWarningOnStartup() {
		return signedChatWarningOnStartup;
	}

	private void loadVelocitySettings(CommentedConfigurationNode configYml) {
		String policy = configYml.node("velocity", "signed-chat-policy").getString(SIGNED_CHAT_POLICY_LEGACY_DENY);
		if (SIGNED_CHAT_POLICY_WARN_ONLY.equalsIgnoreCase(policy)) {
			signedChatPolicy = SIGNED_CHAT_POLICY_WARN_ONLY;
		} else {
			signedChatPolicy = SIGNED_CHAT_POLICY_LEGACY_DENY;
		}
		signedChatWarningOnStartup = configYml.node("velocity", "signed-chat-warning-on-startup").getBoolean(true);
	}

	private Map<String, FilterModuleSettings> buildBlacklistSettings(CommentedConfigurationNode configYml,
			FilterModuleSettings fallback) {
		Map<String, FilterModuleSettings> settingsByModuleId = new HashMap<String, FilterModuleSettings>();
		settingsByModuleId.put(fallback.getModuleId(), fallback);

		for (Object key : configYml.node("blacklist", "modules").childrenMap().keySet()) {
			String moduleId = String.valueOf(key);
			ConfigurationNode moduleNode = configYml.node("blacklist", "modules", key);
			FilterModuleSettings settings = FilterModuleSettings.defaultBlacklist(moduleId,
					moduleNode.node("enabled").getBoolean(fallback.isEnabled()),
					moduleNode.node("custom-module-name").getString(fallback.getCustomName()),
					moduleNode.node("fake_message").getBoolean(fallback.isFakeMessage()),
					moduleNode.node("censorship", "enabled").getBoolean(fallback.isCensorshipEnabled()),
					moduleNode.node("censorship", "replacement").getString(fallback.getCensorshipReplacement()),
					moduleNode.node("warn", "max").getInt(fallback.getMaxWarns()),
					moduleNode.node("warn", "notification").getString(fallback.getWarnNotification()),
					moduleNode.node("warn", "webhook-notification").getBoolean(fallback.isWebhookEnabled()),
					moduleNode.node("punishments").childrenList().isEmpty()
							? fallback.getCommands()
							: moduleNode.node("punishments").childrenList().stream()
									.map(ConfigurationNode::getString)
									.toArray(String[]::new),
					moduleNode.node("block_raw_message").getBoolean(fallback.isBlockRawMessage()));
			settingsByModuleId.put(settings.getModuleId(), settings);
		}

		return settingsByModuleId;
	}

	private ChatSentinel resolvePlugin(ConfigUtil configUtil) {
		try {
			Field pluginField = ConfigUtil.class.getDeclaredField("plugin");
			pluginField.setAccessible(true);
			return (ChatSentinel) pluginField.get(configUtil);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to resolve Velocity plugin", e);
		}
	}
}
