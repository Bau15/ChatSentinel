package dev._2lstudios.chatsentinel.shared.modules;

import java.util.HashMap;
import java.util.Map;

import dev._2lstudios.chatsentinel.shared.utils.PlaceholderUtil;

public class MessagesModule {
	private Map<String, Map<String, String>> locales;
	private String defaultLang = "en";

	public void loadData(String defaultLang, Map<String, Map<String, String>> messages) {
		this.locales = messages;
		this.defaultLang = defaultLang;
	}

	private String getString(String lang, String path) {
		Map<String, String> messages = locales.getOrDefault(lang, locales.getOrDefault(defaultLang, locales.getOrDefault("en", new HashMap<>())));

		return messages.getOrDefault(path, "<CHATSENTINEL STRING NOT FOUND>");
	}

    private boolean hasString(String lang, String path) {
        Map<String, String> messages = locales.getOrDefault(lang, locales.getOrDefault(defaultLang, locales.getOrDefault("en", new HashMap<>())));
        String value = messages.get(path);
        return value != null && !value.isEmpty();
    }

	public String getCleared(String[][] placeholders, String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "cleared"), placeholders);
	}

	public String getReload(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "reload"));
	}

	public String getHelp(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "help"));
	}

	public String getUnknownCommand(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "unknown_command"));
	}

	public String getNoPermission(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "no_permission"));
	}

	public String getWarnMessage(String[][] placeholders, String lang, String module) {
		String moduleLowerCase = module.toLowerCase();
		if ("capitalization".equals(moduleLowerCase) && !hasString(lang, "capitalization_warn_message")) {
			moduleLowerCase = "caps";
		}

		return PlaceholderUtil.replacePlaceholders(getString(lang, moduleLowerCase + "_warn_message"), placeholders);
	}

	public String getFiltered(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "filtered"));
	}

    public String getNotifyEnabled(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "notify-enabled"));
    }

    public String getNotifyDisabled(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "notify-disabled"));
    }

	public String getServerMuted(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "server_muted"));
	}

	public String getNoMoveChatWarnMessage(String lang) {
		return getNoMoveChatWarnMessage(new String[][] { { "%distance%" }, { "" } }, lang);
	}

    public String getNoMoveChatWarnMessage(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "no_move_chat_warn_message"), placeholders);
    }

	public String getServerMuteEnabled(String lang) {
		return getServerMuteEnabled(new String[][] { { "%reason%", "%player%" }, { "", "" } }, lang);
	}

    public String getServerMuteEnabled(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "server_mute_enabled"), placeholders);
    }

	public String getServerMuteDisabled(String lang) {
		return getServerMuteDisabled(new String[][] { { "%reason%", "%player%" }, { "", "" } }, lang);
	}

    public String getServerMuteDisabled(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "server_mute_disabled"), placeholders);
    }

    public String getClearBypassNotice(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "clear_bypass_notice"), placeholders);
    }

    public String getClearSenderSummary(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "clear_sender_summary"), placeholders);
    }

    public String getDeleteUsage(String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_usage"));
    }

    public String getDeleteUnknown(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_unknown"), placeholders);
    }

    public String getDeleteDone(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_done"), placeholders);
    }

    public String getDeleteBypassNotice(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_bypass_notice"), placeholders);
    }

    public String getDeleteListHeader(String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_list_header"));
    }

    public String getDeleteListEntry(String[][] placeholders, String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_list_entry"), placeholders);
    }

    public String getDeleteRefresh(String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "delete_refresh"));
    }
}
