package com.github.youssefwadie.ytsdl.util;

import com.jayway.jsonpath.PathNotFoundException;
import lombok.val;

import java.net.UnknownHostException;

import static com.github.youssefwadie.ytsdl.cli.ConsoleUtils.printError;

public final class ReactiveUtil {
    private ReactiveUtil() {
    }

    public static void handleError(Throwable throwable) {
        if (throwable instanceof PathNotFoundException) {
            printError("No results found");
        } else if (throwable instanceof UnknownHostException) {
            printError("[ERROR] Check your internet connection");
        } else {
            printError("[ERROR] " + throwable.getMessage());
        }
    }

}
