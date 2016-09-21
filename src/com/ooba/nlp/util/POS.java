package com.ooba.nlp.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ooba.nlp.fasttag.FastTag;

public enum POS {
    CC("CC"), CD("CD"), DT("DT"), EX("EX"), FW("FW"), IN("IN"), JJ("JJ"), 
    JJR("JJR"), JJS("JJS"), LS("LS"), MD("MD"), NN("NN"), NNP("NNP"), 
	NNPS("NNPS"), NNS("NNS"), PDT("PDT"), POS("POS"), PP("PP"), PP$("PP$"), 
	PRP("PRP"), RB("RB"), RBR("RBR"), RBS("RBS"), RP("RP"), SYM("SYM"),
    TO("TO"), UH("UH"), VB("VB"), VBD("VBD"), VBG("VBG"), VBN("VBN"),
    VBP("VBP"), VBZ("VBZ"), WDT("WDT"), WP("WP"), WP$("WP$"), WRB("WRB"),
    DOLLAR_SIGN("$"), HASH_SIGN("#"), QUOTE("\""), OPEN_PAREN("("),
    CLOSE_PAREN(")"), COMMA(","), PERIOD("."), COLON(":");

    private static String delim = "/";

    private String str;

    POS(String str) {
        this.str = str;
    }

    public String toString() {
        return str;
    }

    public boolean isNoun() {
        return str.startsWith("N");
    }

    public static boolean isNoun(String taggedWord) {
        return fromTaggedWord(taggedWord).isNoun();
    }

    public boolean isVerb() {
        return str.startsWith("V");
    }

    public static boolean isVerb(String taggedWord) {
        return fromTaggedWord(taggedWord).isVerb();
    }

    public static POS fromTaggedWord(String taggedWord) {
        int i = taggedWord.indexOf(delim);
		return (i < 0) ? SYM : valueOf(taggedWord.substring(i + 1));
    }

    public static List<String> tagText(String text) {
        Map<String, String[]> lexicon = FastTag
                .buildLexicon("data/lexicon.txt");
        return FastTag.tag(lexicon, Tokenizer.wordsToList(text));
    }

    public static Stream<String> tagStream(String text) {
        Map<String, String[]> lexicon = FastTag
                .buildLexicon("data/lexicon.txt");
        return Tokenizer.wordsToStream(text).sequential()
                .map(FastTag.tag(lexicon));
    }
}
