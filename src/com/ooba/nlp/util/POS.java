package com.ooba.nlp.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ooba.nlp.fasttag.FastTag;

public enum POS {
    CC("CC"), CD("CD"), DT("DT"), EX("EX"), FW("FW"), IN("IN"), JJ("JJ"),
    JJR("JJR"), JJS("JJS"), LS("LS"), MD("MD"), NN("NN"), NNP("NNP"),
    NNPS("NNPS"), NNS("NNS"), PDT("PDT"), POS("POS"), PP("PP"), PP$("PP$"),
    PRP("PRP"), PRP$("PRP$"), PRP$R("PRP$R"), RB("RB"), RBR("RBR"), RBS("RBS"),
    RP("RP"), SYM("SYM"), TO("TO"), UH("UH"), VB("VB"), VBD("VBD"), VBG("VBG"),
    VBN("VBN"), VBP("VBP"), VBZ("VBZ"), WDT("WDT"), WP("WP"), WP$("WP$"),
    WRB("WRB"), DOLLAR_SIGN("$"), HASH_SIGN("#"), QUOTE("\""), OPEN_PAREN("("),
    CLOSE_PAREN(")"), COMMA(","), PERIOD("."), COLON(":"), UNKNOWN("^");

    private static String delim = "/";

    private String str;

    POS(final String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public boolean isNoun() {
        return str.startsWith("N");
    }

    public static boolean isNoun(final String taggedWord) {
        return fromTaggedWord(taggedWord).isNoun();
    }

    public boolean isVerb() {
        return str.startsWith("V");
    }

    public static boolean isVerb(final String taggedWord) {
        return fromTaggedWord(taggedWord).isVerb();
    }

    public static POS fromTaggedWord(final String taggedWord) {
        return valueOf(taggedWord.substring(taggedWord.lastIndexOf(delim) + 1));
    }

    public static List<String> tagText(final String text) {
        final Map<String, String[]> lexicon = FastTag.buildLexicon(
                "lexicon.txt");
        return FastTag.tag(lexicon, Tokenizer.wordsToList(text));
    }

    public static Stream<String> tagStream(final String text) {
        final Map<String, String[]> lexicon = FastTag.buildLexicon(
                "lexicon.txt");
        return Tokenizer.wordsToStream(text)
                        .sequential()
                        .map(FastTag.tag(lexicon));
    }
}
