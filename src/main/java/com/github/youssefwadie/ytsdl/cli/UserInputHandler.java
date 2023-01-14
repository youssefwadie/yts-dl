package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.model.Subtitle;
import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import lombok.val;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;

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

        for (int i = 0; i < titles.size(); i++) {
            val title = titles.get(i);
            System.out.printf("%2d - %s%n", i + 1, title.title());
            if (printDescription) {
                System.out.println(title.description());
            }
        }

        val titleNumber = getChoiceNumber(titles.size());
        if (titleNumber == -1) {
            return Optional.empty();
        }
        return Optional.of(titles.get(titleNumber - 1));
    }

    public boolean downloadPrompt() {
        System.out.print("Proceed in downloading (y/N) ");
        return handleYesOrNo();
    }

    private int getChoiceNumber(int end) {
        while (true) {
            System.out.printf("1 - %d > ", end);
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

    public String handleQuery() {
        System.out.println("Search Title");
        System.out.print("> ");
        return scanner.nextLine();
    }

    public boolean handlePrintingDescription() {
        System.out.print("Print description (y/N) ");
        return handleYesOrNo();
    }

    private boolean handleYesOrNo() {
        val ans = scanner.nextLine().toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }

    public Title.Quality handleTitleQuality(Title title) {
        val qualities = title.torrentLinks().stream().map(TorrentLink::quality).toList();
        System.out.println("Choose Quality " + qualities);
        String ans;
        Title.Quality quality;

        do {
            System.out.print("> ");
            ans = scanner.nextLine();
            quality = Title.getQuality(ans);
            if (Objects.isNull(quality)) {
                System.out.println("Invalid choice");
            } else {
                return quality;
            }
        } while (true);
    }

    public boolean handleGettingSubtitle() {
        System.out.print("Download a subtitle (y/N) ");
        return handleYesOrNo();
    }

    public Optional<Subtitle> handleSubtitleSelection(List<Subtitle> subtitles) {
        if (subtitles == null) return Optional.empty();
        if (subtitles.isEmpty()) {
            throw new IllegalStateException("no titles found");
        }

        if (subtitles.size() == 1) {
            return Optional.of(subtitles.get(0));
        }

        for (int i = 0; i < subtitles.size(); i++) {
            val subtitle = subtitles.get(i);
            System.out.printf("%2d - %s%n", i + 1, subtitle.lang());
        }

        val subtitleNumber = getChoiceNumber(subtitles.size());
        if (subtitleNumber == -1) {
            return Optional.empty();
        }
        return Optional.of(subtitles.get(subtitleNumber - 1));
    }
}
