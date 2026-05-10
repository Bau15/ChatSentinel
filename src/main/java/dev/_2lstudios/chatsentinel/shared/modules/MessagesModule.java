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
		final Map<String, String> selected = locales.get(lang);
		if (selected != null) {
			final String value = selected.get(path);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		final Map<String, String> configuredDefault = locales.get(defaultLang);
		if (configuredDefault != null) {
			final String value = configuredDefault.get(path);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		final Map<String, String> english = locales.get("en");
		if (english != null) {
			final String value = english.get(path);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		return "<CHATSENTINEL STRING NOT FOUND: " + path + ">";
	}

    private boolean hasString(String lang, String path) {
        final Map<String, String> selected = locales.get(lang);
        if (selected != null) {
            final String value = selected.get(path);
            if (value != null && !value.isEmpty()) {
                return true;
            }
        }

        final Map<String, String> configuredDefault = locales.get(defaultLang);
        if (configuredDefault != null) {
            final String value = configuredDefault.get(path);
            if (value != null && !value.isEmpty()) {
                return true;
            }
        }

        final Map<String, String> english = locales.get("en");
        if (english != null) {
            final String value = english.get(path);
            if (value != null && !value.isEmpty()) {
                return true;
            }
        }

        return false;
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

    public String getBlockedMessage(final String[][] placeholders, final String lang) {
        return PlaceholderUtil.replacePlaceholders(getString(lang, "blocked_message"), placeholders);
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

	public String getDeleteChatUsage(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "deletechat_usage"));
	}

	public String getRecentChatsUsage(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "recentchats_usage"));
	}

	public String getCorrectionWarnMessage(String[][] placeholders, String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "correction_warn_message"), placeholders);
	}

	public String getCorrectionEnabled(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "correction_enabled"));
	}

	public String getCorrectionDisabled(String lang) {
		return getCorrectionDisabled(new String[][] { { "%corrections%", "%original_message%", "%corrected_message%" }, { "", "", "" } }, lang);
	}

	public String getCorrectionDisabled(String[][] placeholders, String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "correction_disabled"), placeholders);
	}

	public String getCorrectionUsage(String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "correction_usage"));
	}

	public String getCorrectionConsoleOnly(String lang) {
		return getCorrectionConsoleOnly(new String[][] { { "%reason%", "%player%" }, { "", "" } }, lang);
	}

	public String getCorrectionConsoleOnly(String[][] placeholders, String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "correction_console_only"), placeholders);
	}

	public String getCooldownWarnMessage(final String[][] placeholders, final String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "cooldown_warn_message"), placeholders);
	}

	public String getSimilarityWarnMessage(final String[][] placeholders, final String lang) {
		return PlaceholderUtil.replacePlaceholders(getString(lang, "similarity_warn_message"), placeholders);
	}
}