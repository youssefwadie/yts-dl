package com.github.youssefwadie.ytsdl.cli;

import lombok.val;

import java.util.List;
import java.util.function.Function;

public class ConsoleUtils {
    public static void printError(String message) {
        System.out.printf("%s%s%s%n", AnsiColors.ANSI_RED, message, AnsiColors.ANSI_RESET);
    }

    public static void printCyan(String message) {
        System.out.printf("%s%s%s", AnsiColors.ANSI_CYAN, message, AnsiColors.ANSI_RESET);
    }

    public static void printYellow(String message) {
        System.out.printf("%s%s%s", AnsiColors.ANSI_YELLOW, message, AnsiColors.ANSI_RESET);
    }

    public static void printGreen(String message) {
        System.out.printf("%s%s%s", AnsiColors.ANSI_GREEN, message, AnsiColors.ANSI_RESET);
    }

    public static void printPurple(String message) {
        System.out.printf("%s%s%s", AnsiColors.ANSI_PURPLE, message, AnsiColors.ANSI_RESET);
    }

    public static void printBlue(String message) {
        System.out.printf("%s%s%s", AnsiColors.ANSI_BLUE, message, AnsiColors.ANSI_RESET);
    }

    public static void printPrompt() {
        printCyan("> ");
    }

    public static void printPrompt(String message) {
        System.out.printf("%s%s%n", AnsiColors.ANSI_CYAN, message);
        printCyan("> ");
    }

    public static <T> void printList(List<T> items, Function<T, String> mapper) {
        boolean flag = true;
        for (int i = 0; i < items.size(); i++) {
            val item = items.get(i);
            if (flag) {
                printCyan(String.format("%2d - %s%n", i + 1, mapper.apply(item)));
            } else {
                printPurple(String.format("%2d - %s%n", i + 1, mapper.apply(item)));
            }
            flag = !flag;
        }
    }

}
