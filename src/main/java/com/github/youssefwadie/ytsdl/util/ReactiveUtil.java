package com.github.youssefwadie.ytsdl.util;

import com.jayway.jsonpath.PathNotFoundException;
import lombok.val;

import java.net.UnknownHostException;

public final class ReactiveUtil {
    private ReactiveUtil() {
    }

    public static void handleError(Throwable throwable) {
        if (throwable instanceof PathNotFoundException) {
            System.err.println("No results found");
        } else if (throwable instanceof UnknownHostException) {
            System.err.println("[ERROR] Check your internet connection");
        } else {
            System.err.println("[ERROR] " + throwable.getMessage());
        }
    }

}
