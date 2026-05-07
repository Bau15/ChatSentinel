package dev._2lstudios.chatsentinel.shared;

import dev._2lstudios.chatsentinel.shared.text.LegacyText;
import org.junit.Assert;
import org.junit.Test;

public final class LegacyTextTest {
    @Test
    public void toSection_convertsAmpersandColorCode() {
        Assert.assertEquals("\u00A7cBlocked", LegacyText.toSection("&cBlocked"));
    }

    @Test
    public void toSection_preservesPlainText() {
        Assert.assertEquals("Blocked", LegacyText.toSection("Blocked"));
    }

    @Test
    public void toSection_doesNotConvertInvalidAmpersand() {
        Assert.assertEquals("A & Z", LegacyText.toSection("A & Z"));
    }

    @Test
    public void toSectionLines_splitsNonEmptyLines() {
        Assert.assertEquals(2, LegacyText.toSectionLines("&aOne\n&cTwo").size());
    }
}
