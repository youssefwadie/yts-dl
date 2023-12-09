package com.github.youssefwadie.ytsdl.parsers;

import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import com.github.youssefwadie.ytsdl.util.CollectionUtils;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.minidev.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.youssefwadie.ytsdl.util.ParserUtil.getString;

@RequiredArgsConstructor
public class ApiClient {
    private final static String API_BASE_URL = "https://yts.mx/api/v2";
    private final static String LIST_API_URL = API_BASE_URL + "/list_movies.json";

    private final HttpClient httpClient;

    @SneakyThrows
    public List<Title> search(String searchTerm) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LIST_API_URL + "?query_term=" + searchTerm))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        return mapJsonToTitlesList(body);
    }


    @SuppressWarnings("unchecked")
    private List<Title> mapJsonToTitlesList(String json) {
        val context = JsonPath.parse(json);
        final JSONArray movies = context.read("$.data.movies");

        if (CollectionUtils.isEmpty(movies)) {
            return Collections.emptyList();
        }

        return movies.stream()
                .map(movie -> (Map<String, Object>) movie)
                .map(entry -> {
                    val title = getString(entry.get("title_long"));
                    val description = getString(entry.get("summary"));
                    val url = getString(entry.get("url"));
                    val torrents = parseTorrentLinks((JSONArray) entry.get("torrents"));
                    return new Title(title, description, url, torrents);
                })
                .toList();

    }

    @SuppressWarnings("unchecked")
    private List<TorrentLink> parseTorrentLinks(JSONArray torrents) {
        if (CollectionUtils.isEmpty(torrents)) {
            return Collections.emptyList();
        }

        return torrents.stream()
                .map(torrent -> (Map<String, String>) torrent)
                .map(entry -> new TorrentLink(
                        Title.getQuality(getString(entry.get("quality"))),
                        getString(entry.get("url")),
                        getString(entry.get("size"))
                ))
                .toList();
    }

}
