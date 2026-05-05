package dev._2lstudios.chatsentinel.shared.commands;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.config.MutableModuleConfigStore;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileError;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileReport;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileStatus;
import dev._2lstudios.chatsentinel.shared.filter.UserRegexAddService;
import dev._2lstudios.chatsentinel.shared.modules.MessagesModule;
import dev._2lstudios.chatsentinel.shared.modules.ModerationModule;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import dev._2lstudios.chatsentinel.shared.modules.ChatSnapshotModule;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.platform.CommandActor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ChatSentinelCommandService {
    private static final List<String> SUBCOMMANDS = Arrays.asList("help", "reload", "clear", "notify", "spy", "delete", "module", "regex", "mutechat");

    private final ModuleManager moduleManager;
    private final ChatPlayerManager chatPlayerManager;
    private final ChatNotificationManager chatNotificationManager;
    private final ChatPlatform platform;
    private final UserRegexAddService regexAddService;
    private final MutableModuleConfigStore configStore;

    public ChatSentinelCommandService(final ModuleManager moduleManager, final ChatPlayerManager chatPlayerManager,
            final ChatNotificationManager chatNotificationManager, final ChatPlatform platform,
            final UserRegexAddService regexAddService, final MutableModuleConfigStore configStore) {
        this.moduleManager = moduleManager;
        this.chatPlayerManager = chatPlayerManager;
        this.chatNotificationManager = chatNotificationManager;
        this.platform = platform;
        this.regexAddService = regexAddService;
        this.configStore = configStore;
    }

    public CommandResult execute(final CommandActor actor, final String[] args) {
        final String lang = actor.getLocale();
        final MessagesModule messagesModule = moduleManager.getMessagesModule();
        final String subcommand = args.length == 0 ? "help" : args[0].toLowerCase();

        if ("help".equals(subcommand)) {
            sendIfPermitted(actor, CommandPermission.HELP, messagesModule.getHelp(lang));
        } else if ("reload".equals(subcommand)) {
            handleReload(actor, lang);
        } else if ("clear".equals(subcommand)) {
            handleClear(actor, args, lang);
        } else if ("notify".equals(subcommand)) {
            handleNotify(actor, lang);
        } else if ("spy".equals(subcommand)) {
            handleSpy(actor, lang);
        } else if ("delete".equals(subcommand)) {
            handleDelete(actor, args, lang);
        } else if ("module".equals(subcommand)) {
            handleModule(actor, args, lang);
        } else if ("regex".equals(subcommand)) {
            handleRegex(actor, args, lang);
        } else if ("mutechat".equals(subcommand)) {
            handleMuteChat(actor, args, lang);
        } else {
            actor.sendMessage(messagesModule.getUnknownCommand(lang));
        }
        return CommandResult.handled();
    }

    public List<String> suggest(final CommandActor actor, final String[] args) {
        final String prefix = args.length == 0 ? "" : args[0].toLowerCase();
        final List<String> result = new ArrayList<String>();
        for (String subcommand : SUBCOMMANDS) {
            if (subcommand.startsWith(prefix) && hasPermission(actor, permissionFor(subcommand))) {
                result.add(subcommand);
            }
        }
        return result;
    }

    private void sendIfPermitted(final CommandActor actor, final String permission, final String message) {
        if (!hasPermission(actor, permission)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(actor.getLocale()));
            return;
        }
        actor.sendMessage(message);
    }

    private void handleReload(final CommandActor actor, final String lang) {
        if (!hasPermission(actor, CommandPermission.RELOAD)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        moduleManager.reloadData(createStatus(actor));
        sendCompileErrors(actor, moduleManager.getLastCompileReport());
        actor.sendMessage(moduleManager.getMessagesModule().getReload(lang));
    }

    private void handleClear(final CommandActor actor, final String[] args, final String lang) {
        if (!hasPermission(actor, CommandPermission.CLEAR)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        final String reason = args.length > 1 ? joinArgs(args, 1) : "No reason specified";
        final String[][] placeholders = placeholders(actor.getName(), reason, "0", "0", "");
        final String clearedMessage = moduleManager.getMessagesModule().getCleared(placeholders, lang);
        final String payload = moduleManager.getChatSnapshotModule().buildClearPayload(clearedMessage);
        int cleared = 0;
        int bypassed = 0;
        for (ChatUser user : platform.getOnlineUsers()) {
            if (user.hasPermission(CommandPermission.CLEAR_BYPASS)) {
                user.sendMessage(moduleManager.getMessagesModule().getClearBypassNotice(placeholders, user.getLocale()));
                bypassed++;
            } else {
                user.sendMessage(payload);
                cleared++;
            }
        }
        actor.sendMessage(moduleManager.getMessagesModule().getClearSenderSummary(
                placeholders(actor.getName(), reason, String.valueOf(cleared), String.valueOf(bypassed), ""), lang));
    }

    private void handleNotify(final CommandActor actor, final String lang) {
        if (!hasPermission(actor, CommandPermission.NOTIFY)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        final ChatUser user = actor.asUserOrNull();
        if (user == null) {
            actor.sendMessage(moduleManager.getMessagesModule().getUnknownCommand(lang));
            return;
        }
        final ChatPlayer chatPlayer = chatPlayerManager.getPlayer(user);
        if (chatNotificationManager.containsPlayer(chatPlayer)) {
            chatNotificationManager.removePlayer(chatPlayer);
            actor.sendMessage(moduleManager.getMessagesModule().getNotifyDisabled(lang));
        } else {
            chatNotificationManager.addPlayer(chatPlayer);
            actor.sendMessage(moduleManager.getMessagesModule().getNotifyEnabled(lang));
        }
    }

    private void handleSpy(final CommandActor actor, final String lang) {
        if (!hasPermission(actor, moduleManager.getSpyModule().getPermission())) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        final ChatUser user = actor.asUserOrNull();
        if (user == null) {
            actor.sendMessage("Only players can use spy.");
            return;
        }
        final ChatPlayer chatPlayer = chatPlayerManager.getPlayer(user);
        chatPlayer.setSpy(!chatPlayer.isSpy());
        actor.sendMessage("Spy " + (chatPlayer.isSpy() ? "enabled" : "disabled") + ".");
    }

    private void handleDelete(final CommandActor actor, final String[] args, final String lang) {
        if (!hasPermission(actor, CommandPermission.DELETE)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        if (args.length >= 2 && "list".equalsIgnoreCase(args[1])) {
            handleDeleteList(actor, args, lang);
            return;
        }
        if (args.length != 2) {
            actor.sendMessage(moduleManager.getMessagesModule().getDeleteUsage(lang));
            return;
        }
        Optional<ChatSnapshotModule.Entry> deleted = moduleManager.getChatSnapshotModule().markDeletedEntry(args[1]);
        if (!deleted.isPresent()) {
            actor.sendMessage(moduleManager.getMessagesModule().getDeleteUnknown(placeholders(actor.getName(), "", "0", "0", args[1]), lang));
            return;
        }
        for (ChatUser user : platform.getOnlineUsers()) {
            if (user.hasPermission(CommandPermission.CLEAR_BYPASS)) {
                user.sendMessage(moduleManager.getMessagesModule().getDeleteBypassNotice(
                        placeholders(actor.getName(), "", "0", "0", deleted.get().getId()), user.getLocale()));
            } else {
                user.sendMessage(moduleManager.getChatSnapshotModule().buildReplayPayload(user.getUniqueId()));
            }
        }
        actor.sendMessage(moduleManager.getMessagesModule().getDeleteDone(placeholders(actor.getName(), "", "0", "0", deleted.get().getId()), lang));
    }

    private void handleDeleteList(final CommandActor actor, final String[] args, final String lang) {
        int limit = 10;
        if (args.length > 3) {
            actor.sendMessage(moduleManager.getMessagesModule().getDeleteUsage(lang));
            return;
        }
        if (args.length == 3) {
            try {
                limit = Math.max(1, Math.min(40, Integer.parseInt(args[2])));
            } catch (NumberFormatException exception) {
                actor.sendMessage(moduleManager.getMessagesModule().getDeleteUsage(lang));
                return;
            }
        }
        actor.sendMessage(moduleManager.getMessagesModule().getDeleteListHeader(lang));
        List<ChatSnapshotModule.Entry> entries = moduleManager.getChatSnapshotModule().getRecentEntries();
        int start = Math.max(0, entries.size() - limit);
        for (int i = start; i < entries.size(); i++) {
            ChatSnapshotModule.Entry entry = entries.get(i);
            actor.sendMessage(moduleManager.getMessagesModule().getDeleteListEntry(new String[][] {
                    { "%id%", "%player%", "%message%", "%status%" },
                    { entry.getId(), entry.getSenderName(), entry.getMessage(), entry.isDeleted() ? "[deleted]" : "" }
            }, lang));
        }
    }

    private void handleModule(final CommandActor actor, final String[] args, final String lang) {
        if (!hasPermission(actor, CommandPermission.MODULE)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        if (args.length == 2 && "list".equalsIgnoreCase(args[1])) {
            actor.sendMessage(moduleList());
            return;
        }
        if (args.length == 3 && ("enable".equalsIgnoreCase(args[1]) || "disable".equalsIgnoreCase(args[1]))) {
            final boolean enabled = "enable".equalsIgnoreCase(args[1]);
            final String path = modulePath(args[2]);
            if (path == null) {
                actor.sendMessage("Unknown module: " + args[2]);
                return;
            }
            try {
                configStore.setBoolean(path, enabled);
                moduleManager.reloadData(createStatus(actor));
                actor.sendMessage("Module " + args[2].toLowerCase() + " " + (enabled ? "enabled" : "disabled") + ".");
            } catch (IOException exception) {
                actor.sendMessage("Module save failed: " + exception.getMessage());
            }
            return;
        }
        actor.sendMessage("Usage: /chatsentinel module list|enable <moduleId>|disable <moduleId>");
    }

    private void handleRegex(final CommandActor actor, final String[] args, final String lang) {
        if (!hasPermission(actor, CommandPermission.REGEX_ADD)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        if (args.length < 5 || !"add".equalsIgnoreCase(args[1])) {
            actor.sendMessage("Usage: /chatsentinel regex add <moduleId> common <plain...>|raw <regex...>");
            return;
        }
        try {
            final String expression = "common".equalsIgnoreCase(args[3])
                    ? regexAddService.addCommon(args[2], joinArgs(args, 4))
                    : "raw".equalsIgnoreCase(args[3]) ? regexAddService.addRaw(args[2], joinArgs(args, 4)) : null;
            if (expression == null) {
                actor.sendMessage("Usage: /chatsentinel regex add <moduleId> common <plain...>|raw <regex...>");
                return;
            }
            moduleManager.reloadData(createStatus(actor));
            sendCompileErrors(actor, moduleManager.getLastCompileReport());
            actor.sendMessage("Added regex to module " + args[2] + ": " + expression);
        } catch (IllegalArgumentException | IOException exception) {
            actor.sendMessage("Regex add failed: " + exception.getMessage());
        }
    }

    private void handleMuteChat(final CommandActor actor, final String[] args, final String lang) {
        if (!hasPermission(actor, CommandPermission.MUTE)) {
            actor.sendMessage(moduleManager.getMessagesModule().getNoPermission(lang));
            return;
        }
        if (args.length < 2) {
            actor.sendMessage("Usage: /chatsentinel mutechat <on|off|toggle> [reason...]");
            return;
        }
        final boolean muted;
        if ("on".equalsIgnoreCase(args[1])) {
            muted = true;
        } else if ("off".equalsIgnoreCase(args[1])) {
            muted = false;
        } else if ("toggle".equalsIgnoreCase(args[1])) {
            muted = !moduleManager.getServerMuteModule().isMuted();
        } else {
            actor.sendMessage("Usage: /chatsentinel mutechat <on|off|toggle> [reason...]");
            return;
        }
        final String reason = args.length > 2 ? joinArgs(args, 2) : "No reason specified";
        try {
            configStore.setBoolean("server-mute.muted", muted);
            moduleManager.reloadData(createStatus(actor));
            String[][] placeholders = new String[][] {
                    { "%player%", "%reason%" },
                    { actor.getName(), reason }
            };
            actor.sendMessage(muted ? moduleManager.getMessagesModule().getServerMuteEnabled(placeholders, lang)
                    : moduleManager.getMessagesModule().getServerMuteDisabled(placeholders, lang));
        } catch (IOException exception) {
            actor.sendMessage("Mutechat save failed: " + exception.getMessage());
        }
    }

    private boolean hasPermission(final CommandActor actor, final String permission) {
        return actor.hasPermission(permission) || actor.hasPermission(CommandPermission.ADMIN);
    }

    private String permissionFor(final String subcommand) {
        if ("reload".equals(subcommand)) return CommandPermission.RELOAD;
        if ("clear".equals(subcommand)) return CommandPermission.CLEAR;
        if ("notify".equals(subcommand)) return CommandPermission.NOTIFY;
        if ("module".equals(subcommand)) return CommandPermission.MODULE;
        if ("regex".equals(subcommand)) return CommandPermission.REGEX_ADD;
        if ("mutechat".equals(subcommand)) return CommandPermission.MUTE;
        if ("spy".equals(subcommand)) return moduleManager.getSpyModule().getPermission();
        if ("delete".equals(subcommand)) return CommandPermission.DELETE;
        return CommandPermission.HELP;
    }

    private String modulePath(final String moduleId) {
        final String id = normalizeModuleId(moduleId);
        if ("blacklist".equals(id)) return "blacklist.enabled";
        if ("capitalization".equals(id) || "caps".equals(id)) return "capitalization.enabled";
        if ("cooldown".equals(id)) return "cooldown.enabled";
        if ("flood".equals(id)) return "flood.enabled";
        if ("syntax".equals(id)) return "syntax.enabled";
        if ("whitelist".equals(id)) return "whitelist.enabled";
        if ("server-mute".equals(id)) return "server-mute.enabled";
        if ("allowed-characters".equals(id)) return "allowed-characters.enabled";
        if ("no-move-chat".equals(id)) return "no-move-chat.enabled";
        if (id.startsWith("blacklist/")) return "blacklist.modules." + id.substring("blacklist/".length()) + ".enabled";
        if (moduleManager.getBlacklistModule().getSettingsRegistry().moduleIds().contains(id)) return "blacklist.modules." + id + ".enabled";
        return null;
    }

    private String normalizeModuleId(final String moduleId) {
        return moduleId == null ? "" : moduleId.trim().toLowerCase().replace('_', '-');
    }

    private String moduleList() {
        final StringBuilder builder = new StringBuilder("Modules:");
        appendModule(builder, "blacklist", moduleManager.getBlacklistModule().isEnabled());
        appendModule(builder, "capitalization", moduleManager.getCapitalizationModule().isEnabled());
        appendModule(builder, "cooldown", moduleManager.getCooldownModule().isEnabled());
        appendModule(builder, "flood", moduleManager.getFloodModule().isEnabled());
        appendModule(builder, "syntax", moduleManager.getSyntaxModule().isEnabled());
        appendModule(builder, "whitelist", moduleManager.getWhitelistModule().isEnabled());
        final Set<String> moduleIds = moduleManager.getBlacklistModule().getSettingsRegistry().moduleIds();
        final List<String> sortedIds = new ArrayList<String>(moduleIds);
        Collections.sort(sortedIds);
        for (String moduleId : sortedIds) {
            if (!"default".equals(moduleId)) {
                appendModule(builder, "blacklist/" + moduleId, moduleManager.getBlacklistModule().getSettingsRegistry().resolve(moduleId).isEnabled());
            }
        }
        return builder.toString();
    }

    private void appendModule(final StringBuilder builder, final String moduleId, final boolean enabled) {
        builder.append("\n- ").append(moduleId).append(": ").append(enabled ? "enabled" : "disabled");
    }

    private String[][] placeholders(final String player, final String reason, final String cleared, final String bypassed, final String id) {
        return new String[][] {
                { "%player%", "%reason%", "%cleared%", "%bypassed%", "%id%" },
                { player, reason, cleared, bypassed, id }
        };
    }

    private String joinArgs(final String[] args, final int startIndex) {
        final StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private FilterCompileStatus createStatus(final CommandActor actor) {
        return new FilterCompileStatus(new FilterCompileStatus.MessageSink() {
            @Override
            public void send(final String message) {
                actor.sendMessage(message);
                platform.sendConsoleMessage(message);
            }
        });
    }

    private void sendCompileErrors(final CommandActor actor, final FilterCompileReport report) {
        if (report == null || !report.hasErrors()) {
            return;
        }
        int sent = 0;
        for (FilterCompileError error : report.getErrors()) {
            final String message = FilterCompileStatus.formatError(error);
            platform.sendConsoleMessage(message);
            if (sent < 5) {
                actor.sendMessage(message);
                sent++;
            }
        }
    }
}
