package dev._2lstudios.chatsentinel.shared.modules;

import dev._2lstudios.chatsentinel.shared.utils.PlaceholderUtil;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordWebhookModule {

    private static final Logger LOGGER = Logger.getLogger(DiscordWebhookModule.class.getName());

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS    = 5_000;
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private boolean enabled;
    private String  webhookUrl;
    private String  senderUsername;
    private String  senderAvatarUrl;
    private String  authorName;
    private String  authorUrl;
    private String  authorIconUrl;
    private String  title;
    private String  color;
    private String  description;
    private String  messageFieldName;
    private String  serverFieldName;
    private String  footerText;
    private String  footerIconUrl;
    private String  thumbnailUrl;

    public void loadData(
            boolean enabled,         String webhookUrl,
            String senderUsername,   String senderAvatarUrl,
            String authorName,       String authorUrl,       String authorIconUrl,
            String title,            String color,           String description,
            String messageFieldName, String serverFieldName,
            String footerText,       String footerIconUrl,   String thumbnailUrl) {

        this.enabled          = enabled;
        this.webhookUrl       = webhookUrl;
        this.senderUsername   = senderUsername;
        this.senderAvatarUrl  = senderAvatarUrl;
        this.authorName       = authorName;
        this.authorUrl        = authorUrl;
        this.authorIconUrl    = authorIconUrl;
        this.title            = title;
        this.color            = color;
        this.description      = description;
        this.messageFieldName = messageFieldName;
        this.serverFieldName  = serverFieldName;
        this.footerText       = footerText;
        this.footerIconUrl    = footerIconUrl;
        this.thumbnailUrl     = thumbnailUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void dispatchWebhookNotification(ModerationModule moderationModule, String[][] placeholders) {
        if (!enabled || !moderationModule.isWebhookEnabled()) return;

        try {
            String message = PlaceholderUtil.replacePlaceholders("%message%",     placeholders);
            String server  = PlaceholderUtil.replacePlaceholders("%server_name%", placeholders);
            String desc    = PlaceholderUtil.replacePlaceholders(description,     placeholders);

            sendWebhook(buildPayload(message, server, desc));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to dispatch Discord webhook notification", e);
        }
    }

    private String buildPayload(String message, String server, String desc) {
        int    colorInt = parseColor(color);
        String ts       = OffsetDateTime.now().format(TIMESTAMP_FMT);

        return "{"
                + str("username",   senderUsername)  + ","
                + str("avatar_url", senderAvatarUrl) + ","
                + "\"embeds\":[{"
                + str("title",       title) + ","
                + str("description", desc)  + ","
                + "\"color\":"  + colorInt  + ","
                + str("timestamp", ts)      + ","
                + "\"author\":{"
                + str("name",     authorName)    + ","
                + str("url",      authorUrl)     + ","
                + str("icon_url", authorIconUrl)
                + "},"
                + "\"thumbnail\":{" + str("url", thumbnailUrl) + "},"
                + "\"footer\":{"
                + str("text",     footerText)   + ","
                + str("icon_url", footerIconUrl)
                + "},"
                + "\"fields\":["
                + field(messageFieldName, message, true) + ","
                + field(serverFieldName,  server,  true)
                + "]"
                + "}]}";
    }

    private static String str(String key, String value) {
        return "\"" + key + "\":\"" + escape(value) + "\"";
    }

    private static String field(String name, String value, boolean inline) {
        return "{" + str("name", name) + "," + str("value", value) + ",\"inline\":" + inline + "}";
    }

    private void sendWebhook(String payload) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(webhookUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();

        if (status >= 200 && status < 300) {
            drainAndClose(conn.getInputStream());
        } else {
            LOGGER.warning("Discord webhook HTTP " + status + " | " + readStream(conn.getErrorStream()));
            LOGGER.warning("Payload: " + payload);
        }

        conn.disconnect();
    }

    private static int parseColor(String hex) {
        if (hex == null || hex.isEmpty()) return 0;
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex).getRGB() & 0xFFFFFF;
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid color '" + hex + "', embed will have no color.");
            return 0;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n");  break;
                case '\r':                    break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else          sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void drainAndClose(InputStream stream) {
        if (stream == null) return;
        try (InputStream s = stream) { while (s.read() != -1) { } }
        catch (IOException ignored) { }
    }

    private static String readStream(InputStream stream) {
        if (stream == null) return "<empty>";
        try (InputStream s = stream) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int n;
            while ((n = s.read(chunk)) != -1) buf.write(chunk, 0, n);
            return buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return "<unreadable: " + e.getMessage() + ">";
        }
    }
}
