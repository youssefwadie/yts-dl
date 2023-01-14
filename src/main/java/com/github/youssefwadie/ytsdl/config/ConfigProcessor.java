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

import static com.github.youssefwadie.ytsdl.config.ConfigConstants.HTTP_DOWNLOAD_COMMAND_CONFIG_KEY;
import static com.github.youssefwadie.ytsdl.config.ConfigConstants.TORRENT_DOWNLOAD_COMMAND_CONFIG_KEY;

public final class ConfigProcessor {
    private final static Pattern DOWNLOAD_COMMAND_REGEX = Pattern.compile(".+%s.*");

    private final static String OPERATING_SYSTEM = System.getProperty("os.name").toLowerCase();
    private final static String CONFIG_FILE_NAME = "yts-cli.conf";


    private ConfigProcessor() {
    }

    public static DownloadProperties readConfig() {
        val configFilePath = getConfigFilePath();
        return readConfig(configFilePath.toAbsolutePath());
    }

    public static void write(String key, String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        val configFilePath = getConfigFilePath();
        write(key, value, configFilePath);
    }

    private static DownloadProperties readConfig(final Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            throw new ConfigParserException("Unable to find the config file: " + configFilePath.toAbsolutePath());
        }

        try (val input = Files.newInputStream(configFilePath)) {
            val properties = new Properties();
            properties.load(input);
            val httpDownloadCommand = properties.getProperty(HTTP_DOWNLOAD_COMMAND_CONFIG_KEY);
            val torrentDownloadCommand = properties.getProperty(TORRENT_DOWNLOAD_COMMAND_CONFIG_KEY);
            validateDownloadCommand(httpDownloadCommand, HTTP_DOWNLOAD_COMMAND_CONFIG_KEY);
            validateDownloadCommand(torrentDownloadCommand, TORRENT_DOWNLOAD_COMMAND_CONFIG_KEY);
            return new DownloadProperties(
                    httpDownloadCommand,
                    torrentDownloadCommand
            );

        } catch (IOException e) {
            throw new ConfigParserException("Error while reading the config file: " + e.getMessage());
        }
    }


    private static void write(String key, String value, Path configFilePath) {

        if (!Files.exists(configFilePath)) {
            throw new ConfigParserException("Unable to find the config file: " + configFilePath.toAbsolutePath());
        }

        validateDownloadCommand(value, key);

        try (val input = Files.newInputStream(configFilePath)) {
            val properties = new Properties();
            properties.load(input);
            properties.put(key, value);

            val output = Files.newOutputStream(configFilePath);
            properties.store(output, null);
            output.close();
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading the config file: " + e.getMessage());
        }
    }

    private static void validateDownloadCommand(String downloadCommand, String commandName) {
        if (downloadCommand == null) {
            throw new ConfigParserException(String.format("%s must be set", commandName));
        }

        if (!DOWNLOAD_COMMAND_REGEX.matcher(downloadCommand).matches()) {
            throw new ConfigParserException("invalid command expected: <command> %s, found " + downloadCommand);
        }
    }

    public static Path getConfigFilePath() {
        return switch (OPERATING_SYSTEM.toLowerCase()) {
            case "linux" -> linuxConfigPath();
            case "win" -> winConfigPath();
            default -> throw new ConfigParserException("Unexpected value: " + OPERATING_SYSTEM.toLowerCase());
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
