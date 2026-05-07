package dev._2lstudios.chatsentinel.shared;

import dev._2lstudios.chatsentinel.shared.filter.CompiledFilterRegistry;
import dev._2lstudios.chatsentinel.shared.filter.FilterCompiler;
import dev._2lstudios.chatsentinel.shared.filter.FilterExpressionFile;
import dev._2lstudios.chatsentinel.shared.filter.FilterKind;
import dev._2lstudios.chatsentinel.shared.filter.FilterSource;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterCompilerTest {
    private static final String SPANISH_PUT_PATTERN = "(?:^|[^\\p{L}\\p{N}_])p[\\W_]{0,2}(?:[uv][\\W_]{0,2})?[t7][\\W_]{0,2}(?:[o0a4@]|[i1!][\\W_]{0,2}[t7][\\W_]{0,2}[o0a4@])(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_FUCK_PATTERN = "(?:^|[^\\p{L}\\p{N}_])f[\\W_]{0,2}(?:[uv][\\W_]{0,2})?c[\\W_]{0,2}k(?:[\\W_]{0,2}[e3][\\W_]{0,2}r)?(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_TRASH_PATTERN = "(?:^|[^\\p{L}\\p{N}_])[t7][\\W_]{0,2}r[\\W_]{0,2}[a4@][\\W_]{0,2}[s5$][\\W_]{0,2}h(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_PEDOPHILE_PATTERN = "(?:^|[^\\p{L}\\p{N}_])p[\\W_]{0,2}[e3][\\W_]{0,2}(?:d[\\W_]{0,2}[o0][\\W_]{0,2})?p[\\W_]{0,2}h[\\W_]{0,2}[i1!][\\W_]{0,2}l[\\W_]{0,2}[e3](?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_GAY_PATTERN = "(?:^|[^\\p{L}\\p{N}_])g[\\W_]{0,2}[a4@][\\W_]{0,2}y(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_ANAL_PATTERN = "(?:^|[^\\p{L}\\p{N}_])[a4@][\\W_]{0,2}n[\\W_]{0,2}(?:[a4@][\\W_]{0,2})?l(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_SEX_PATTERN = "(?:^|[^\\p{L}\\p{N}_])[s5$][\\W_]{0,2}[e3][\\W_]{0,2}x(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_SMELL_PATTERN = "(?:^|[^\\p{L}\\p{N}_])[s5$][\\W_]{0,2}m[\\W_]{0,2}[e3][\\W_]{0,2}l[\\W_]{0,2}l(?:$|[^\\p{L}\\p{N}_])";
    private static final String ENGLISH_IDIOT_PATTERN = "(?:^|[^\\p{L}\\p{N}_])[i1!][\\W_]{0,2}d[\\W_]{0,2}[i1!][\\W_]{0,2}[o0][\\W_]{0,2}[t7](?:[\\W_]{0,2}[a4@])?(?:$|[^\\p{L}\\p{N}_])";

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

    @Test
    public void englishOffense_detectsTrashPhrase() {
        assertTrue(compileEnglish(ENGLISH_TRASH_PATTERN).findFirst("your server is trash").isPresent());
    }

    @Test
    public void englishOffense_detectsLeetTrash() {
        assertTrue(compileEnglish(ENGLISH_TRASH_PATTERN).findFirst("tr4sh").isPresent());
    }

    @Test
    public void englishOffense_detectsPedophile() {
        assertTrue(compileEnglish(ENGLISH_PEDOPHILE_PATTERN).findFirst("pedophile").isPresent());
    }

    @Test
    public void englishOffense_detectsMaskedPephile() {
        assertTrue(compileEnglish(ENGLISH_PEDOPHILE_PATTERN).findFirst("pe phile").isPresent());
    }

    @Test
    public void englishOffense_detectsGay() {
        assertTrue(compileEnglish(ENGLISH_GAY_PATTERN).findFirst("gay").isPresent());
    }

    @Test
    public void englishOffense_detectsAnal() {
        assertTrue(compileEnglish(ENGLISH_ANAL_PATTERN).findFirst("anal").isPresent());
    }

    @Test
    public void englishOffense_detectsAnlTypo() {
        assertTrue(compileEnglish(ENGLISH_ANAL_PATTERN).findFirst("anl").isPresent());
    }

    @Test
    public void englishOffense_detectsSex() {
        assertTrue(compileEnglish(ENGLISH_SEX_PATTERN).findFirst("sex").isPresent());
    }

    @Test
    public void englishOffense_detectsSmellPhrase() {
        assertTrue(compileEnglish(ENGLISH_SMELL_PATTERN).findFirst("You smell bad").isPresent());
    }

    @Test
    public void englishOffense_detectsIdiotExistingRule() {
        assertTrue(compileEnglish(ENGLISH_IDIOT_PATTERN).findFirst("idiot").isPresent());
    }

    @Test
    public void englishOffense_doesNotMatchAnalysisForAnal() {
        assertFalse(compileEnglish(ENGLISH_ANAL_PATTERN).findFirst("analysis").isPresent());
    }

    @Test
    public void englishOffense_doesNotMatchProfileForPephile() {
        assertFalse(compileEnglish(ENGLISH_PEDOPHILE_PATTERN).findFirst("profile").isPresent());
    }

    private static CompiledFilterRegistry spanishRegistry() {
        return compile("offense", "blacklist/offense/spanish.yml", SPANISH_PUT_PATTERN);
    }

    private static CompiledFilterRegistry englishRegistry() {
        return compile("offense", "blacklist/offense/english.yml", ENGLISH_FUCK_PATTERN);
    }

    private static CompiledFilterRegistry compileEnglish(final String expression) {
        return compile("offense", "blacklist/offense/english.yml", expression);
    }

    private static CompiledFilterRegistry compile(final String moduleId, final String path, final String expression) {
        final FilterSource source = new FilterSource(FilterKind.BLACKLIST, moduleId, path, path);
        final FilterExpressionFile file = new FilterExpressionFile(source, Collections.singletonList(expression));
        return new FilterCompiler().compile(FilterKind.BLACKLIST, Collections.singletonList(file)).getRegistry();
    }
}
