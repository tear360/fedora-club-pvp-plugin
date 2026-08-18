package fr.duelplugin.models;

import org.bukkit.entity.Player;

import java.util.UUID;

public class DuelRequest {

    private final UUID sender;
    private final UUID receiver;
    private final DuelGameMode mode;
    private final long timestamp;

    public DuelRequest(UUID sender, UUID receiver, DuelGameMode mode) {
        this.sender = sender;
        this.receiver = receiver;
        this.mode = mode;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getSender() { return sender; }
    public UUID getReceiver() { return receiver; }
    public DuelGameMode getMode() { return mode; }
    public long getTimestamp() { return timestamp; }

    public Player getSenderPlayer() {
        return org.bukkit.Bukkit.getPlayer(sender);
    }

    public Player getReceiverPlayer() {
        return org.bukkit.Bukkit.getPlayer(receiver);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 60_000;
    }
}
