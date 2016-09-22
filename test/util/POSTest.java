package util;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ooba.nlp.fasttag.FastTag;
import com.ooba.nlp.util.POS;
import com.ooba.nlp.util.Tokenizer;
import com.ooba.nlp.util.Util;

public class POSTest {
    Map<String, String[]> lexicon;
    String[]              possibleTags;

    POS[] nouns = { POS.NN, POS.NNP, POS.NNPS, POS.NNS };
    POS[] verbs = { POS.VB, POS.VBD, POS.VBG, POS.VBN, POS.VBP, POS.VBZ };

    String   testText = "The quick brown fox jumps over the lazy dog.";
    POS[]    testTags;
    String[] taggedText;

    @Before
    public void setUp() throws Exception {
        lexicon = FastTag.buildLexicon("lexicon.txt");
        possibleTags = lexicon.values()
                              .stream()
                              .flatMap(Arrays::stream)
                              .distinct()
                              .toArray(String[]::new);
        taggedText = Tokenizer.wordsToStream(testText)
                              .sequential()
                              .map(FastTag.tag(lexicon))
                              .toArray(String[]::new);
        testTags = Arrays.stream(taggedText)
                         .map(POS::fromTaggedWord)
                         .toArray(POS[]::new);
    }

    @Test
    public void testIsNoun() {
        for (final POS pos : nouns)
            Assert.assertTrue(pos.isNoun());
        Arrays.stream(POS.values())
              .filter(p -> !Util.arrayContains(nouns, p))
              .map(POS::isNoun)
              .forEach(Assert::assertFalse);
    }

    @Test
    public void testStaticIsNoun() {
        for (final String taggedWord : taggedText)
            if (taggedWord.startsWith("fox") || taggedWord.startsWith("dog"))
                Assert.assertTrue(POS.isNoun(taggedWord));
            else
                Assert.assertFalse(POS.isNoun(taggedWord));
    }

    @Test
    public void testIsVerb() {
        for (final POS pos : verbs)
            Assert.assertTrue(pos.isVerb());
        Arrays.stream(POS.values())
              .filter(p -> !Util.arrayContains(verbs, p))
              .map(POS::isVerb)
              .forEach(Assert::assertFalse);
    }

    @Test
    public void testValueOf() {
        for (final String tag : possibleTags)
            try {
                POS.valueOf(tag);
            } catch (final IllegalArgumentException e) {
                fail("POS does not account for all possible tags in Lexicon: "
                        + tag);
            }
    }

    @Test
    public void testStaticIsVerb() {
        for (final String taggedWord : taggedText)
            if (taggedWord.startsWith("jumps"))
                Assert.assertTrue(POS.isVerb(taggedWord));
            else
                Assert.assertFalse(POS.isVerb(taggedWord));
    }

    @Test
    public void testFromTaggedWord() {
        POS[] tags = new POS[taggedText.length];
        for (int i = 0; i < tags.length; i++)
            tags[i] = POS.fromTaggedWord(taggedText[i]);
        Assert.assertArrayEquals(testTags, tags);
    }

    @Test
    public void testTagText() {
        List<String> ls = POS.tagText(testText);
        Assert.assertArrayEquals(taggedText, ls.toArray(new String[ls.size()]));
    }

    @Test
    public void testTagStream() {
        Assert.assertArrayEquals(taggedText,
                POS.tagStream(testText).toArray(String[]::new));
    }

}
