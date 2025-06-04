package com.historical.voting.user.entity;

public enum UserRank {
    NEWCOMER("新人"),
    ACTIVE_VOTER("活跃投票者"),
    HISTORY_ENTHUSIAST("历史爱好者"),
    SENIOR_COMMENTATOR("资深评论家"),
    HISTORY_MASTER("历史大师");

    private final String displayName;

    UserRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 