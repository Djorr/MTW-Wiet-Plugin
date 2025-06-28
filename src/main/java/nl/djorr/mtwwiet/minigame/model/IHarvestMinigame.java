package nl.djorr.mtwwiet.minigame.model;

import org.bukkit.entity.Player;
import nl.djorr.mtwwiet.plant.model.PlantData;
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

    boolean isActive();
    Player getPlayer();
    PlantData getPlant();
} 