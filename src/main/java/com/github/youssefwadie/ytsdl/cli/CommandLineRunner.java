package com.github.youssefwadie.ytsdl.cli;


import com.github.youssefwadie.ytsdl.parsers.ApiClient;
import com.github.youssefwadie.ytsdl.parsers.SubtitleParser;
import com.github.youssefwadie.ytsdl.config.ConfigParserException;
import com.github.youssefwadie.ytsdl.config.ConfigProcessor;
import com.github.youssefwadie.ytsdl.download.DownloadConfig;
import com.github.youssefwadie.ytsdl.download.DownloadService;
import com.github.youssefwadie.ytsdl.model.DownloadBag;
import com.github.youssefwadie.ytsdl.util.Languages;
import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.github.youssefwadie.ytsdl.cli.ConsoleUtils.*;
import static com.github.youssefwadie.ytsdl.config.ConfigConstants.HTTP_DOWNLOAD_COMMAND_CONFIG_KEY;
import static com.github.youssefwadie.ytsdl.config.ConfigConstants.TORRENT_DOWNLOAD_COMMAND_CONFIG_KEY;
import static com.github.youssefwadie.ytsdl.config.ConfigProcessor.readConfig;

@Command(name = "yts-cli", versionProvider = YTSCliVersionProvider.class)
public class CommandLineRunner implements Runnable {
    @Option(names = {"-v", "--version"}, versionHelp = true, description = "print the version information and exit", defaultValue = "false")
    private boolean versionInfoRequested;


    @Option(names = {"-h", "--help"}, usageHelp = true,
            description = "print this help and exit", defaultValue = "false")
    private boolean usageHelpRequested;


    private final ApiClient apiClient;
    private final SubtitleParser subtitleParser;
    private final DownloadService downloadService;
    @Getter
    private final ConfigSubCommand configCommand;
    @Getter
    private final NonInteractiveDownloadAndSearchSubCommand nonInteractiveDownloadAndSearchCommand;
    @Getter
    private final InteractiveSubCommand interactiveSubCommand;

    private final UserInputHandler userInputHandler;
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public CommandLineRunner(final ApiClient apiClient, final SubtitleParser subtitleParser, final DownloadService downloadService) {
        this.apiClient = apiClient;
        this.subtitleParser = subtitleParser;
        this.downloadService = downloadService;
        this.configCommand = new ConfigSubCommand();
        this.userInputHandler = new UserInputHandler(ScannerHolder.INSTANCE);
        this.nonInteractiveDownloadAndSearchCommand = new NonInteractiveDownloadAndSearchSubCommand();
        this.interactiveSubCommand = new InteractiveSubCommand();
    }

    @Override
    @SneakyThrows
    public void run() {
        if (usageHelpRequested) {
            printUsage(this);
        } else {
            this.interactiveSubCommand.call();
        }
    }

    private void printUsage(Object command) {
        CommandLine.usage(command, System.out);
    }

    private TorrentLink getTorrentLink(Title title, Title.Quality quality) {
        return title.torrentLinks().stream()
                .filter(torrentLink -> torrentLink.quality() == quality)
                .findFirst()
                .orElse(title.torrentLinks().get(0));
    }


    private void download(DownloadBag bag, DownloadConfig downloadProps) throws IOException {
        downloadService.startTorrentDownload(bag.getMagnetLink(), downloadProps);
        if (Objects.nonNull(bag.getHttpLink())) {
            downloadService.startHttpDownload(bag.getHttpLink(), downloadProps);
        }
    }


    private void proceedDownloading(DownloadBag downloadBag, boolean proceedDownloading) throws IOException {
        if (proceedDownloading) {
            try {
                val downloadProps = readConfig();
                download(downloadBag, downloadProps);
            } catch (ConfigParserException ex) {
                printDownloadBag(downloadBag);
                throw ex;
            }
        } else {
            printDownloadBag(downloadBag);
        }
    }

    private void printDownloadBag(DownloadBag downloadBag) {
        printPurple(String.format("Torrent File Link: %s%n", downloadBag.getMagnetLink()));
        if (Objects.nonNull(downloadBag.getHttpLink())) {
            printPurple(String.format("Subtitle download link: %s%n", downloadBag.getHttpLink()));
        }
    }

    @Command(name = "non-interactive", aliases = "n", description = "non-interactive mode")
    private class NonInteractiveDownloadAndSearchSubCommand implements Callable<Integer> {
        @Parameters(arity = "1..", description = "search for a movie", paramLabel = "query")
        private List<String> query;


        @Option(names = {"-q", "--quality"}, description = "the quality of video to download", defaultValue = "HD")
        private Title.Quality quality;

        @Option(names = {"-s", "--subtitle"}, paramLabel = "lang")
        private String subtitleLang;

