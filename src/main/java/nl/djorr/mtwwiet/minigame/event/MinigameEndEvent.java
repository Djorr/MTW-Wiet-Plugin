package nl.djorr.mtwwiet.minigame.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;

public class MinigameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IHarvestMinigame minigame;
    public MinigameEndEvent(IHarvestMinigame minigame) {
        this.minigame = minigame;
    }
    public IHarvestMinigame getMinigame() { return minigame; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
} 