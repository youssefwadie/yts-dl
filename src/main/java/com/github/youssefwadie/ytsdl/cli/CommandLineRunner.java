package com.github.youssefwadie.ytsdl.cli;


import com.github.youssefwadie.ytsdl.ApiClient;
import com.github.youssefwadie.ytsdl.SubtitleParser;
import com.github.youssefwadie.ytsdl.config.DownloadConfigProcessor;
import com.github.youssefwadie.ytsdl.download.DownloadService;
import com.github.youssefwadie.ytsdl.model.Languages;
import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Command(name = "yts-cli", versionProvider = YTSCliVersionProvider.class)
public class CommandLineRunner implements Runnable {
    @Parameters(arity = "1..", description = "search for a movie", paramLabel = "query")
    private List<String> query;


    @Option(names = {"-q", "--quality"}, description = "the quality of video to download", defaultValue = "HD")
    private Title.Quality quality;

    @Option(names = {"-s", "--subtitle"}, paramLabel = "lang")
    private String subtitleLang;

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "print the version information and exit", defaultValue = "false")
    private boolean versionInfoRequested;

    @Option(arity = "0", names = {"-d", "--description"}, description = "Print the description of the titles", defaultValue = "false")
    private boolean printDescription;

    @Option(names = {"-h", "--help"}, usageHelp = true,
            description = "print this help and exit", defaultValue = "false")
    private boolean usageHelpRequested;


    private final ApiClient apiClient;
    private final SubtitleParser subtitleParser;
    private final DownloadService downloadService;

    @Override
    public void run() {
        if (usageHelpRequested) {
            CommandLine.usage(this, System.out);
            return;
        }
        if (versionInfoRequested) {
            System.out.println("version ....");
            return;
        }

        try (val scannerHolder = ScannerHolder.INSTANCE) {
            val titlesMono = apiClient.search(String.join(" ", query));
            val userInputHandler = new UserInputHandler(scannerHolder);
            val titles = titlesMono.block();

            val title = userInputHandler.handleTitleSelection(titles, printDescription);

            val downloadProps = DownloadConfigProcessor.readConfig();

            val torrentLink = getTorrentLink(title, quality);


            downloadService.startTorrentDownload(torrentLink.url(), downloadProps);

            if (subtitleLang != null) {
                val lang = Languages.getLanguage(subtitleLang);
                val subtitle = subtitleParser.getSubtitle(title.url(), lang);
                val downloadLink = subtitleParser.getDownloadLink(subtitle).block();
                downloadService.startHttpDownload(downloadLink, downloadProps);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TorrentLink getTorrentLink(Title title, Title.Quality quality) {
        return title.torrentLinks().stream()
                .filter(torrentLink -> torrentLink.quality() == quality)
                .findFirst()
                .orElse(title.torrentLinks().get(0));
    }
}
