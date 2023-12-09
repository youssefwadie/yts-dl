package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.parsers.ApiClient;
import com.github.youssefwadie.ytsdl.parsers.SubtitleParser;
import com.github.youssefwadie.ytsdl.download.DownloadService;
import lombok.val;
import picocli.CommandLine;

import java.net.http.HttpClient;

public class CLI {
    public static void main(String[] args) {
        HttpClient httpClient = HttpClient.newHttpClient();
        val apiClient = new ApiClient(httpClient);
        val subtitleParser = new SubtitleParser(httpClient);
        val downloadService = new DownloadService();

        val commandLineRunner = new CommandLineRunner(apiClient, subtitleParser, downloadService);
        val cmd = new CommandLine(commandLineRunner);
        cmd.addSubcommand(commandLineRunner.getConfigCommand());
        cmd.addSubcommand(commandLineRunner.getNonInteractiveDownloadAndSearchCommand());
        cmd.addSubcommand(commandLineRunner.getInteractiveSubCommand());
        cmd.setExecutionExceptionHandler(new ExecutionExceptionHandler());
        cmd.setParameterExceptionHandler(new ParameterMessageHandler());
        int exitCode = cmd.execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }

    }
}
