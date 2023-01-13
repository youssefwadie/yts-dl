package com.github.youssefwadie.ytsdl.util;

public class Assert {

    public static void nonNull(Object obj1, String message1, Object obj2, String message2) {
        if (obj1 == null && obj2 == null) {
            throw new IllegalArgumentException(String.format("%s and %s", message1, message2));
        }

        if (obj1 == null) throw new IllegalArgumentException(message1);
        if (obj2 == null) throw new IllegalArgumentException(message2);
    }

    public static void nonNull(Object obj, String message) {
        if (obj == null) throw new IllegalArgumentException(message);
    }
}
