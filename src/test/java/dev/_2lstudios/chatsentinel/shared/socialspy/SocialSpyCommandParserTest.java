package dev._2lstudios.chatsentinel.shared.socialspy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SocialSpyCommandParserTest {
    private final SocialSpyCommandParser parser = new dev._2lstudios.chatsentinel.shared.modules.SocialSpyModule().getCommandParser();

    @Test
    public void msgCommandIsPrivateMessage() {
        assertEquals(SocialSpyModuleId.MESSAGES, parser.parse("/msg Steve hello there").getModuleId());
    }

    @Test
    public void msgCommandExtractsTarget() {
        assertEquals("Steve", parser.parse("/msg Steve hello there").getTarget());
    }

    @Test
    public void msgCommandExtractsContent() {
        assertEquals("hello there", parser.parse("/msg Steve hello there").getContent());
    }

    @Test
    public void replyCommandUsesReplyTarget() {
        assertEquals("<reply>", parser.parse("/r yes").getTarget());
    }

    @Test
    public void replyCommandExtractsContent() {
        assertEquals("yes", parser.parse("/r yes").getContent());
    }

    @Test
    public void namespacedMessageCommandUsesRootAlias() {
        assertEquals(SocialSpyModuleId.MESSAGES, parser.parse("/essentials:msg Steve hi").getModuleId());
    }

    @Test
    public void loginCommandIsIgnored() {
        assertFalse(parser.parse("/login secret").isSpy());
    }

    @Test
    public void homeCommandIsGeneralCommand() {
        assertEquals(SocialSpyModuleId.COMMANDS, parser.parse("/home").getModuleId());
    }

    @Test
    public void emptyPrivateMessageIsIgnored() {
        assertFalse(parser.parse("/msg Steve").isSpy());
    }

    @Test
    public void dmCommandIsPrivateMessage() {
        assertEquals(SocialSpyModuleId.MESSAGES, parser.parse("/dm Steve secret").getModuleId());
    }

    @Test
    public void directmessageCommandIsPrivateMessage() {
        assertEquals(SocialSpyModuleId.MESSAGES, parser.parse("/directmessage Steve secret").getModuleId());
    }

    @Test
    public void respondCommandIsReplyPrivateMessage() {
        assertEquals("<reply>", parser.parse("/respond secret").getTarget());
    }

    @Test
    public void namespacedMsgCommandNormalizesNamespaceAndParses() {
        assertEquals(SocialSpyModuleId.MESSAGES, parser.parse("/minecraft:msg Steve secret").getModuleId());
    }

    @Test
    public void loginCommandReturnsNone() {
        assertFalse(parser.parse("/login password").isSpy());
    }
}
