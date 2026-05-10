package dev._2lstudios.chatsentinel.shared.modules;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.UUID;

import org.junit.Test;

public final class ChatSnapshotModuleTest {
    @Test
    public void buildReplayPayloadDoesNotPrefixFirstVisibleLineWithSpace() {
        final ChatSnapshotModule module = new ChatSnapshotModule();
        module.loadData(true, 50, 2, ChatSnapshotModule.DEFAULT_PROXY_REPLAY_FORMAT);
        module.record(UUID.randomUUID(), "LinsaFTW", "hello", "<LinsaFTW> hello", Collections.<UUID>emptyList());

        final String payload = module.buildReplayPayload(UUID.randomUUID());

        assertTrue(payload.contains("\n<LinsaFTW> hello\n"));
    }
}
