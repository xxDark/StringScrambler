package me.xdark.stringscrambler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;

public final class StringUnsafe {
    private static final char[] EMPTY = new char[0];
    private static final MethodHandle GET_STRING_VALUE;
    private static final MethodHandle SET_STRING_VALUE;
    private static final MethodHandle SET_STRING_HASHCODE;

    private StringUnsafe() { }

    public static void setValue(String str, char[] value) {
        try {
            SET_STRING_VALUE.invokeExact(str, value);
            SET_STRING_HASHCODE.invokeExact(str, 0);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    public static void free(String str) {
        try {
            Arrays.fill((char[]) GET_STRING_VALUE.invokeExact(str), (char) 0);
        } catch (Throwable t) {
            throw new Error(t);
        }
        setValue(str, EMPTY);
    }

    static {
        try {
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            MethodHandles.publicLookup();
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) field.get(null);
            GET_STRING_VALUE = lookup.findGetter(String.class, "value", char[].class);
            SET_STRING_VALUE = lookup.findSetter(String.class, "value", char[].class);
            SET_STRING_HASHCODE = lookup.findSetter(String.class, "hash", int.class);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }
}