        @Option(arity = "0", names = {"-desc", "--description"}, description = "Print the description of the titles")
        private boolean printDescription = false;

        @Option(names = {"-d", "--download"}, description = "Download without asking")
        private boolean proceedDownloading = false;

        @Option(names = {"-h", "--help"}, usageHelp = true,
                description = "print this help and exit", defaultValue = "false")
        private boolean usageHelpRequested;

        @Override
        public Integer call() throws Exception {
            if (usageHelpRequested) {
                printUsage(this);
                return 0;
            }

            try {
                val titles = apiClient.search(String.join(" ", query));

                val titleOptional = userInputHandler.handleTitleSelection(titles, printDescription);
                if (titleOptional.isEmpty()) {
                    return 0;
                }
                val title = titleOptional.get();

                val torrentLink = getTorrentLink(title, quality);


                val downloadBag = new DownloadBag(torrentLink.url());
                if (subtitleLang != null) {
                    val lang = Languages.getLanguage(subtitleLang);
                    val subtitle = subtitleParser.getSubtitle(title.url(), lang);
                    val downloadLink = subtitleParser.getDownloadLink(subtitle);
                    downloadBag.setHttpLink(downloadLink);
                }

                proceedDownloading(downloadBag, userInputHandler.downloadPrompt());

                return 0;
            } catch (ConfigParserException ex) {
                System.out.println("corrupted configuration");
                printUsage(configCommand);
                throw ex;
            }
        }
    }

    @Command(name = "interactive", aliases = "i", description = "interactive mode")
    private class InteractiveSubCommand implements Callable<Integer> {

        @Option(names = {"-h", "--help"}, usageHelp = true,
                description = "print this help and exit", defaultValue = "false")
        private boolean usageHelpRequested;

        @Override
        public Integer call() throws Exception {
            if (usageHelpRequested) {
                printUsage(this);
                return 0;
            }

            try {
                String query = userInputHandler.handleQuery();
                val titles = apiClient.search(query);
                val printDescription = userInputHandler.handlePrintingDescription();
                val optionalTitle = userInputHandler.handleTitleSelection(titles, printDescription);
                if (optionalTitle.isEmpty()) {
                    return 0;
                }
                val title = optionalTitle.get();

                val quality = userInputHandler.handleTitleQuality(title);

                val torrentLink = getTorrentLink(title, quality);
                val downloadBag = new DownloadBag(torrentLink.url());

                if (userInputHandler.handleGettingSubtitle()) {
                    val subtitles = subtitleParser.findAllForTitle(title.url());
                    val chosenSubtitle = userInputHandler.handleSubtitleSelection(subtitles);
                    if (chosenSubtitle.isPresent()) {
                        String downloadLink = subtitleParser.getDownloadLink(chosenSubtitle.get());
                        downloadBag.setHttpLink(downloadLink);
                    }
                }

                proceedDownloading(downloadBag, userInputHandler.downloadPrompt());
                return 0;
            } catch (ConfigParserException ex) {
                System.out.println("corrupted configuration");
                printUsage(configCommand);
                throw ex;
            }
        }
    }

    @Command(name = "config", aliases = "conf", description = "manipulating the configuration file")
    private class ConfigSubCommand implements Callable<Integer> {
        @Option(arity = "1..",
                names = "-http", description = "http download command, syntax <command> %%s, %%s will be replaced with the actual download link",
                paramLabel = "command")
        private List<String> httpDownloadCommand;

        @Option(arity = "1..", names = "-torrent",
                description = "torrent download command, syntax <command> %%s, %%s will be replaced with the actual magnet link",
                paramLabel = "command")
        private List<String> torrentDownloadCommand;

        @Option(arity = "0", names = {"-p", "--print"}, description = "Print the current configuration")
        private boolean printConfiguration = false;

        @Option(names = {"-h", "--help"}, usageHelp = true,
                description = "print this help and exit", defaultValue = "false")
        private boolean usageHelpRequested;


        @Override
        public Integer call() throws Exception {
            if (usageHelpRequested) {
                printUsage(this);
                return 0;
            }
            if (printConfiguration) {
                printCurrentConfiguration();
                return 0;
            }

            if (httpDownloadCommand != null)
                ConfigProcessor.write(HTTP_DOWNLOAD_COMMAND_CONFIG_KEY, String.join(" ", httpDownloadCommand));

            if (torrentDownloadCommand != null)
                ConfigProcessor.write(TORRENT_DOWNLOAD_COMMAND_CONFIG_KEY, String.join(" ", torrentDownloadCommand));
            return 0;
        }
    }

    private void printCurrentConfiguration() {
        val currentConfig = readConfig();
        printGreen(String.format("Http Download Command: %s%n", currentConfig.httpDownloadCommand()));
        printGreen(String.format("Torrent Download Command: %s%n", currentConfig.torrentDownloadCommand()));
    }
}
