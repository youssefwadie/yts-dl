package com.github.youssefwadie.ytsdl.util;

import com.github.youssefwadie.ytsdl.model.Language;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class Languages {

    private static final String INVALID_FILE_HEADER = "invalid lang.csv header, expected, Language name,Language Alias";
    private static final String INVALID_FILE_SYNTAX = "invalid syntax expected, `lang name, lang alias`. found %s";
    private static final List<Language> AVAILABLE_LANGUAGES = new ArrayList<>();

    private Languages() {
    }


    static {
        try {
            val langFileURL = Languages.class.getClassLoader().getResource("lang.csv");
            if (langFileURL == null) {
                fallbackLanguages();
            } else {
                val langFilePath = Paths.get(langFileURL.toURI());
                readFile(langFilePath);
            }
        } catch (Exception e) {
            log.warn("error encountered while reading lang.csv with message {}", e.getMessage());
            fallbackLanguages();
        }

    }

    private static void readFile(Path langsFilePath) throws IOException {
        try (val reader = Files.newBufferedReader(langsFilePath)) {
            String line = reader.readLine();
            if (line == null) {
                log.warn("lang.csv is empty");
                fallbackLanguages();
                return;
            }

            checkHeader(line);

            while ((line = reader.readLine()) != null) {
                readLine(line);
            }
        }

    }

    private static void readLine(String line) {
        if (line.isBlank()) return;
        val splitLine = line.split(",");
        if (splitLine.length != 2) {
            throw new IllegalStateException(INVALID_FILE_SYNTAX.formatted(line));
        }
        addLang(splitLine[0], splitLine[1]);
    }

    private static void checkHeader(String line) {
        val splitLine = line.split(",");

        if (splitLine.length != 2) {
            throw new IllegalStateException(INVALID_FILE_HEADER);
        }

        if (!splitLine[0].strip().equalsIgnoreCase("Language name")) {
            throw new IllegalStateException(INVALID_FILE_HEADER);
        }

        if (!splitLine[1].strip().equalsIgnoreCase("Language Alias")) {
            throw new IllegalStateException(INVALID_FILE_HEADER);
        }
    }

    private static void addLang(String name, String alias) {
        AVAILABLE_LANGUAGES.add(new Language(name.strip(), alias.strip()));
    }

    private static void fallbackLanguages() {
        AVAILABLE_LANGUAGES.clear();
        addLang("English", "eng");
        addLang("Arabic", "ara");
        log.info("falling back to the default languages: {}", AVAILABLE_LANGUAGES.stream().map(Language::name).toList());
    }

    public static Language getLanguage(String nameOrAlias) {
        return AVAILABLE_LANGUAGES
                .stream()
                .filter(language -> language.alias().equalsIgnoreCase(nameOrAlias) || language.name().equalsIgnoreCase(nameOrAlias))
                .findFirst()
                .orElseGet(() -> {
                    val fallbackLang = AVAILABLE_LANGUAGES.get(0);
                    log.warn("{} not found falling back to {}", nameOrAlias, fallbackLang.name());
                    return fallbackLang;
                });
    }
}
