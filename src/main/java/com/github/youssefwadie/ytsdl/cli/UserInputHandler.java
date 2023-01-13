package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.model.Title;
import lombok.val;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class UserInputHandler {
    private final Scanner scanner;

    public UserInputHandler(ScannerHolder scannerHolder) {
        this.scanner = scannerHolder.getScanner();
    }


    public Title handleTitleSelection(final List<Title> titles, boolean printDescription) {
        Objects.requireNonNull(titles, "titles must not be null");
        if (titles.isEmpty()) {
            throw new IllegalStateException("no titles found");
        }
        if (titles.size() == 1) {
            return titles.get(0);
        }

        for (int i = 0; i < titles.size(); i++) {
            val title = titles.get(i);
            System.out.printf("%2d - %s%n", i + 1, title.title());
            if (printDescription) {
                System.out.println(title.description());
            }
        }

        val titleNumber = getTitleNumber(titles.size());
        return titles.get(titleNumber - 1);
    }

    private int getTitleNumber(int end) {
        while (true) {
            System.out.printf("1 - %d [+] ", end);
            val readLine = scanner.nextLine();
            try {
                val num = Integer.parseInt(readLine);
                if (num < 1 || num > end) {
                    System.err.println("out of range");
                } else {
                    return num;
                }
            } catch (NumberFormatException ex) {
                System.err.printf("invalid input expected integer found %s.%n", readLine);
            }
        }
    }

}
