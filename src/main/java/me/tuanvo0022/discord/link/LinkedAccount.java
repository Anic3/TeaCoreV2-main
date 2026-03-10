package me.tuanvo0022.discord.link;

import java.util.UUID;

public final class LinkedAccount {

    private final UUID playerId;
    private final long discordId;
    private volatile boolean locked;
    private final long linkedAt;

    public LinkedAccount(UUID playerId, long discordId, boolean locked, long linkedAt) {
        this.playerId = playerId;
        this.discordId = discordId;
        this.locked = locked;
        this.linkedAt = linkedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getDiscordId() {
        return discordId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getLinkedAt() {
        return linkedAt;
    }
}
