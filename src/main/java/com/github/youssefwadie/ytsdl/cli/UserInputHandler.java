package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.model.Subtitle;
import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import lombok.val;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import static com.github.youssefwadie.ytsdl.cli.ConsoleUtils.*;

public class UserInputHandler {
    private final Scanner scanner;

    public UserInputHandler(ScannerHolder scannerHolder) {
        this.scanner = scannerHolder.getScanner();
    }


    public Optional<Title> handleTitleSelection(final List<Title> titles, boolean printDescription) {
        Objects.requireNonNull(titles, "titles must not be null");
        if (titles.isEmpty()) {
            throw new IllegalStateException("no titles found");
        }
        if (titles.size() == 1) {
            return Optional.of(titles.get(0));
        }
        if (printDescription) {
            ConsoleUtils.printList(titles, title -> String.format("%s%n%s", title.title(), title.description()));
        } else {
            ConsoleUtils.printList(titles, title -> String.format("%s", title.title()));
        }

        val titleNumber = getChoiceNumber(titles.size());
        if (titleNumber == -1) {
            return Optional.empty();
        }
        return Optional.of(titles.get(titleNumber - 1));
    }

    public boolean downloadPrompt() {
        return handleYesOrNo("Proceed in downloading");
    }

    private int getChoiceNumber(int end) {
        printYellow("-1 to exit\n");
        while (true) {
            printCyan(String.format("1 - %d > ", end));
            val readLine = scanner.nextLine().trim();
            if (readLine.isBlank()) {
                continue;
            }
            try {
                val num = Integer.parseInt(readLine);
                if (num == -1) {
                    return -1;
                }
                if (num < 1 || num > end) {
                    printError("out of range");
                } else {
                    return num;
                }
            } catch (NumberFormatException ex) {
                printError(String.format("invalid input expected integer found %s.%n", readLine));
            }
        }
    }

    public String handleQuery() {
        printPrompt("Search Title");
        String query = scanner.nextLine().trim();
        while (query.isBlank()) {
            printPrompt();
            query = scanner.nextLine().trim();
        }
        return query;
    }

    public boolean handlePrintingDescription() {
        return handleYesOrNo("Print Description");
    }

    private boolean handleYesOrNo(String message) {
        printBlue(String.format("%s (y/N) ", message));
        val ans = scanner.nextLine().toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }

    public Title.Quality handleTitleQuality(Title title) {
        val qualities = title.torrentLinks().stream().map(TorrentLink::quality).toList();
        printPrompt("Choose Quality " + qualities);
        String ans;
        Title.Quality quality;

        do {
            ans = scanner.nextLine();
            quality = Title.getQuality(ans);
            if (Objects.isNull(quality)) {
                printError("Invalid choice");
            } else {
                return quality;
            }
            printPrompt();
        } while (true);
    }

    public boolean handleGettingSubtitle() {
        return handleYesOrNo("Subtitle ?");
    }

    public Optional<Subtitle> handleSubtitleSelection(List<Subtitle> subtitles) {
        if (subtitles == null) return Optional.empty();
        if (subtitles.isEmpty()) {
            throw new IllegalStateException("no titles found");
        }

        if (subtitles.size() == 1) {
            return Optional.of(subtitles.get(0));
        }

        printList(subtitles, subtitle -> String.format("%s [%d*]", subtitle.lang(), subtitle.stars()));

        val subtitleNumber = getChoiceNumber(subtitles.size());
        if (subtitleNumber == -1) {
            return Optional.empty();
        }
        return Optional.of(subtitles.get(subtitleNumber - 1));
    }
}
