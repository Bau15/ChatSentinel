package dev._2lstudios.chatsentinel.shared.socialspy;

import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.commands.CommandPermission;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import dev._2lstudios.chatsentinel.shared.modules.SocialSpyModule;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;

import java.util.ArrayList;
import java.util.List;

public final class SocialSpyService {
    private final ModuleManager moduleManager;
    private final ChatPlayerManager chatPlayerManager;
    private final ChatPlatform platform;

    public SocialSpyService(final ModuleManager moduleManager, final ChatPlayerManager chatPlayerManager,
            final ChatPlatform platform) {
        this.moduleManager = moduleManager;
        this.chatPlayerManager = chatPlayerManager;
        this.platform = platform;
    }

    public void publish(final String moduleId, final ChatUser sender, final String[][] placeholders) {
        final SocialSpyModule module = moduleManager.getSocialSpyModule();
        final String id = SocialSpyModuleId.normalize(moduleId);
        if (!module.isEnabled() || !SocialSpyModuleId.isValid(id) || !module.isModuleEnabled(id)) {
            return;
        }
        final String message = module.format(id, mergeCommonPlaceholders(id, sender, placeholders));
        for (ChatUser watcher : platform.getOnlineUsers()) {
            if (watcher == null || sender == null) {
                continue;
            }
            if (!module.isIncludeSelf() && watcher.getUniqueId().equals(sender.getUniqueId())) {
                continue;
            }
            if (!hasModulePermission(watcher, id)) {
                continue;
            }
            if (!isActiveFor(watcher, id)) {
                continue;
            }
            watcher.sendMessage(message);
        }
    }

    public void publishCommand(final ChatUser sender, final String commandText) {
        final SocialSpyModule module = moduleManager.getSocialSpyModule();
        final SocialSpyCommandSpyResult spy = module.getCommandParser().parse(commandText);
        if (!spy.isSpy()) {
            return;
        }
        if (SocialSpyModuleId.MESSAGES.equals(spy.getModuleId())) {
            publish(SocialSpyModuleId.MESSAGES, sender, new String[][] {
                    { "%target%", "%message%", "%command%" },
                    { spy.getTarget(), module.getTrimSettings().trim(spy.getContent(), module.getTrimSettings().getMessageContentChars()), spy.getFullCommandWithoutSlash() }
            });
            return;
        }
        publish(SocialSpyModuleId.COMMANDS, sender, new String[][] {
                { "%command%" },
                { module.getTrimSettings().trim(spy.getFullCommandWithoutSlash(), module.getTrimSettings().getCommandContentChars()) }
        });
    }

    public boolean isActiveFor(final ChatUser watcher, final String moduleId) {
        final String id = SocialSpyModuleId.normalize(moduleId);
        if (watcher == null || !SocialSpyModuleId.isValid(id)) {
            return false;
        }
        final ChatPlayer chatPlayer = chatPlayerManager.getPlayer(watcher);
        if (chatPlayer.hasSocialSpyOverride(id)) {
            return chatPlayer.getSocialSpyOverride(id);
        }
        return moduleManager.getSocialSpyModule().isDefaultEnabled(id);
    }

    public boolean toggle(final ChatUser watcher, final String moduleId) {
        final boolean enabled = !isActiveFor(watcher, moduleId);
        setEnabled(watcher, moduleId, enabled);
        return enabled;
    }

    public boolean setEnabled(final ChatUser watcher, final String moduleId, final boolean enabled) {
        final String id = SocialSpyModuleId.normalize(moduleId);
        if (watcher == null || !SocialSpyModuleId.isValid(id)) {
            return false;
        }
        chatPlayerManager.getPlayer(watcher).setSocialSpyOverride(id, enabled);
        return enabled;
    }

    public boolean toggleAllPermitted(final ChatUser watcher) {
        final List<String> permitted = permittedModules(watcher);
        boolean allActive = !permitted.isEmpty();
        for (String moduleId : permitted) {
            if (!isActiveFor(watcher, moduleId)) {
                allActive = false;
                break;
            }
        }
        final boolean newState = !allActive;
        for (String moduleId : permitted) {
            setEnabled(watcher, moduleId, newState);
        }
        return newState;
    }

    public String status(final ChatUser watcher) {
        final SocialSpyModule module = moduleManager.getSocialSpyModule();
        final StringBuilder builder = new StringBuilder(module.getStatusHeader());
        for (String moduleId : module.getModuleIds()) {
            if (hasModulePermission(watcher, moduleId)) {
                builder.append('\n').append(module.getStatusLine(moduleId, isActiveFor(watcher, moduleId)));
            }
        }
        return builder.toString();
    }

    public boolean hasModulePermission(final ChatUser watcher, final String moduleId) {
        if (watcher == null) {
            return false;
        }
        final SocialSpyModule module = moduleManager.getSocialSpyModule();
        final String permission = module.getPermission(moduleId);
        return watcher.hasPermission(permission) || watcher.hasPermission(module.getRootPermission())
                || watcher.hasPermission(CommandPermission.ADMIN);
    }

    public boolean hasAnyPermission(final ChatUser watcher) {
        return !permittedModules(watcher).isEmpty();
    }

    public void resetAll(final ChatUser watcher) {
        if (watcher == null) {
            return;
        }
        chatPlayerManager.getPlayer(watcher).clearSocialSpyOverrides();
    }

    private List<String> permittedModules(final ChatUser watcher) {
        final List<String> result = new ArrayList<String>();
        for (String moduleId : moduleManager.getSocialSpyModule().getModuleIds()) {
            if (hasModulePermission(watcher, moduleId)) {
                result.add(moduleId);
            }
        }
        return result;
    }

    private String[][] mergeCommonPlaceholders(final String moduleId, final ChatUser sender, final String[][] placeholders) {
        final String[][] common = new String[][] {
                { "%player%", "%uuid%", "%server%", "%module%" },
                { sender == null ? "" : sender.getName(), sender == null ? "" : sender.getUniqueId().toString(),
                        sender == null ? "" : sender.getServerName(), moduleId }
        };
        if (placeholders == null || placeholders.length < 2) {
            return common;
        }
        final String[] keys = new String[common[0].length + placeholders[0].length];
        final String[] values = new String[common[1].length + placeholders[1].length];
        System.arraycopy(common[0], 0, keys, 0, common[0].length);
        System.arraycopy(common[1], 0, values, 0, common[1].length);
        System.arraycopy(placeholders[0], 0, keys, common[0].length, placeholders[0].length);
        for (int i = 0; i < placeholders[1].length; i++) {
            values[common[1].length + i] = placeholders[1][i] == null ? "" : placeholders[1][i];
        }
        return new String[][] { keys, values };
    }
}
