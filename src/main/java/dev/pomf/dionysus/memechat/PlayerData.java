package dev.pomf.dionysus.memechat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class PlayerData {
    private final Player player;
    private UUID lastReplied;

    public PlayerData(@Nonnull Player player) {
        this.player = player;
        this.lastReplied = null;
    }

    public Player getLastReplied() {
        if (lastReplied != null)
            return Bukkit.getPlayer(lastReplied);
        return null;
    }

    public void setLastReplied(UUID lastReplied) {
        this.lastReplied = lastReplied;
    }

    @Nonnull
    public Player getPlayer() {
        return player;
    }
}
