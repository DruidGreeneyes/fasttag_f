// Copyright 2003-2008.  Mark Watson (markw@markwatson.com).  All rights reserved.
// This software is released under the LGPL (www.fsf.org)
// For an alternative non-GPL license: contact the author
// THIS SOFTWARE COMES WITH NO WARRANTY

package com.knowledgebooks.nlp.fasttag;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.knowledgebooks.nlp.util.Util;

import pair.Pair;

/**
 * <p/>
 * Copyright 2002-2007 by Mark Watson. All rights reserved.
 * <p/>
 */
public class FastTag {

	private static final Map<String, String[]> lexicon = buildLexicon();

	/**
	 * 
	 * @param word
	 * @return true if the input word is in the lexicon, otherwise return false
	 */
    public static boolean wordInLexicon(Map<String, String[]> lexicon,
            String word) {
        return lexicon.containsKey(word)
                || lexicon.containsKey(word.toLowerCase());
	}

    public static String[] getWordFromLexicon(Map<String, String[]> lexicon,
            String word) {
        return lexicon.getOrDefault(word, lexicon.get(word.toLowerCase()));
    }

    // The following is a list of rules through which we run the sequence of
    // words.
    private static Function<Pair<String, String[]>, Pair<String, String>> rule_0 = p -> {
        String[] ss = p.right;
            String res;
            if (ss == null)
                if (p.left.length() == 1)
                res = p.left + "^";
                else
                    res = "NN";
            else
                res = ss[0];
        return p.mapRight(res);
    };

    // rule 1: DT, {VBD | VBP} --> DT, NN
    private static UnaryOperator<Pair<String, String>> rule_1() {
        ArrayBlockingQueue<String> window = new ArrayBlockingQueue<>(2);
        String[] verbs = {"VBD", "VBP", "VB"};
        return p -> {
            window.add(p.right);
            return Util.onlyIf(pair -> (window.size() == 2
                    && window.remove().equals("DT")
                    && Util.arrayContains(verbs, pair.right)),
                    Pair.F.replaceRight("NN"), p);
        };
    }

    // rule 2: convert a noun to a number (CD) if "." appears in the word
    private static UnaryOperator<Pair<String, String>> rule_2 = Util
            .onlyIf(pair -> (pair.right.startsWith("N")
                && (pair.left.contains(".") || Util.containsFloat(pair.left))),
                    Pair.F.replaceRight("CD"));
    
    // rule 3: convert a noun to a past participle if words.get(i) ends with
    // "ed"
    private static UnaryOperator<Pair<String, String>> rule_3 = Util
            .onlyIf(pair -> (pair.right
                    .startsWith("N") && pair.left.endsWith("ed")),
                    Pair.F.replaceRight("VBN"));

    // rule 4: convert any type to adverb if it ends in "ly";
    private static UnaryOperator<Pair<String, String>> rule_4 = Util
            .onlyIf(pair -> (pair.left
                    .endsWith("ly")), Pair.F.replaceRight("RB"));

    // rule 5: convert a common noun (NN or NNS) to a adjective if it ends with
    // "al"
    private static UnaryOperator<Pair<String, String>> rule_5 = Util
            .onlyIf(pair -> (pair.right
                    .startsWith("NN") && pair.left.endsWith("al")),
                    Pair.F.replaceRight("JJ"));

    // rule 6: convert a noun to a verb if the preceeding work is "would"
    private static UnaryOperator<Pair<String, String>> rule_6() {
        ArrayBlockingQueue<String> window = new ArrayBlockingQueue<>(2);
        return p -> {
            window.add(p.left);
            return Util.onlyIf(
                    pair -> (window.size() == 2 && p.right.startsWith("NN")
                            && window.remove().equalsIgnoreCase("would")),
                    Pair.F.replaceRight("VB"), p);
        };
    }

    // rule 7: if a word has been categorized as a common noun and it ends with
    // "s",
    // then set its type to plural common noun (NNS)
    private static UnaryOperator<Pair<String, String>> rule_7 = Util.onlyIf(
            pair -> (pair.right.equals("NN") && pair.left.endsWith("s")),
            Pair.F.replaceRight("NNS"));

    // rule 8: convert a common noun to a present participle verb (i.e., a
    // gerand)
    private static UnaryOperator<Pair<String, String>> rule_8 = Util.onlyIf(
            pair -> (pair.right.equals("NN") && pair.left.endsWith("ing")),
            Pair.F.replaceRight("VBG"));

    /**
     * 
     * @param words
     *            list of strings to tag with parts of speech
     * @return list of strings for part of speech tokens
     */
    public static List<String> tag(Map<String, String[]> lexicon,
            List<String> words) {
        return words.stream().sequential()
                .map(w -> Pair.make(w, getWordFromLexicon(lexicon, w)))
                .map(rule_0).map(rule_1()).map(rule_2).map(rule_3).map(rule_4)
                .map(rule_5).map(rule_6()).map(rule_7).map(rule_8)
                .map(Pair.F.into((a, b) -> a + "/" + b))
                .collect(Collectors.toList());
    }

