package nl.djorr.mtwwiet.minigame.model;

import org.bukkit.entity.Player;

/**
 * Houdt de status van een actieve minigame bij.
 */
public class MinigameState {
    private Player player;
    private long startTime;
    private boolean completed;
    private boolean failed;
    // Voeg meer velden toe indien nodig

    public MinigameState(Player player) {
        this.player = player;
        this.startTime = System.currentTimeMillis();
        this.completed = false;
        this.failed = false;
    }

    public Player getPlayer() { return player; }
    public long getStartTime() { return startTime; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isFailed() { return failed; }
    public void setFailed(boolean failed) { this.failed = failed; }
} 