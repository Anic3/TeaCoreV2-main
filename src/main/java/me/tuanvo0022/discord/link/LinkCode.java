package me.tuanvo0022.discord.link;

import java.util.UUID;

public final class LinkCode {

    private final String code;
    private final long discordId;
    private final long expiresAt;

    public LinkCode(String code, long discordId, long expiresAt) {
        this.code = code;
        this.discordId = discordId;
        this.expiresAt = expiresAt;
    }

    public String getCode() {
        return code;
    }

    public long getDiscordId() {
        return discordId;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
