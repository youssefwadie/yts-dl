package com.github.youssefwadie.ytsdl.download;

import lombok.val;

import java.io.IOException;

import static com.github.youssefwadie.ytsdl.util.Assert.nonNull;

public class DownloadService {
    /**
     * Starts a http download.
     *
     * @param httpLink   - not null.
     * @param properties - not null.
     * @return {@link Process} the started process.
     * @throws IllegalArgumentException if {@literal httpLink}  or {@literal properties} is null.
     * @throws IOException              if an I/O error occurs
     */
    public Process startHttpDownload(String httpLink, DownloadProperties properties) throws IOException {
        nonNull(httpLink, "httpLink must not be null.");
        nonNull(properties, "properties must not be null");
        val fullCommand = fullCommand(properties.httpDownloadCommand(), httpLink);

        return new ProcessBuilder(fullCommand)
                .inheritIO()
                .start();
    }

    /**
     * Start torrent download.
     *
     * @param properties - not null.
     * @return {@link Process} the started process.
     * @throws IllegalArgumentException if {@literal magnetLink}  or {@literal properties} is null.
     * @throws IOException              if an I/O error occurs
     */
    public Process startTorrentDownload(String magnetLink, DownloadProperties properties) throws IOException {
        nonNull(magnetLink, "magnetLink must not be null.");
        nonNull(properties, "properties must not be null");
        String[] fullCommand = fullCommand(properties.torrentDownloadCommand(), magnetLink);

        return new ProcessBuilder(fullCommand)
                .inheritIO()
                .start();

    }

    private static String[] fullCommand(String downloadCommand, String link) {
        return new String[]{
                downloadCommand.replace("%s", "").trim(),
                link.trim()
        };
    }
}
