package nl.yourname.weedplugin.manager;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import nl.yourname.weedplugin.model.PlantData;
import org.bukkit.entity.Player;

/**
 * Beheert alle geplante wietplanten in de wereld.
 * Threadsafe voor meerdere spelers.
 */
public class PlantManager {
    private static PlantManager instance;
    public static PlantManager getInstance() {
        if (instance == null) instance = new PlantManager();
        return instance;
    }

    // Map: plant locatie -> PlantData
    private final Map<Location, PlantData> plants = new ConcurrentHashMap<>();

    public void addPlant(Location loc, PlantData data) {
        plants.put(loc, data);
    }

    public void removePlant(Location loc) {
        plants.remove(loc);
    }

    public PlantData getPlant(Location loc) {
        return plants.get(loc);
    }

    public boolean isPlantAt(Location loc) {
        return plants.containsKey(loc);
    }

    public java.util.Collection<PlantData> getAllPlants() {
        return plants.values();
    }

    /**
     * Start een minigame voor de plant op de gegeven locatie, als mogelijk.
     * @return true als gestart, false als niet mogelijk
     */
    public boolean startMinigameAt(Location loc, Player player) {
        PlantData data = getPlant(loc);
        if (data == null || !data.ready) return false;
        data.startMinigame(player);
        return true;
    }
} 