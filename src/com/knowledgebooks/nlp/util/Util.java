package com.knowledgebooks.nlp.util;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class Util {
	private Util(){}
    public static <T> UnaryOperator<T> onlyIf(Predicate<T> pred,
            UnaryOperator<T> fun) {
        return arg -> (pred.test(arg)) ? fun.apply(arg) : arg;
    }

    public static <T> T onlyIf(Predicate<T> pred, UnaryOperator<T> fun, T arg) {
        return (pred.test(arg)) ? fun.apply(arg) : arg;
    }
    
    public static boolean containsFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Shamelessly copied from Apache Commons: https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/ArrayUtils.java
     */
    
    private static int INDEX_NOT_FOUND = -1;

    public static <T> int arrayIndexOf(T[] arr, T thing) {
        return arrayIndexOf(arr, thing, 0);
    }

    public static <T> int arrayIndexOf(T[] arr, T thing, int start) {
        if (arr == null)
            return INDEX_NOT_FOUND;
        start = Integer.max(start, 0);
        if (thing == null) {
            for (int i = start; i < arr.length; i++)
                if (arr[i] == null)
                    return i;
        } else
            for (int i = start; i < arr.length; i++)
                if (arr[i].equals(thing))
                    return i;
        return INDEX_NOT_FOUND;
    }

    public static <T> boolean arrayContains(T[] arr, T thing) {
        return arrayIndexOf(arr, thing) != INDEX_NOT_FOUND;
    }
}
