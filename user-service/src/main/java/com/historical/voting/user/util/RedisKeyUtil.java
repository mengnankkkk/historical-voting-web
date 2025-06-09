package com.historical.voting.user.util;

public class RedisKeyUtil {
    public static String viewCountKey(Object videoId) {
        return "video:viewCount:" + videoId;
    }

    public static String viewIpKey(Object videoId, String ip) {
        return "video:ip:" + videoId + ":" + ip;
    }

    public static String likeKey(Object videoId) {
        return "video:like:" + videoId;
    }

    public static String dislikeKey(Object videoId) {
        return "video:dislike:" + videoId;
    }

    public static String shareKey(Object videoId) {
        return "video:share:" + videoId;
    }

    public static Long extractId(String key) {
        return Long.valueOf(key.replaceAll("\\D+", ""));
    }
}

