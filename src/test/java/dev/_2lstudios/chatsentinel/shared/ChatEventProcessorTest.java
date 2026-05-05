package dev._2lstudios.chatsentinel.shared;

import dev._2lstudios.chatsentinel.shared.alerts.LocalAlertBus;
import dev._2lstudios.chatsentinel.shared.chat.ChatEventProcessor;
import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayer;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.chat.ProcessedChatEvent;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileStatus;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatEventProcessorTest {
    @Test
    public void process_blocksServerMute_whenMuted() {
        TestModuleManager modules = modules();
        modules.getServerMuteModule().loadData(true, true, "mute.bypass");
        FakeUser user = new FakeUser(UUID.randomUUID(), "Steve");
        ChatEventProcessor processor = processor(modules, new FakePlatform("BungeeCord", Collections.singletonList(user)), new ChatPlayerManager());

        ProcessedChatEvent result = processor.process(user, "hello", true);

        assertTrue(result.isCancelled());
    }

    @Test
    public void process_skipsNoMove_whenPlatformNotBukkit() {
        TestModuleManager modules = modules();
        modules.getNoMoveChatModule().loadData(true, "move.bypass");
        FakeUser user = new FakeUser(UUID.randomUUID(), "Steve");
        ChatEventProcessor processor = processor(modules, new FakePlatform("Velocity", Collections.singletonList(user)), new ChatPlayerManager());

        ProcessedChatEvent result = processor.process(user, "hello", true);

        assertFalse(result.isCancelled());
    }

    @Test
    public void process_blocksNoMove_whenBukkitAndNotMoved() {
        TestModuleManager modules = modules();
        modules.getNoMoveChatModule().loadData(true, "move.bypass");
        FakeUser user = new FakeUser(UUID.randomUUID(), "Steve");
        ChatEventProcessor processor = processor(modules, new FakePlatform("Bukkit", Collections.singletonList(user)), new ChatPlayerManager());

        ProcessedChatEvent result = processor.process(user, "hello", true);

        assertTrue(result.isCancelled());
    }

    @Test
    public void process_allowsNoMove_whenMovementGatePassed() {
        TestModuleManager modules = modules();
        modules.getNoMoveChatModule().loadData(true, "move.bypass", 5.0D, true);
        FakeUser user = new FakeUser(UUID.randomUUID(), "Steve");
        ChatPlayerManager players = new ChatPlayerManager();
        players.getPlayer(user).markMovementGatePassed();
        ChatEventProcessor processor = processor(modules, new FakePlatform("Bukkit", Collections.singletonList(user)), players);

        ProcessedChatEvent result = processor.process(user, "hello", true);

        assertFalse(result.isCancelled());
    }

    private static ChatEventProcessor processor(TestModuleManager modules, ChatPlatform platform, ChatPlayerManager players) {
        return new ChatEventProcessor(modules, players, new ChatNotificationManager(), platform, new LocalAlertBus());
    }

    private static TestModuleManager modules() {
        TestModuleManager manager = new TestModuleManager();
        Map<String, Map<String, String>> locales = new HashMap<String, Map<String, String>>();
        Map<String, String> en = new HashMap<String, String>();
        en.put("server_muted", "muted");
        en.put("no_move_chat_warn_message", "move first");
        en.put("filtered", "filtered");
        locales.put("en", en);
        manager.getMessagesModule().loadData("en", locales);
        manager.getGeneralModule().loadData(false, false, false, Collections.<String>emptyList());
        manager.getSyntaxModule().loadData(false, "Syntax", 0, "", false, new String[0], new String[0]);
        manager.getAllowedCharactersModule().loadData(false,
                dev._2lstudios.chatsentinel.shared.modules.AllowedCharactersModule.DEFAULT_MODE,
                dev._2lstudios.chatsentinel.shared.modules.AllowedCharactersModule.DEFAULT_ALLOWED_REGEX,
                dev._2lstudios.chatsentinel.shared.modules.AllowedCharactersModule.DEFAULT_REPLACEMENT);
        manager.getCapitalizationModule().loadData(false, "Capitalization", true, 0, 0, "", false, new String[0], false,
                new String[0], new java.util.function.Supplier<Collection<String>>() {
                    @Override
                    public Collection<String> get() {
                        return Collections.emptyList();
                    }
                });
        manager.getCooldownModule().loadData(false, 0, 0, 0, 0);
        manager.getFloodModule().loadData(false, "Flood", false, 0, "", "", false, new String[0]);
        manager.getBlacklistModule().loadData(false, "Blacklist", false, false, "", 0, "", false, new String[0], new String[0], true);
        manager.getNoMoveChatModule().loadData(false, "move.bypass", 5.0D, true);
        return manager;
    }

    private static final class TestModuleManager extends ModuleManager {
        @Override
        public void reloadData(FilterCompileStatus status) {
        }
    }

    private static final class FakePlatform implements ChatPlatform {
        private final String name;
        private final List<ChatUser> users;

        private FakePlatform(String name, Collection<ChatUser> users) {
            this.name = name;
            this.users = new ArrayList<ChatUser>(users);
        }

        @Override
        public Collection<ChatUser> getOnlineUsers() {
            return users;
        }

        @Override
        public Optional<ChatUser> findUser(UUID uniqueId) {
            for (ChatUser user : users) {
                if (user.getUniqueId().equals(uniqueId)) {
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        }

        @Override
        public void sendConsoleMessage(String legacyMessage) {
        }

        @Override
        public void dispatchConsoleCommand(String command) {
        }

        @Override
        public void runAsync(Runnable runnable) {
            runnable.run();
        }

        @Override
        public String getPlatformName() {
            return name;
        }
    }

    private static final class FakeUser implements ChatUser {
        private final UUID uuid;
        private final String name;

        private FakeUser(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() { return uuid; }

        @Override
        public String getName() { return name; }

        @Override
        public String getLocale() { return "en"; }

        @Override
        public String getServerName() { return "server"; }

        @Override
        public boolean hasPermission(String permission) { return false; }

        @Override
        public void sendMessage(String legacyMessage) { }

        @Override
        public void sendWarning(String legacyMessage, WarningDeliverySettings settings) { }
    }
}
