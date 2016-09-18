// Copyright 2003-2008.  Mark Watson (markw@markwatson.com).  All rights reserved.
// This software is released under the LGPL (www.fsf.org)
// For an alternative non-GPL license: contact the author
// THIS SOFTWARE COMES WITH NO WARRANTY

package com.knowledgebooks.nlp.util;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * <p/>
 * Copyright 2007 by Mark Watson. All rights reserved.
 * <p/>
 */
public final class Tokenizer {
    /**
     * utility to tokenize an input string into an Array of Strings
     * @param s2 string containing words to tokenize
     * @return a List<String> of parsed tokens
     */
    public static List<String> wordsToList(String str) {
        return wordsToStream(str).collect(Collectors.toList());
    }

    public static Stream<String> wordsToStream(String str) {
        String s2 = stripControlCharacters(str);
        Stream.Builder<String> sb = Stream.builder();
        String x;
        try {
            StreamTokenizer str_tok = new StreamTokenizer(new StringReader(s2));
            str_tok.whitespaceChars('"', '"');
            str_tok.whitespaceChars('\'', '\'');
            str_tok.whitespaceChars('/', '/');
            //str_tok.wordChars(':', ':');
            while (str_tok.nextToken() != StreamTokenizer.TT_EOF) {
                String s;
                switch (str_tok.ttype) {
                    case StreamTokenizer.TT_EOL:
                        s = ""; // we will ignore this
                        break;
                    case StreamTokenizer.TT_WORD:
                        s = str_tok.sval;
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        s = "" + (int) str_tok.nval; // .toString(); // we will ignore this

                        break;
                    default :
                        s = String.valueOf((char) str_tok.ttype);
                }
                if (s.length() < 1)
                    continue;
                //if (s.indexOf("-") > -1) continue;
                //s = s.toLowerCase();
                if (s.endsWith(".")) {
                    // first check for abreviations like "N.J.":
                    int index = s.indexOf(".");
                    if (index < (s.length() - 1)) {
                        sb.accept(s);
                    } else {
                        sb.accept(s.substring(0, s.length() - 1));
                        sb.accept(".");
                    }
                } else if (s.endsWith(",")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0)
                        sb.accept(x);
                    sb.accept(",");
                } else if (s.endsWith(";")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0)
                        sb.accept(x);
                    sb.accept(";");
                } else if (s.endsWith("?")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0)
                        sb.accept(x);
                    sb.accept("?");
                } else if (s.endsWith(":")) {
                    x = s.substring(0, s.length() - 1);
                    if (x.length() > 0)
                        sb.accept(x);
                    sb.accept(":");
                } else {
                    sb.accept(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.build();
    }

    static private String stripControlCharacters(String s) {
        StringBuffer sb = new StringBuffer(s.length() + 1);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch > 256 || ch == '\n' || ch == '\t' || ch == '\r' || ch == 226) {
                sb.append(' ');
                continue;
            }
            //System.out.println(" ch: " + ch + " (int)ch: " + (int)ch + " Character.isISOControl(ch): " + Character.isISOControl(ch));
            if ((int) ch < 129)
                sb.append(ch);
            else
                sb.append(' ');
        }
        return sb.toString();
    }
}

