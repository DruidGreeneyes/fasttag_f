package com.ooba.nlp.util;

import java.lang.reflect.Array;
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

	public static <T> T[] subarray(final T[] arr, final int start) {
		return subarray(arr, start, arr.length);
	}

	public static <T> T[] subarray(final T[] arr, final int start, final int endExclusive) {
    	final int s = Integer.max(start, 0);
    	final int e = Integer.min(endExclusive, arr.length);
    	final int newSize = e - s;
    	
    	@SuppressWarnings("unchecked") // We know this is okay because arr and newArr are both of type T.
		final T[] newArr = (T[]) Array.newInstance(arr.getClass().getComponentType(), newSize);
    	if (newSize > 0)
    		System.arraycopy(arr, s, newArr, 0, newSize);
    	return newArr;
    }
}
