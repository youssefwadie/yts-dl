package com.github.youssefwadie.ytsdl.model;


import lombok.Data;

@Data
public final class DownloadBag {
    private String httpLink;
    private final String magnetLink;

    public DownloadBag(String magnetLink) {
        this.magnetLink = magnetLink;
    }
}