    /**
     * Same as tag for List[String]s, but only operates on a single word, so
     * that you can map it across a string[] or something if you want. MAKE SURE
     * THIS RUNS SEQUENTIALLY, as it needs to preserve some information about
     * its current context in the sentence, and that breaks if it runs in
     * parallel.
     * 
     * @param lexicon
     * @param word
     * @return the word, tagged: word/tag
     */
    public static String tag(Map<String, String[]> lexicon, String word) {
        UnaryOperator<Pair<String, String>> rule_1 = rule_1();
        UnaryOperator<Pair<String, String>> rule_6 = rule_6();
        Pair<String, String> p = rule_0
                .apply(Pair.make(word, getWordFromLexicon(lexicon, word)));
        p = rule_1.apply(p);
        p = rule_2.apply(p);
        p = rule_3.apply(p);
        p = rule_4.apply(p);
        p = rule_5.apply(p);
        p = rule_6.apply(p);
        p = rule_7.apply(p);
        p = rule_8.apply(p);
        return p.intoFun((a, b) -> a + "/" + b);
    }

    /*********************************
     * Old Code from here on out.
     ********************************/

    public static List<String> _tag(Map<String, String[]> lexicon,
            List<String> words) {
        // Stream<String> res = words.stream().sequential().map(w ->
        // Pair.make(w, getWordFromLexicon(lexicon,
        // w))).map(rule_0).map(rule_1(2)).map(rule_2)
        
		List<String> ret = new ArrayList<String>(words.size());
		for (int i = 0, size = words.size(); i < size; i++) {
			String[] ss = (String[]) lexicon.get(words.get(i));
			// 1/22/2002 mod (from Lisp code): if not in hash, try lower case:
			if (ss == null)
				ss = lexicon.get(words.get(i).toLowerCase());
			if (ss == null && words.get(i).length() == 1)
				ret.add(words.get(i) + "^");
			else if (ss == null)
				ret.add("NN");
			else
				ret.add(ss[0]);
		}
		/**
		 * Apply transformational rules
		 **/
		for (int i = 0; i < words.size(); i++) {
			String word = ret.get(i);
			// rule 1: DT, {VBD | VBP} --> DT, NN
			if (i > 0 && ret.get(i - 1).equals("DT")) {
				if (word.equals("VBD") || word.equals("VBP") || word.equals("VB")) {
					ret.set(i, "NN");
				}
			}
			// rule 2: convert a noun to a number (CD) if "." appears in the word
			if (word.startsWith("N")) {
				if (words.get(i).indexOf(".") > -1) {
					ret.set(i, "CD");
				}
				try {
					Float.parseFloat(words.get(i));
					ret.set(i, "CD");
				} catch (Exception e) { // ignore: exception OK: this just means
										// that the string could not parse as a
										// number
				}
			}
			// rule 3: convert a noun to a past participle if words.get(i) ends with "ed"
			if (ret.get(i).startsWith("N") && words.get(i).endsWith("ed"))
				ret.set(i, "VBN");
			// rule 4: convert any type to adverb if it ends in "ly";
			if (words.get(i).endsWith("ly"))
				ret.set(i, "RB");
			// rule 5: convert a common noun (NN or NNS) to a adjective if it ends with "al"
			if (ret.get(i).startsWith("NN") && words.get(i).endsWith("al"))
				ret.set(i, "JJ");
			// rule 6: convert a noun to a verb if the preceeding work is "would"
			if (i > 0 && ret.get(i).startsWith("NN")
					&& words.get(i - 1).equalsIgnoreCase("would"))
				ret.set(i, "VB");
			// rule 7: if a word has been categorized as a common noun and it ends with "s",
			// then set its type to plural common noun (NNS)
			if (ret.get(i).equals("NN") && words.get(i).endsWith("s"))
				ret.set(i, "NNS");
			// rule 8: convert a common noun to a present participle verb (i.e., a gerand)
			if (ret.get(i).startsWith("NN") && words.get(i).endsWith("ing"))
				ret.set(i, "VBG");
		}
		return ret;
	}

	/**
	 * Simple main test program
	 * 
	 * @param args
	 *            string to tokenize and tag
	 */
	public static void main(String[] args) {
        String text;
		if (args.length == 0) {
			System.out.println("Usage: argument is a string like \"The ball rolled down the street.\"\n\nSample run:\n");
            text = "The ball rolled down the street.";
        } else
            text = args[0];

        List<String> words = com.knowledgebooks.nlp.util.Tokenizer
                .wordsToList(text);
        List<String> tags = _tag(lexicon, words);
			for (int i = 0; i < words.size(); i++)
				System.out.println(words.get(i) + "/" + tags.get(i));
	}

	private static Map<String, String[]> buildLexicon() {
		Map<String, String[]> lexicon = new HashMap<String, String[]>();
		try {
			InputStream ins = FastTag.class.getClassLoader().getResourceAsStream("lexicon.txt");
			if (ins == null) {
				ins = new FileInputStream("data/lexicon.txt");
			}
			Scanner scanner = new Scanner(ins);
			scanner.useDelimiter(System.getProperty("line.separator"));
			while (scanner.hasNext()) {
				String line = scanner.next();
				int count = 0;
				for (int i = 0, size = line.length(); i < size; i++) {
					if (line.charAt(i) == ' ') {
						count++;
					}
				}
				if (count == 0) {
					continue;
				}
				String[] ss = new String[count];
				Scanner lineScanner = new Scanner(line);
				lineScanner.useDelimiter(" ");
				String word = lineScanner.next();
				count = 0;
				while (lineScanner.hasNext()) {
					ss[count++] = lineScanner.next();
				}
				lineScanner.close();
				lexicon.put(word, ss);
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableMap(lexicon);
	}

}
