package com.github.youssefwadie.ytsdl.config;

import com.github.youssefwadie.ytsdl.download.DownloadProperties;
import com.github.youssefwadie.ytsdl.util.Assert;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public final class DownloadConfigProcessor {
    private final static Pattern DOWNLOAD_COMMAND_REGEX = Pattern.compile(".+%s.*");

    private final static String OPERATING_SYSTEM = System.getProperty("os.name").toLowerCase();
    private final static String CONFIG_FILE_NAME = "yts-cli.conf";


    private DownloadConfigProcessor() {
    }

    public static DownloadProperties readConfig() {
        val configFilePath = getConfigFilePath();
        return readConfig(configFilePath.toAbsolutePath());
    }

    public static DownloadProperties write(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        val configFilePath = getConfigFilePath();
        write(key, value, configFilePath);
        return readConfig(configFilePath);
    }

    private static DownloadProperties readConfig(final Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            throw new IllegalStateException("Unable to find the config file: " + configFilePath.toAbsolutePath());
        }

        try (val input = Files.newInputStream(configFilePath)) {
            val properties = new Properties();
            properties.load(input);
            val httpDownloadCommand = properties.getProperty("http-download-command");
            val torrentDownloadCommand = properties.getProperty("torrent-download-command");
            validateDownloadCommand(httpDownloadCommand, "http-download-command");
            validateDownloadCommand(torrentDownloadCommand, "torrent-download-command");
            return new DownloadProperties(
                    httpDownloadCommand,
                    torrentDownloadCommand
            );

        } catch (IOException e) {
            throw new RuntimeException("Error while reading the config file: " + e.getMessage());
        }
    }


    private static void write(String key, String value, Path configFilePath) {

        if (!Files.exists(configFilePath)) {
            throw new IllegalStateException("Unable to find the config file: " + configFilePath.toAbsolutePath());
        }

        try (val input = Files.newInputStream(configFilePath);
             val output = Files.newOutputStream(configFilePath)) {

            val properties = new Properties();
            properties.load(input);
            properties.put(key, value);

            properties.store(output, null);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the config file: " + e.getMessage());
        }
    }

    private static void validateDownloadCommand(String downloadCommand, String commandName) {
        Assert.nonNull(downloadCommand, String.format("%s must be set", commandName));

        if (!DOWNLOAD_COMMAND_REGEX.matcher(downloadCommand).matches()) {
            throw new IllegalArgumentException("invalid command expected: <command> %s, found " + downloadCommand);
        }
    }

    public static Path getConfigFilePath() {
        return switch (OPERATING_SYSTEM.toLowerCase()) {
            case "linux" -> linuxConfigPath();
            case "win" -> winConfigPath();
            default -> throw new IllegalStateException("Unexpected value: " + OPERATING_SYSTEM.toLowerCase());
        };
    }

    private static Path winConfigPath() {
        val localappdata = System.getenv("LOCALAPPDATA");
        if (localappdata != null) return Path.of(localappdata, CONFIG_FILE_NAME);

        val userHome = System.getProperty("user.home");
        Objects.requireNonNull(userHome, "cannot get the user home directory");
        return Path.of(userHome, "AppData", "Local", CONFIG_FILE_NAME);
    }

    private static Path linuxConfigPath() {
        val xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfigHome != null) return Path.of(xdgConfigHome, CONFIG_FILE_NAME);

        val userHome = System.getProperty("user.home");
        Objects.requireNonNull(userHome, "cannot get the user home directory");
        return Path.of(userHome, ".config", CONFIG_FILE_NAME);
    }
}
