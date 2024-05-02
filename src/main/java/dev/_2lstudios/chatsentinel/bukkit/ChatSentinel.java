package dev._2lstudios.chatsentinel.bukkit;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev._2lstudios.chatsentinel.bukkit.commands.ChatSentinelCommand;
import dev._2lstudios.chatsentinel.bukkit.listeners.AsyncPlayerChatListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.PlayerJoinListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.PlayerQuitListener;
import dev._2lstudios.chatsentinel.bukkit.listeners.ServerCommandListener;
import dev._2lstudios.chatsentinel.bukkit.modules.BukkitModuleManager;
import dev._2lstudios.chatsentinel.bukkit.utils.ConfigUtil;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.modules.CooldownModule;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import dev._2lstudios.chatsentinel.shared.modules.Module;
import dev._2lstudios.chatsentinel.shared.modules.SyntaxModule;

public class ChatSentinel extends JavaPlugin {
	// Static instance
	private static ChatSentinel instance;

	public static ChatSentinel getInstance() {
		return instance;
	}

	public static void setInstance(ChatSentinel instance) {
		ChatSentinel.instance = instance;
	}

	// Module Manager
	private BukkitModuleManager moduleManager;

	public BukkitModuleManager getModuleManager() {
		return moduleManager;
	}

	@Override
	public void onEnable() {
		setInstance(this);

		ConfigUtil configUtil = new ConfigUtil(this);
		Server server = getServer();

		moduleManager = new BukkitModuleManager(configUtil);
		GeneralModule generalModule = moduleManager.getGeneralModule();
		ChatPlayerManager chatPlayerManager = new ChatPlayerManager();
		PluginManager pluginManager = server.getPluginManager();

		pluginManager.registerEvents(new AsyncPlayerChatListener(chatPlayerManager), this);
		pluginManager.registerEvents(new PlayerJoinListener(generalModule, chatPlayerManager), this);
		pluginManager.registerEvents(new PlayerQuitListener(moduleManager.getGeneralModule()), this);
		pluginManager.registerEvents(new ServerCommandListener(chatPlayerManager), this);

		getCommand("chatsentinel").setExecutor(new ChatSentinelCommand(chatPlayerManager, moduleManager, server));

		getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
			if (generalModule.needsNicknameCompile()) {
				generalModule.compileNicknamesPattern();
			}
		}, 20L, 20L);
	}

	public void dispatchCommmands(Module module, ChatPlayer chatPlayer, String[][] placeholders) {
		Server server = getServer();

		server.getScheduler().runTask(this, () -> {
			ConsoleCommandSender console = server.getConsoleSender();

			for (String command : module.getCommands(placeholders)) {
				server.dispatchCommand(console, command);
			}
		});

		chatPlayer.clearWarns();
	}

	public void dispatchNotification(Module module, String[][] placeholders) {
		Server server = getServer();
		String notificationMessage = module.getWarnNotification(placeholders);

		if (notificationMessage != null && !notificationMessage.isEmpty()) {
			for (Player player1 : server.getOnlinePlayers()) {
				if (player1.hasPermission("chatsentinel.notify"))
					player1.sendMessage(notificationMessage);
			}

			server.getConsoleSender().sendMessage(notificationMessage);
		}
	}

	public String[][] getPlaceholders(Player player, ChatPlayer chatPlayer, Module module, String message) {
		String playerName = player.getName();
		int warns = chatPlayer.getWarns(module);
		int maxWarns = module.getMaxWarns();
		float remainingTime = moduleManager.getCooldownModule().getRemainingTime(chatPlayer, message);

		return new String[][] {
				{ "%player%", "%message%", "%warns%", "%maxwarns%", "%cooldown%" },
				{ playerName, message, String.valueOf(warns), String.valueOf(maxWarns), String.valueOf(remainingTime) }
		};
	}

	public void sendWarning(String[][] placeholders, Module module, Player player, String lang) {
		String warnMessage = moduleManager.getMessagesModule().getWarnMessage(placeholders, lang, module.getName());

		if (warnMessage != null && !warnMessage.isEmpty()) {
			player.sendMessage(warnMessage);
		}
	}

	public ChatEventResult processEvent(ChatPlayer chatPlayer, Player player, String originalMessage) {
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