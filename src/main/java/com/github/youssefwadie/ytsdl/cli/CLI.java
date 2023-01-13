package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.ApiClient;
import com.github.youssefwadie.ytsdl.SubtitleParser;
import com.github.youssefwadie.ytsdl.config.DownloadConfigProcessor;
import com.github.youssefwadie.ytsdl.download.DownloadProperties;
import com.github.youssefwadie.ytsdl.download.DownloadService;
import lombok.val;
import picocli.CommandLine;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;

public class CLI {
    public static void main(String[] args) {

        val httpClient = HttpClient.create();

        val apiClient = new ApiClient(httpClient);
        val subtitleParser = new SubtitleParser(httpClient);
        val downloadService = new DownloadService();

        val commandLineRunner = new CommandLineRunner(apiClient, subtitleParser, downloadService);
        CommandLine cmd = new CommandLine(commandLineRunner);
        cmd.setExecutionExceptionHandler(new ExecutionExceptionHandler());
        cmd.setParameterExceptionHandler(new ParameterMessageHandler());
        int exitCode = cmd.execute(args);
        if (exitCode != 0) {
            System.exit(1);
        }

    }
}
