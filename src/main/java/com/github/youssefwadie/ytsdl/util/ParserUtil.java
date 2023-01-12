package com.github.youssefwadie.ytsdl.util;

public final class ParserUtil {
    private ParserUtil() {
    }

    /**
     * <p>
     * Converts string from the given object,
     * if the given object is a string, just return it, otherwise return empty string.
     * </p>
     *
     * @param object {@code nullable}
     * @return a string representation of the object
     */
    public static String getString(Object object) {
        if (!(object instanceof String)) return "";
        return object.toString();
    }
}
