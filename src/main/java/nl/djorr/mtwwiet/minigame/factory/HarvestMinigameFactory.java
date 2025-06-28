package nl.djorr.mtwwiet.minigame.factory;

import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;
import nl.djorr.mtwwiet.minigame.model.HarvestMinigame;
import nl.djorr.mtwwiet.plant.model.PlantData;
import org.bukkit.entity.Player;

/**
 * Factory voor het aanmaken van harvest minigames.
 */
public class HarvestMinigameFactory {
    public static IHarvestMinigame create(Player player, PlantData plant) {
        return new HarvestMinigame(player, plant);
    }
} 