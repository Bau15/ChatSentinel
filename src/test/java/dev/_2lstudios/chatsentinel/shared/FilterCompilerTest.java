package dev._2lstudios.chatsentinel.shared;

import dev._2lstudios.chatsentinel.shared.filter.CompiledFilterRegistry;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompiler;
import dev._2lstudios.chatsentinel.shared.filter.FilterExpressionFile;
import dev._2lstudios.chatsentinel.shared.filter.FilterKind;
import dev._2lstudios.chatsentinel.shared.filter.FilterSource;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class FilterCompilerTest {
    private static final String SPANISH_PUT_PATTERN = "(?:^|[^\\p{L}\\p{N}_])p[\\W_]{0,2}(?:[uv][\\W_]{0,2})?[t7][\\W_]{0,2}(?:[o0a4@]|[i1!][\\W_]{0,2}[t7][\\W_]{0,2}[o0a4@])(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_FUCK_PATTERN = "(?:^|[^\\p{L}\\p{N}_])f[\\W_]{0,2}(?:[uv][\\W_]{0,2})?c[\\W_]{0,2}k(?:[\\W_]{0,2}[e3][\\W_]{0,2}r)?(?:$|[^\\p{L}\\p{N}_])";

    @Test
    public void spanishOffense_detectsPta() {
        assertTrue(spanishRegistry().findFirst("Pta").isPresent());
    }

    @Test
    public void spanishOffense_detectsPut4() {
        assertTrue(spanishRegistry().findFirst("Put4").isPresent());
    }

    @Test
    public void spanishOffense_detectsPvt4() {
        assertTrue(spanishRegistry().findFirst("Pvt4").isPresent());
    }

    @Test
    public void spanishOffense_detectsSpacedPvta() {
        assertTrue(spanishRegistry().findFirst("p v t a").isPresent());
    }

    @Test
    public void englishOffense_detectsFcker() {
        assertTrue(englishRegistry().findFirst("fcker").isPresent());
    }

    @Test
    public void englishOffense_detectsFuck3r() {
        assertTrue(englishRegistry().findFirst("fuck3r").isPresent());
    }

    @Test
    public void englishOffense_detectsSpacedFucker() {
        assertTrue(englishRegistry().findFirst("f u c k e r").isPresent());
    }

    private static CompiledFilterRegistry spanishRegistry() {
        return compile("offense", "blacklist/offense/spanish.yml", SPANISH_PUT_PATTERN);
    }

    private static CompiledFilterRegistry englishRegistry() {
        return compile("offense", "blacklist/offense/english.yml", ENGLISH_FUCK_PATTERN);
    }

    private static CompiledFilterRegistry compile(final String moduleId, final String path, final String expression) {
        final FilterSource source = new FilterSource(FilterKind.BLACKLIST, moduleId, path, path);
        final FilterExpressionFile file = new FilterExpressionFile(source, Collections.singletonList(expression));
        return new FilterCompiler().compile(FilterKind.BLACKLIST, Collections.singletonList(file)).getRegistry();
    }
}
