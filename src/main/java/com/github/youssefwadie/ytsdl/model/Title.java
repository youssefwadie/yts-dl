package com.github.youssefwadie.ytsdl.model;

import java.util.List;

public record Title(String title, String description, String url, List<TorrentLink> torrentLinks) {
}
