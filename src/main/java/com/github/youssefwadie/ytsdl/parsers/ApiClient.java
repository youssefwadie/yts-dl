package com.github.youssefwadie.ytsdl.parsers;

import com.github.youssefwadie.ytsdl.model.Title;
import com.github.youssefwadie.ytsdl.model.TorrentLink;
import com.github.youssefwadie.ytsdl.util.ReactiveUtil;
import com.jayway.jsonpath.JsonPath;
import io.netty.handler.codec.http.QueryStringEncoder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minidev.json.JSONArray;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.youssefwadie.ytsdl.util.ParserUtil.getString;

@RequiredArgsConstructor
public class ApiClient {
    private final static String API_BASE_URL = "https://yts.mx/api/v2";
    private final static String LIST_API_URL = API_BASE_URL + "/list_movies.json";

    private final HttpClient client;


    public Mono<List<Title>> search(String searchTerm) {
        val queryString = new QueryStringEncoder(LIST_API_URL);
        queryString.addParam("query_term", searchTerm);
        return client.get()
                .uri(queryString.toString())
                .responseSingle((httpClientResponse, byteBufMono) -> byteBufMono.asString())
                .map(this::mapJsonToTitlesList)
                .doOnError(ReactiveUtil::handleError)
                ;

    }


    @SuppressWarnings("unchecked")
    private List<Title> mapJsonToTitlesList(String json) {
        val context = JsonPath.parse(json);
        final JSONArray movies = context.read("$.data.movies");
        val titles = new ArrayList<Title>();

        for (val movie : movies) {
            val entry = (Map<String, Object>) movie;
            val title = getString(entry.get("title_long"));
            val description = getString(entry.get("summary"));
            val url = getString(entry.get("url"));
            val torrents = parseTorrentLinks((JSONArray) entry.get("torrents"));
            titles.add(new Title(title, description, url, torrents));
        }
        return titles;
    }

    @SuppressWarnings("unchecked")
    private List<TorrentLink> parseTorrentLinks(JSONArray torrents) {
        val torrentLinks = new ArrayList<TorrentLink>();
        for (val torrent : torrents) {
            val entry = (Map<String, String>) torrent;
            val torrentLink = new TorrentLink(
                    Title.getQuality(getString(entry.get("quality"))),
                    getString(entry.get("url")),
                    getString(entry.get("size"))
            );
            torrentLinks.add(torrentLink);
        }
        return torrentLinks;
    }

}
