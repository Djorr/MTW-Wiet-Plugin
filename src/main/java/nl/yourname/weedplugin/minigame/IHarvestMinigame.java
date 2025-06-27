package nl.yourname.weedplugin.minigame;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Interface voor oogst-minigames.
 */
public interface IHarvestMinigame {
    /**
     * Forceer een fail van de minigame (bijvoorbeeld als een nieuwe minigame gestart wordt).
     */
    void forceFail();

    /**
     * Ruim alle resources van de minigame netjes op.
     */
    void forceCleanup();

    /**
     * Verwerk een click in de minigame-GUI.
     */
    void handleClick(InventoryClickEvent event);
} 