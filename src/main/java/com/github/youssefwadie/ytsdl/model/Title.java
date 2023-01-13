package com.github.youssefwadie.ytsdl.model;

import java.util.List;

public record Title(String title, String description, String url, List<TorrentLink> torrentLinks) {

    public static Quality getQuality(String quality) {
        if (quality == null) return Quality.HD;
        return switch (quality.toLowerCase()) {
            case "2160p" -> Quality.UHD;
            case "1080p" -> Quality.FHD;
            case "720p" -> Quality.HD;
            default -> Quality.SD480;
        };
    }

    public enum Quality {
        UHD,
        FHD,
        HD,
        SD480;

        @Override
        public String toString() {
            return switch (this) {
                case UHD -> "4K";
                case FHD -> "1080p";
                case HD -> "720p";
                default -> "480p";
            };
        }

    }
}
