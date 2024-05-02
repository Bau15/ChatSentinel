package dev._2lstudios.chatsentinel.bungee;

import java.util.concurrent.TimeUnit;

import dev._2lstudios.chatsentinel.bungee.commands.ChatSentinelCommand;
import dev._2lstudios.chatsentinel.bungee.listeners.ChatListener;
import dev._2lstudios.chatsentinel.bungee.listeners.PlayerDisconnectListener;
import dev._2lstudios.chatsentinel.bungee.listeners.PostLoginListener;
import dev._2lstudios.chatsentinel.bungee.modules.BungeeModuleManager;
import dev._2lstudios.chatsentinel.bungee.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.CooldownModule;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import dev._2lstudios.chatsentinel.shared.modules.Module;
import dev._2lstudios.chatsentinel.shared.modules.SyntaxModule;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class ChatSentinel extends Plugin {
	// Static instance
	private static ChatSentinel instance;

	public static ChatSentinel getInstance() {
		return instance;
	}

	public static void setInstance(ChatSentinel instance) {
		ChatSentinel.instance = instance;
	}

	// Module Manager
	private BungeeModuleManager moduleManager;

	public BungeeModuleManager getModuleManager() {
		return moduleManager;
	}

	@Override
	public void onEnable() {
		setInstance(this);

		ConfigUtil configUtil = new ConfigUtil(this);

		configUtil.create("%datafolder%/config.yml");
		configUtil.create("%datafolder%/messages.yml");
		configUtil.create("%datafolder%/whitelist.yml");
		configUtil.create("%datafolder%/blacklist.yml");

		ProxyServer server = getProxy();
		moduleManager = new BungeeModuleManager(configUtil);
		GeneralModule generalModule = moduleManager.getGeneralModule();
		ChatPlayerManager chatPlayerManager = new ChatPlayerManager();
		PluginManager pluginManager = server.getPluginManager();

		pluginManager.registerListener(this, new ChatListener(chatPlayerManager));
		pluginManager.registerListener(this, new PlayerDisconnectListener(generalModule));
		pluginManager.registerListener(this, new PostLoginListener(generalModule, chatPlayerManager));

		pluginManager.registerCommand(this, new ChatSentinelCommand(chatPlayerManager, moduleManager, server));

		getProxy().getScheduler().schedule(this, () -> {
			if (generalModule.needsNicknameCompile()) {
				generalModule.compileNicknamesPattern();
			}
		}, 1000L, 1000L, TimeUnit.MILLISECONDS);
	}

	public void dispatchCommmands(Module module, ChatPlayer chatPlayer, String[][] placeholders) {
		ProxyServer server = getProxy();

		server.getScheduler().runAsync(this, () -> {
			CommandSender console = server.getConsole();

			for (String command : module.getCommands(placeholders)) {
				server.getPluginManager().dispatchCommand(console, command);
			}
		});

		chatPlayer.clearWarns();
	}

	public void dispatchNotification(Module module, String[][] placeholders) {
		ProxyServer server = getProxy();
		String notificationMessage = module.getWarnNotification(placeholders);

		if (notificationMessage != null && !notificationMessage.isEmpty()) {
			for (ProxiedPlayer player1 : server.getPlayers()) {
				if (player1.hasPermission("chatsentinel.notify"))
					player1.sendMessage(notificationMessage);
			}

			server.getConsole().sendMessage(notificationMessage);
		}
	}

	public String[][] getPlaceholders(ProxiedPlayer player, ChatPlayer chatPlayer, Module module, String message) {
		String playerName = player.getName();
		int warns = chatPlayer.getWarns(module);
		int maxWarns = module.getMaxWarns();
		float remainingTime = moduleManager.getCooldownModule().getRemainingTime(chatPlayer, message);

		return new String[][] {
				{ "%player%", "%message%", "%warns%", "%maxwarns%", "%cooldown%" },
				{ playerName, message, String.valueOf(warns), String.valueOf(maxWarns), String.valueOf(remainingTime) }
		};
	}

	public void sendWarning(String[][] placeholders, Module module, ProxiedPlayer player, String lang) {
		String warnMessage = moduleManager.getMessagesModule().getWarnMessage(placeholders, lang, module.getName());

		if (warnMessage != null && !warnMessage.isEmpty()) {
			player.sendMessage(warnMessage);
		}
	}

	public ChatEventResult processEvent(ChatPlayer chatPlayer, ProxiedPlayer player, String originalMessage) {
		ChatEventResult finalResult = new ChatEventResult(originalMessage, false, false);
		MessagesModule messagesModule = moduleManager.getMessagesModule();
		String playerName = player.getName();
		String lang = chatPlayer.getLocale();
		Module[] modulesToProcess = {
				moduleManager.getSyntaxModule(),
				moduleManager.getCapsModule(),
				moduleManager.getCooldownModule(),
				moduleManager.getFloodModule(),
				moduleManager.getBlacklistModule()
		};

		for (Module module : modulesToProcess) {
			// Do not check annormal commands (unless syntax or cooldown)
			boolean isCommmand = originalMessage.startsWith("/");
			boolean isNormalCommmand = ChatSentinel.getInstance().getModuleManager().getGeneralModule()
					.isCommand(originalMessage);
			if (!(module instanceof SyntaxModule) &&
					!(module instanceof CooldownModule) &&
					isCommmand &&
					!isNormalCommmand) {
				continue;
			}

			// Get the modified message
			String message = finalResult.getMessage();

			// Check if player has bypass
			if (player.hasPermission(module.getBypassPermission())) {
				continue;
			}

			// Process
			ChatEventResult result = module.processEvent(chatPlayer, messagesModule, playerName, message, lang);

			// Skip result
			if (result != null) {
				// Add warning
				chatPlayer.addWarn(module);

				// Get placeholders
				String[][] placeholders = ChatSentinel.getInstance().getPlaceholders(player, chatPlayer, module,
						message);

				// Send warning
				ChatSentinel.getInstance().sendWarning(placeholders, module, player, lang);

				// Send punishment comamnds
				if (module.hasExceededWarns(chatPlayer)) {
					ChatSentinel.getInstance().dispatchCommmands(module, chatPlayer, placeholders);
				}

				// Send admin notification
				ChatSentinel.getInstance().dispatchNotification(module, placeholders);

				// Update message
				finalResult.setMessage(result.getMessage());

				// Update hide
				if (result.isHide())
					finalResult.setHide(true);

				// Update cancelled
				if (result.isCancelled()) {
					finalResult.setCancelled(true);
					break;
				}
			}
		}

		return finalResult;
	}
}