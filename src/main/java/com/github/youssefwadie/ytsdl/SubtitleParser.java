package com.github.youssefwadie.ytsdl;

import com.github.youssefwadie.ytsdl.exceptions.UnableToParseException;
import com.github.youssefwadie.ytsdl.model.Language;
import com.github.youssefwadie.ytsdl.model.Subtitle;
import com.github.youssefwadie.ytsdl.util.ReactiveUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public class SubtitleParser {
    private final HttpClient httpClient;

    public Subtitle getSubtitle(String titleUrl, Language language) {
        Objects.requireNonNull(titleUrl, "titleUrl must not be null.");
        Objects.requireNonNull(language, "lang must not be null.");
        val subtitles = getAvailableSubtitles(titleUrl).block();
        if (Objects.isNull(subtitles)) {
            throw new UnableToParseException("no subtitles found");
        }

        val bestSubtitle = new AtomicReference<Subtitle>(null);
        subtitles.forEach(subtitle -> {
            val bestSubtitleValue = bestSubtitle.get();
            if (subtitle.lang().equalsIgnoreCase(language.name())) {
                if (bestSubtitleValue == null) {
                    bestSubtitle.set(subtitle);
                } else if (subtitle.stars() > bestSubtitleValue.stars()) {
                    bestSubtitle.set(subtitle);
                }
            }
        });

        if (bestSubtitle.get() == null) {
            log.warn("{} not found", language.name());
        }

        return bestSubtitle.get();
    }

    public Mono<String> getDownloadLink(Subtitle subtitle) {
        if (subtitle == null) return Mono.error(() -> new UnableToParseException("subtitle not found"));
        return httpClient
                .get()
                .uri(subtitle.downloadPageUrl())
                .responseSingle((httpClientResponse, byteBufMono) -> byteBufMono.asString())
                .flatMap(response -> {
                    val document = Jsoup.parse(response);
                    val anchorElement = document.selectFirst("a.download-subtitle");
                    if (anchorElement == null) {
                        return Mono.error(() -> new UnableToParseException("unable to find the download link"));
                    }
                    try {
                        val uri = new URI(subtitle.downloadPageUrl());
                        String domainName = subtitle.downloadPageUrl().replace(uri.getPath(), "");
                        return Mono.just(String.format("%s%s", domainName, anchorElement.attr("href")));
                    } catch (URISyntaxException e) {
                        return Mono.error(new UnableToParseException(e));
                    }
                });

    }

    private Mono<List<Subtitle>> getAvailableSubtitles(String titleUrl) {
        return getSubtitlesPage(titleUrl)
                .doOnError(ReactiveUtil::handleError)
                .onErrorComplete()
                .flatMap(this::getSubtitles);
    }

    private Mono<List<Subtitle>> getSubtitles(String subtitleUrl) {
        return httpClient
                .get()
                .uri(subtitleUrl)
                .responseSingle((httpClientResponse, byteBufMono) -> byteBufMono.asString())
                .map(response -> {
                    val document = Jsoup.parse(response);
                    val subtitleTable = document
                            .selectFirst("div.table-responsive")
                            .select("table.other-subs");
                    val subtitleElements = subtitleTable.select("tr");
                    val subtitles = new ArrayList<Subtitle>();

                    for (val subtitleElement : subtitleElements) {
                        val langElement = subtitleElement.selectFirst("span.sub-lang");
                        if (Objects.isNull(langElement)) continue;
                        val rating = Integer.parseInt(subtitleElement.selectFirst("span.label").text());
                        val lang = langElement.text();
                        Elements td = subtitleElement.select("td");
                        val subtitlePageUrl = td.get(td.size() - 3).selectFirst("a").attr("href");
                        subtitles.add(new Subtitle(lang, subtitlePageUrl, rating));
                    }

                    return subtitles;
                });

    }

    private Mono<String> getSubtitlesPage(String titleUrl) {
        return httpClient
                .get()
                .uri(titleUrl)
                .responseSingle((httpClientResponse, byteBufMono) -> byteBufMono.asString())
                .map(SubtitleParser::getSubtitlePagesLink);
    }


    private static String getSubtitlePagesLink(String response) {
        val document = Jsoup.parse(response);
        val movieInfo = document.getElementById("movie-info");
        if (movieInfo == null) {
            throw new UnableToParseException("unable to find the movie info section");
        }
        val buttons = movieInfo.select("a.button");
        if (buttons.isEmpty()) {
            throw new UnableToParseException("unable to find subtitle section");
        }

        val subtitlesButton = buttons.get(buttons.size() - 1);
        val href = subtitlesButton.attr("href");
        if (href.contains("request-subtitle")) {
            throw new UnableToParseException("no subtitles found");
        }
        return href;
    }

}
