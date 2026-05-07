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
}
