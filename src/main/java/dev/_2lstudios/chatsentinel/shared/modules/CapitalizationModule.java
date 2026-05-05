package dev._2lstudios.chatsentinel.shared.modules;

import dev._2lstudios.chatsentinel.shared.chat.ChatEventResult;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class CapitalizationModule extends ModerationModule {
    private boolean correct;
    private int maxUppercase;
    private boolean whitelistPlayerNames;
    private String bypassPermission = "chatsentinel.bypass.capitalization";
    private String[] whitelist = new String[0];
    private Supplier<Collection<String>> onlinePlayerNamesSupplier = Collections::<String>emptyList;

    public void loadData(boolean enabled, String customName, boolean correct, int max, int maxWarns,
            String warnNotification, boolean webhookEnabled, String[] commands, boolean whitelistPlayerNames,
            String[] whitelist, Supplier<Collection<String>> onlinePlayerNamesSupplier) {
        loadData(enabled, customName, correct, max, maxWarns, warnNotification, webhookEnabled, commands,
                whitelistPlayerNames, whitelist, onlinePlayerNamesSupplier, "chatsentinel.bypass.capitalization");
    }

    public void loadData(boolean enabled, String customName, boolean correct, int max, int maxWarns,
            String warnNotification, boolean webhookEnabled, String[] commands, boolean whitelistPlayerNames,
            String[] whitelist, Supplier<Collection<String>> onlinePlayerNamesSupplier, String bypassPermission) {
        setEnabled(enabled);
        setMaxWarns(maxWarns);
        setWarnNotification(warnNotification == null ? "" : warnNotification);
        setWebhookEnabled(webhookEnabled);
        setCommands(commands == null ? new String[0] : commands);
        setCustomName(customName);
        this.correct = correct;
        this.maxUppercase = max;
        this.whitelistPlayerNames = whitelistPlayerNames;
        this.whitelist = whitelist == null ? new String[0] : whitelist;
        this.onlinePlayerNamesSupplier = onlinePlayerNamesSupplier == null
                ? Collections::<String>emptyList
                : onlinePlayerNamesSupplier;
        this.bypassPermission = bypassPermission == null || bypassPermission.trim().isEmpty()
                ? "chatsentinel.bypass.capitalization"
                : bypassPermission;
    }

    public boolean isCorrect() {
        return correct;
    }

    public long uppercaseCount(String string) {
        return string == null ? 0L : string.codePoints().filter(c -> c >= 'A' && c <= 'Z').count();
    }

    @Override
    public ChatEventResult processEvent(ChatPlayer chatPlayer, MessagesModule messagesModule, String playerName,
            String originalMessage, String lang) {
        if (!isEnabled() || uppercaseCount(removeWhitelistEntries(originalMessage)) <= maxUppercase) {
            return null;
        }
        return new ChatEventResult(originalMessage == null ? "" : originalMessage.toLowerCase(), false);
    }

    @Override
    public String getName() {
        return "Capitalization";
    }

    @Override
    public String getCustomName() {
        String customName = super.getCustomName();
        return customName == null || customName.trim().isEmpty() ? "Capitalization" : customName;
    }

    @Override
    public String getBypassPermission() {
        return bypassPermission;
    }

    private String removeWhitelistEntries(String message) {
        String filteredMessage = message == null ? "" : message;
        if (whitelistPlayerNames) {
            filteredMessage = removeTokens(filteredMessage, onlinePlayerNamesSupplier.get());
        }
        return removeTokens(filteredMessage, Arrays.asList(whitelist));
    }

    private String removeTokens(String message, Iterable<String> tokens) {
        String filteredMessage = message;
        if (tokens == null) {
            return filteredMessage;
        }
        for (String token : tokens) {
            if (token != null && !token.isEmpty()) {
                filteredMessage = filteredMessage.replace(token, "");
            }
        }
        return filteredMessage;
    }
}
