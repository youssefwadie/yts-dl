package com.github.youssefwadie.ytsdl.cli;

import com.github.youssefwadie.ytsdl.ApiClient;
import com.github.youssefwadie.ytsdl.SubtitleParser;
import com.github.youssefwadie.ytsdl.model.Languages;
import com.github.youssefwadie.ytsdl.model.Subtitle;
import lombok.val;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;

public class CLI {
    public static void main(String[] args) throws IOException {
        try (val scannerHolder = ScannerHolder.INSTANCE) {

            val httpClient = HttpClient.create();

            val apiClient = new ApiClient(httpClient);
            val subtitleParser = new SubtitleParser(httpClient);
            val titlesMono = apiClient.search("No Country for Old Men");
            val userInputHandler = new UserInputHandler(scannerHolder);
            val titles = titlesMono.block();
            val lang = Languages.getLanguage("ara");
            val title = userInputHandler.handleTitleSelection(titles);

            Subtitle subtitle = subtitleParser.getSubtitle(title.url(), lang);
            System.out.println(subtitle);

            System.in.read();
        }

    }
}
