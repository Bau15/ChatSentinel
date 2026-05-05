package dev._2lstudios.chatsentinel.shared.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ChatSnapshotModule {
    public static final int DEFAULT_HISTORY_SIZE = 40;
    public static final int DEFAULT_CLEAR_LINES = 128;
    public static final String DEFAULT_PROXY_REPLAY_FORMAT = "&7[%player%] &f%message%";

    private boolean enabled = true;
    private int historySize = DEFAULT_HISTORY_SIZE;
    private int clearLines = DEFAULT_CLEAR_LINES;
    private String proxyReplayFormat = DEFAULT_PROXY_REPLAY_FORMAT;
    private final Deque<Entry> entries = new ArrayDeque<Entry>(DEFAULT_HISTORY_SIZE);

    public synchronized void loadData(boolean enabled, int historySize, int clearLines, String proxyReplayFormat) {
        this.enabled = enabled;
        this.historySize = Math.max(1, Math.min(200, historySize));
        this.clearLines = Math.max(1, Math.min(300, clearLines));
        this.proxyReplayFormat = proxyReplayFormat == null || proxyReplayFormat.trim().isEmpty()
                ? DEFAULT_PROXY_REPLAY_FORMAT
                : proxyReplayFormat;
        trimHistory();
    }

    public synchronized Optional<Entry> record(UUID senderUuid, String senderName, String message, String renderedLine,
            Collection<UUID> recipientIds) {
        if (!enabled) {
            return Optional.empty();
        }
        Entry entry = new Entry(uniqueId(), System.currentTimeMillis(), senderUuid, safe(senderName), safe(message),
                safe(renderedLine), copyRecipients(recipientIds));
        entries.addLast(entry);
        trimHistory();
        return Optional.of(entry);
    }

    public synchronized Optional<Entry> markDeletedEntry(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        for (Entry entry : entries) {
            if (entry.getId().equalsIgnoreCase(id)) {
                entry.markDeleted();
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public synchronized List<Entry> getRecentEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }

    public synchronized List<Entry> getVisibleEntriesFor(UUID viewer) {
        List<Entry> visible = new ArrayList<Entry>();
        for (Entry entry : entries) {
            if (!entry.isDeleted() && entry.isVisibleTo(viewer)) {
                visible.add(entry);
            }
        }
        return Collections.unmodifiableList(visible);
    }

    public String renderProxyLine(String player, String message) {
        return proxyReplayFormat.replace("%player%", safe(player)).replace("%message%", safe(message));
    }

    public String buildClearPayload(String footerMessage) {
        StringBuilder builder = new StringBuilder();
        appendBlankLines(builder);
        builder.append(safe(footerMessage));
        return builder.toString();
    }

    public synchronized String buildReplayPayload(UUID viewer) {
        StringBuilder builder = new StringBuilder();
        appendBlankLines(builder);
        for (Entry entry : entries) {
            if (!entry.isDeleted() && entry.isVisibleTo(viewer)) {
                builder.append(entry.getRenderedLine()).append('\n');
            }
        }
        return builder.toString();
    }

    public int getHistorySize() {
        return historySize;
    }

    public int getClearLines() {
        return clearLines;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void appendBlankLines(StringBuilder builder) {
        for (int i = 0; i < clearLines; i++) {
            builder.append("\n ");
        }
    }

    private String uniqueId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8);
        } while (containsId(id));
        return id;
    }

    private boolean containsId(String id) {
        for (Entry entry : entries) {
            if (entry.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private Set<UUID> copyRecipients(Collection<UUID> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<UUID>(recipientIds));
    }

    private void trimHistory() {
        while (entries.size() > historySize) {
            entries.removeFirst();
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static final class Entry {
        private final String id;
        private final long timestamp;
        private final UUID senderUuid;
        private final String senderName;
        private final String message;
        private final String renderedLine;
        private final Set<UUID> recipientIds;
        private boolean deleted;

        private Entry(String id, long timestamp, UUID senderUuid, String senderName, String message,
                String renderedLine, Set<UUID> recipientIds) {
            this.id = id;
            this.timestamp = timestamp;
            this.senderUuid = senderUuid;
            this.senderName = senderName;
            this.message = message;
            this.renderedLine = renderedLine;
            this.recipientIds = recipientIds;
        }

        public String getId() { return id; }

        public long getTimestamp() { return timestamp; }

        public UUID getSenderUuid() { return senderUuid; }

        public String getSenderName() { return senderName; }

        public String getMessage() { return message; }

        public String getRenderedLine() { return renderedLine; }

        public Set<UUID> getRecipientIds() { return recipientIds; }

        public boolean isDeleted() { return deleted; }

        public boolean isVisibleTo(UUID viewer) {
            return recipientIds.isEmpty() || (viewer != null && recipientIds.contains(viewer));
        }

        public String getDisplayLine() {
            return deleted ? "[deleted]" : renderedLine;
        }

        private void markDeleted() {
            this.deleted = true;
        }
    }
}
