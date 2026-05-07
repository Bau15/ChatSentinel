package dev._2lstudios.chatsentinel.shared.socialspy;

import dev._2lstudios.chatsentinel.shared.chat.ChatNotificationManager;
import dev._2lstudios.chatsentinel.shared.chat.ChatPlayerManager;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompileStatus;
import dev._2lstudios.chatsentinel.shared.modules.GeneralModule;
import dev._2lstudios.chatsentinel.shared.modules.ModuleManager;
import dev._2lstudios.chatsentinel.shared.platform.ChatPlatform;
import dev._2lstudios.chatsentinel.shared.platform.ChatUser;
import dev._2lstudios.chatsentinel.shared.text.WarningDeliverySettings;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocialSpyServiceTest {
    @Test
    public void defaultEnabledMessagesSendToPermittedWatcher() {
        final Fixture fixture = fixture(true, false);

        fixture.service.publishCommand(fixture.sender, "/msg Admin hello");

        assertEquals(1, fixture.watcher.messages.size());
    }

    @Test
    public void defaultDisabledCommandsDoNotSend() {
        final Fixture fixture = fixture(true, false);

        fixture.service.publishCommand(fixture.sender, "/home");

        assertTrue(fixture.watcher.messages.isEmpty());
    }

    @Test
    public void explicitToggleEnablesCommands() {
        final Fixture fixture = fixture(true, false);
        fixture.service.toggle(fixture.watcher, SocialSpyModuleId.COMMANDS);

        fixture.service.publishCommand(fixture.sender, "/home");

        assertEquals(1, fixture.watcher.messages.size());
    }

    @Test
    public void includeSelfFalseSkipsSender() {
        final Fixture fixture = fixture(true, true);

        fixture.service.publishCommand(fixture.sender, "/msg Admin hello");

        assertTrue(fixture.sender.messages.isEmpty());
    }

    @Test
    public void missingPermissionSkipsWatcher() {
        final Fixture fixture = fixture(false, false);

        fixture.service.publishCommand(fixture.sender, "/msg Admin hello");

        assertTrue(fixture.watcher.messages.isEmpty());
    }

    private static Fixture fixture(final boolean watcherPermitted, final boolean senderPermitted) {
        final TestModuleManager modules = new TestModuleManager();
        final ChatPlayerManager playerManager = new ChatPlayerManager();
        final FakeUser sender = new FakeUser(UUID.randomUUID(), "Steve", senderPermitted);
        final FakeUser watcher = new FakeUser(UUID.randomUUID(), "Admin", watcherPermitted);
        final FakePlatform platform = new FakePlatform(sender, watcher);
        return new Fixture(new SocialSpyService(modules, playerManager, platform), sender, watcher);
    }

    private static final class Fixture {
        private final SocialSpyService service;
        private final FakeUser sender;
        private final FakeUser watcher;

        private Fixture(final SocialSpyService service, final FakeUser sender, final FakeUser watcher) {
            this.service = service;
            this.sender = sender;
            this.watcher = watcher;
        }
    }

    private static final class TestModuleManager extends ModuleManager {
        @Override
        public void reloadData(final FilterCompileStatus status) {
        }
    }

    private static final class FakePlatform implements ChatPlatform {
        private final List<ChatUser> users = new ArrayList<ChatUser>();

        private FakePlatform(final ChatUser... users) {
            Collections.addAll(this.users, users);
        }

        @Override
        public Collection<ChatUser> getOnlineUsers() { return users; }

        @Override
        public Optional<ChatUser> findUser(final UUID uniqueId) { return Optional.empty(); }

        @Override
        public void sendConsoleMessage(final String legacyMessage) { }

        @Override
        public void dispatchConsoleCommand(final String command) { }

        @Override
        public void runAsync(final Runnable runnable) { runnable.run(); }

        @Override
        public String getPlatformName() { return "Test"; }

        @Override
        public void refreshOnlinePlayers(final ChatPlayerManager chatPlayerManager,
                final ChatNotificationManager chatNotificationManager, final GeneralModule generalModule) { }
    }

    private static final class FakeUser implements ChatUser {
        private final UUID uuid;
        private final String name;
        private final boolean permitted;
        private final List<String> messages = new ArrayList<String>();

        private FakeUser(final UUID uuid, final String name, final boolean permitted) {
            this.uuid = uuid;
            this.name = name;
            this.permitted = permitted;
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
        public boolean hasPermission(final String permission) { return permitted; }

        @Override
        public void sendMessage(final String legacyMessage) { messages.add(legacyMessage); }

        @Override
        public void sendWarning(final String legacyMessage, final WarningDeliverySettings settings) { messages.add(legacyMessage); }
    }
}
