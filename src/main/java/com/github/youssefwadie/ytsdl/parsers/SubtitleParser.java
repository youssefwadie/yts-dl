package com.github.youssefwadie.ytsdl.parsers;

import com.github.youssefwadie.ytsdl.exceptions.UnableToParseException;
import com.github.youssefwadie.ytsdl.model.Language;
import com.github.youssefwadie.ytsdl.model.Subtitle;
import com.github.youssefwadie.ytsdl.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public class SubtitleParser {
    private final HttpClient client;

    public List<Subtitle> findAllForTitle(String titleUrl) {
        try {
            return getAvailableSubtitles(titleUrl);
        } catch (UnableToParseException e) {
            throw e;
        } catch (Exception e) {
            throw new UnableToParseException(e);
        }
    }

    public Subtitle getSubtitle(String titleUrl, Language language) {
        Objects.requireNonNull(titleUrl, "titleUrl must not be null.");
        Objects.requireNonNull(language, "lang must not be null.");
        try {
            val subtitles = getAvailableSubtitles(titleUrl);
            if (CollectionUtils.isEmpty(subtitles)) {
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
        } catch (IOException | InterruptedException e) {
            throw new UnableToParseException(e);
        }
    }


    public String getDownloadLink(Subtitle subtitle) {
        if (subtitle == null) {
            throw new UnableToParseException("subtitle not found");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(subtitle.downloadPageUrl()))
                    .header("Accept", "text/html")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String html = response.body();

            val document = Jsoup.parse(html);
            val anchorElement = document.selectFirst("a.download-subtitle");
            if (anchorElement == null) {
                throw new UnableToParseException("unable to find the download link");
            }
            val uri = URI.create(subtitle.downloadPageUrl());
            String domainName = subtitle.downloadPageUrl().replace(uri.getPath(), "");
            return String.format("%s%s", domainName, anchorElement.attr("href"));
        } catch (IOException | InterruptedException e) {
            throw new UnableToParseException(e);
        }
    }

    private List<Subtitle> getAvailableSubtitles(String titleUrl) throws IOException, InterruptedException {
        String subtitlesPage = getSubtitlesPage(titleUrl);
        return getSubtitles(subtitlesPage);
    }

    private List<Subtitle> getSubtitles(String subtitleUrl) throws IOException, InterruptedException {
        URI subtitleURI = URI.create(subtitleUrl);
        HttpRequest request = HttpRequest.newBuilder(subtitleURI).build();
        HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
        String html = res.body();

        val document = Jsoup.parse(html);
        val subtitleTable = document
                .selectFirst("div.table-responsive")
                .select("table.other-subs");

        val subtitleElements = subtitleTable.select("tr");

        if (CollectionUtils.isEmpty(subtitleElements)) {
            return Collections.emptyList();
        }

        val subtitlePageBaseUrl = subtitleURI.getScheme() + "://" + subtitleURI.getHost();

        val subtitles = new ArrayList<Subtitle>();
        for (val subtitleElement : subtitleElements) {
            val langElement = subtitleElement.selectFirst("span.sub-lang");
            if (Objects.isNull(langElement)) continue;
            val rating = Integer.parseInt(subtitleElement.selectFirst("span.label").text());
            val lang = langElement.text();
            Elements td = subtitleElement.select("td");
            val subtitlePageUrl = subtitlePageBaseUrl + td.get(td.size() - 3).selectFirst("a").attr("href");
            subtitles.add(new Subtitle(lang, subtitlePageUrl, rating));
        }

        return subtitles;
    }

    private String getSubtitlesPage(String titleUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(titleUrl))
                .header("Accept", "text/html")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return getSubtitlePagesLink(response.body());
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
