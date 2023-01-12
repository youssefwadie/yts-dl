package com.github.youssefwadie.ytsdl.util;

public final class ReactiveUtil {
    private ReactiveUtil() {}

    public static void handleError(Throwable throwable) {
        System.err.println("[ERROR] " + throwable.getMessage());
    }

}
