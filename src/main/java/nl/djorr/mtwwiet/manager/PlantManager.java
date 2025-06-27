package nl.djorr.mtwwiet.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import nl.djorr.mtwwiet.model.PlantData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Beheert alle geplante wietplanten in de wereld.
 * Threadsafe voor meerdere spelers.
 */
public class PlantManager {
    private static PlantManager instance;
    private final Map<Location, PlantData> plants = new ConcurrentHashMap<>();
    private File plantFile;
    private YamlConfiguration plantConfig;
    private Plugin plugin;

    public static PlantManager getInstance() {
        if (instance == null) instance = new PlantManager();
        return instance;
    }

    public void init(Plugin plugin) {
        this.plugin = plugin;
        plantFile = new File(plugin.getDataFolder(), "plants.yml");
        if (!plantFile.exists()) {
            try { plantFile.createNewFile(); } catch (IOException ignored) {}
        }
        plantConfig = YamlConfiguration.loadConfiguration(plantFile);
        loadPlants();
    }

    public void savePlants() {
        plantConfig.set("plants", null);
        for (Map.Entry<Location, PlantData> entry : plants.entrySet()) {
            Location loc = entry.getKey();
            PlantData data = entry.getValue();
            String path = "plants." + loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
            plantConfig.set(path + ".world", loc.getWorld().getName());
            plantConfig.set(path + ".x", loc.getBlockX());
            plantConfig.set(path + ".y", loc.getBlockY());
            plantConfig.set(path + ".z", loc.getBlockZ());
            plantConfig.set(path + ".owner", data.owner.toString());
            plantConfig.set(path + ".plantedAt", data.plantedAt);
            plantConfig.set(path + ".growTime", data.growTime);
            plantConfig.set(path + ".ready", data.ready);
            plantConfig.set(path + ".oogstSpeler", data.oogstSpeler != null ? data.oogstSpeler.toString() : null);
        }
        try { plantConfig.save(plantFile); } catch (IOException ignored) {}
    }

    public void loadPlants() {
        plants.clear();
        if (plantConfig.getConfigurationSection("plants") == null) return;
        for (String key : plantConfig.getConfigurationSection("plants").getKeys(false)) {
            try {
                String world = plantConfig.getString("plants." + key + ".world");
                int x = plantConfig.getInt("plants." + key + ".x");
                int y = plantConfig.getInt("plants." + key + ".y");
                int z = plantConfig.getInt("plants." + key + ".z");
                UUID owner = UUID.fromString(plantConfig.getString("plants." + key + ".owner"));
                long plantedAt = plantConfig.getLong("plants." + key + ".plantedAt");
                long growTime = plantConfig.getLong("plants." + key + ".growTime");
                boolean ready = plantConfig.getBoolean("plants." + key + ".ready");
                String oogstSpelerStr = plantConfig.getString("plants." + key + ".oogstSpeler");
                UUID oogstSpeler = oogstSpelerStr != null ? UUID.fromString(oogstSpelerStr) : null;
                World w = org.bukkit.Bukkit.getWorld(world);
                if (w == null) continue;
                Location loc = new Location(w, x, y, z);
                PlantData data = new PlantData();
                data.owner = owner;
                data.plantedAt = plantedAt;
                data.growTime = growTime;
                data.ready = ready;
                data.oogstSpeler = oogstSpeler;
                data.plantBlockLocation = loc;
                plants.put(loc, data);
                // Start groei timer als plant nog niet klaar is
                if (!ready) {
                    startGrowthTimer(data, loc);
                }
            } catch (Exception ignored) {}
        }
    }

    private void startGrowthTimer(PlantData data, Location loc) {
        long elapsed = System.currentTimeMillis() - data.plantedAt;
        long remaining = Math.max(0, data.growTime - elapsed);
        if (remaining > 0) {
            data.growthTask = new BukkitRunnable() {
                @Override
                public void run() {
                    data.ready = true;
                    // Plaats volgroeide plant
                    org.bukkit.block.Block plantBlock = loc.getBlock();
                    org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
                    if (topBlock.getType() == org.bukkit.Material.AIR) {
                        plantBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                        plantBlock.setData((byte) 3);
                        topBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                        topBlock.setData((byte) 11);
                    }
                    savePlants();
                }
            };
            data.growthTask.runTaskLater(plugin, remaining / 50); // Convert to ticks
        } else {
            data.ready = true;
            savePlants();
        }
    }

    public void addPlant(Location loc, PlantData data) {
        plants.put(loc, data);
        savePlants();
    }

    public void removePlant(Location loc) {
        plants.remove(loc);
        savePlants();
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