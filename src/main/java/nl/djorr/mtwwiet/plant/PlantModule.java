package nl.djorr.mtwwiet.plant;

import nl.djorr.mtwwiet.core.PluginModule;
import nl.djorr.mtwwiet.minigame.model.IHarvestMinigame;
import nl.djorr.mtwwiet.plant.model.PlantData;
import nl.djorr.mtwwiet.minigame.HarvestMinigameModule;
import nl.djorr.mtwwiet.minigame.event.MinigameEndEvent;
import nl.djorr.mtwwiet.core.PluginContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service voor het beheren van alle planten, groei, timers en persistentie.
 */
public class PlantModule implements PluginModule {
    private final Map<Location, PlantData> plants = new ConcurrentHashMap<>();
    private File plantFile;
    private YamlConfiguration plantConfig;
    private Plugin plugin;
    private HarvestMinigameModule minigameService;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
        this.plantFile = new File(plugin.getDataFolder(), "plants.yml");
        if (!plantFile.exists()) {
            try { plantFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.plantConfig = YamlConfiguration.loadConfiguration(plantFile);
        loadPlants();
        
        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(new nl.djorr.mtwwiet.plant.listener.PlantListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlantPhysicsListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlantBreakListener(), plugin);
        
        // Get minigame service
        this.minigameService = PluginContext.getInstance(plugin).getService(HarvestMinigameModule.class).orElse(null);
    }

    @Override
    public void shutdown(Plugin plugin) {
        savePlants();
    }

    @Override
    public String getName() {
        return "PlantService";
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
                } else {
                    // Update hologram naar "klaar om te oogsten"
                    if (data.hologram != null) {
                        data.hologram.clearLines();
                        data.hologram.appendTextLine(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.ready-to-harvest"));
                    } else {
                        // Maak nieuwe hologram 1 blok hoger voor volgroeide planten
                        try {
                            data.hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 2.2, 0.5));
                            data.hologram.appendTextLine(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.ready-to-harvest"));
                        } catch (Exception e) {
                            plugin.getLogger().warning("Could not create ready-to-harvest hologram: " + e.getMessage());
                        }
                    }
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
                    
                    // Stop de groei timer
                    if (data.growthTask != null) {
                        data.growthTask.cancel();
                        data.growthTask = null;
                    }
                    
                    // Stop de countdown timer
                    if (data.countdownTask != null) {
                        data.countdownTask.cancel();
                        data.countdownTask = null;
                    }
                    
                    // Plaats volgroeide plant op de plant locatie en erboven
                    org.bukkit.block.Block plantBlock = loc.getBlock();
                    org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
                    if (topBlock.getType() == org.bukkit.Material.AIR) {
                        plantBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                        plantBlock.setData((byte) 3);
                        topBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                        topBlock.setData((byte) 11);
                    }
                    
                    // Update hologram naar "klaar om te oogsten" 2 blokken hoger
                    if (data.hologram != null) {
                        data.hologram.delete();
                    }
                    try {
                        data.hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 2.2, 0.5));
                        data.hologram.appendTextLine(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.ready-to-harvest"));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not create ready-to-harvest hologram: " + e.getMessage());
                    }
                    
                    // Toon particles dat de plant klaar is
                    loc.getWorld().spigot().playEffect(loc.clone().add(0.5, 1, 0.5), org.bukkit.Effect.HAPPY_VILLAGER, 0, 0, 0.5f, 0.5f, 0.5f, 0.1f, 20, 50);
                    
                    savePlants();
                }
            };
            data.growthTask.runTaskLater(plugin, remaining / 50); // Convert to ticks
        } else {
            data.ready = true;
            
            // Plaats volgroeide plant direct op de plant locatie en erboven
            org.bukkit.block.Block plantBlock = loc.getBlock();
            org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
            if (topBlock.getType() == org.bukkit.Material.AIR) {
                plantBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                plantBlock.setData((byte) 3);
                topBlock.setType(org.bukkit.Material.DOUBLE_PLANT);
                topBlock.setData((byte) 11);
            }
            
            // Update hologram naar "klaar om te oogsten" 2 blokken hoger
            if (data.hologram != null) {
                data.hologram.delete();
            }
            try {
                data.hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 2.2, 0.5));
                data.hologram.appendTextLine(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.ready-to-harvest"));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not create ready-to-harvest hologram: " + e.getMessage());
            }
            
            // Toon particles dat de plant klaar is
            loc.getWorld().spigot().playEffect(loc.clone().add(0.5, 1, 0.5), org.bukkit.Effect.HAPPY_VILLAGER, 0, 0, 0.5f, 0.5f, 0.5f, 0.1f, 20, 50);
            
            savePlants();
        }
        
        // Start countdown timer voor hologram updates
        if (remaining > 1000) { // Alleen als er meer dan 1 seconde over is
            startHologramCountdown(data, loc, remaining);
        }
    }
    
    private void startHologramCountdown(PlantData data, Location loc, long totalRemaining) {
        long secondsRemaining = totalRemaining / 1000;
        long ticksRemaining = totalRemaining / 50;
        
        data.countdownTask = new BukkitRunnable() {
            private long currentSeconds = secondsRemaining;
            
            @Override
            public void run() {
                if (currentSeconds <= 0 || data.hologram == null) {
                    this.cancel();
                    return;
                }
                
                // Update hologram met resterende tijd
                data.hologram.clearLines();
                String message = nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.growing-text");
                message = message.replace("%seconds%", String.valueOf(currentSeconds));
                data.hologram.appendTextLine(message);
                
                currentSeconds--;
            }
        };
        data.countdownTask.runTaskTimer(plugin, 20L, 20L); // Update elke seconde (20 ticks)
    }

    public void addPlant(Location loc, PlantData data) {
        plants.put(loc, data);
        
        // Plaats sapling blok op de plant locatie (blok erboven)
        org.bukkit.block.Block plantBlock = loc.getBlock();
        if (plantBlock.getType() == org.bukkit.Material.AIR) {
            plantBlock.setType(org.bukkit.Material.SAPLING);
            plantBlock.setData((byte) 0); // Oak sapling
        }
        
        // Maak groei hologram boven de plant
        if (data.hologram != null) {
            data.hologram.delete();
        }
        try {
            data.hologram = com.gmail.filoghost.holographicdisplays.api.HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 1.2, 0.5));
            long seconds = data.growTime / 1000;
            String message = nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.growing-text");
            message = message.replace("%seconds%", String.valueOf(seconds));
            data.hologram.appendTextLine(message);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create hologram for plant: " + e.getMessage());
        }
        
        // Start groei timer
        startGrowthTimer(data, loc);
        
        savePlants();
    }

    public void removePlant(Location loc) {
        PlantData data = plants.get(loc);
        if (data != null) {
            // Cleanup timers
            if (data.growthTask != null) {
                data.growthTask.cancel();
                data.growthTask = null;
            }
            if (data.countdownTask != null) {
                data.countdownTask.cancel();
                data.countdownTask = null;
            }
            
            // Cleanup holograms
            if (data.hologram != null) {
                data.hologram.delete();
                data.hologram = null;
            }
            if (data.oogstHologram != null) {
                data.oogstHologram.delete();
                data.oogstHologram = null;
            }
            
            // Cleanup blocks
            org.bukkit.block.Block plantBlock = loc.getBlock();
            org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
            
            if (plantBlock.getType() == org.bukkit.Material.DOUBLE_PLANT || plantBlock.getType() == org.bukkit.Material.SAPLING) {
                plantBlock.setType(org.bukkit.Material.AIR);
            }
            if (topBlock.getType() == org.bukkit.Material.DOUBLE_PLANT) {
                topBlock.setType(org.bukkit.Material.AIR);
            }
            
            // Remove oogstSpeler lock
            data.oogstSpeler = null;
            
            // Remove from data
            plants.remove(loc);
            
            // Force cleanup of the PlantData object
            data.cleanup();
        }
        savePlants();
    }

    public PlantData getPlant(Location loc) {
        return plants.get(loc);
    }

    public boolean isPlantAt(Location loc) {
        return plants.containsKey(loc);
    }

    /**
     * Check of er echt een plant blok staat op de locatie (niet alleen data)
     */
    public boolean hasPlantBlockAt(Location loc) {
        PlantData data = plants.get(loc);
        if (data == null) return false;
        
        // Check of er een plant blok staat
        org.bukkit.block.Block block = loc.getBlock();
        return block.getType() == org.bukkit.Material.SAPLING || 
               block.getType() == org.bukkit.Material.DOUBLE_PLANT;
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
        
        // Check if player is already harvesting this plant
        if (data.oogstSpeler != null && !data.oogstSpeler.equals(player.getUniqueId())) {
            return false;
        }
        
        // Use minigame service to start minigame
        if (minigameService != null) {
            // Create minigame using factory
            IHarvestMinigame minigame =
                nl.djorr.mtwwiet.minigame.factory.HarvestMinigameFactory.create(player, data);
            minigameService.startMinigame(player, data, minigame);
            data.oogstSpeler = player.getUniqueId();
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle minigame end event - cleanup plant and give rewards
     */
    public void onMinigameEnd(MinigameEndEvent event) {
        IHarvestMinigame minigame = event.getMinigame();
        PlantData plantData = minigame.getPlant();
        Player player = minigame.getPlayer();
        boolean success = !minigame.isActive(); // If not active, assume it completed (success/fail determined by strategy)
        
        // Cleanup plant data
        if (plantData != null) {
            // Verwijder plant blokken
            if (plantData.plantBlockLocation != null) {
                org.bukkit.block.Block plantBlock = plantData.plantBlockLocation.getBlock();
                org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
                
                // Verwijder plant blokken
                if (plantBlock.getType() == org.bukkit.Material.DOUBLE_PLANT) {
                    plantBlock.setType(org.bukkit.Material.AIR);
                }
                if (topBlock.getType() == org.bukkit.Material.DOUBLE_PLANT) {
                    topBlock.setType(org.bukkit.Material.AIR);
                }
                
                // Verwijder uit storage
                removePlant(plantData.plantBlockLocation);
            }
            
            // Cleanup alle data
            plantData.cleanup();
        }
        
        // Handle rewards based on success/failure
        if (success) {
            // Give wiet items to player (minigame already shows success message)
            giveWietRewards(player);
        }
        // On failure, plant is destroyed but no rewards given
    }
    
    /**
     * Give wiet rewards to player after successful harvest
     */
    private void giveWietRewards(Player player) {
        // Get CustomItems service
        nl.djorr.mtwwiet.item.CustomItems customItems = PluginContext.getInstance(plugin).getService(nl.djorr.mtwwiet.item.CustomItems.class).orElse(null);
        if (customItems == null) {
            // Fallback to basic item if CustomItems not available
            org.bukkit.inventory.ItemStack wietItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.POTATO_ITEM);
            org.bukkit.inventory.meta.ItemMeta meta = wietItem.getItemMeta();
            meta.setDisplayName("§aWiet");
            wietItem.setItemMeta(meta);
            player.getInventory().addItem(wietItem);
        } else {
            // Use CustomItems to create weed with metadata
            org.bukkit.inventory.ItemStack wietItem = customItems.createWeed();
            player.getInventory().addItem(wietItem);
        }
        
        // Don't show success message here - minigame already shows it
    }
    
    /**
     * Physics protection SellListener.java voor plugin-planten.
     * Voorkomt dat saplings/double plants verdwijnen door physics.
     */
    public static class PlantPhysicsListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
            org.bukkit.block.Block block = event.getBlock();
            org.bukkit.Material type = block.getType();
            if (type == org.bukkit.Material.SAPLING || type == org.bukkit.Material.DOUBLE_PLANT) {
                // Check of dit een plugin-plant is
                PlantModule plantModule = PluginContext.getInstance(org.bukkit.Bukkit.getPluginManager().getPlugin("MTWWiet")).getService(PlantModule.class).orElse(null);
                if (plantModule != null) {
                    if (plantModule.hasPlantBlockAt(block.getLocation()) ||
                        (type == org.bukkit.Material.DOUBLE_PLANT && plantModule.hasPlantBlockAt(block.getLocation().add(0, -1, 0)))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Break protection SellListener.java voor plugin-planten.
     * Voorkomt dat planten kapotgeslagen kunnen worden tenzij ze uit de data zijn verwijderd.
     */
    public static class PlantBreakListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
            org.bukkit.block.Block block = event.getBlock();
            org.bukkit.Material type = block.getType();
            
            PlantModule plantModule = PluginContext.getInstance(org.bukkit.Bukkit.getPluginManager().getPlugin("MTWWiet")).getService(PlantModule.class).orElse(null);
            if (plantModule == null) return;
            
            // Check of dit een plugin-plant is (onderste blok)
            if (plantModule.hasPlantBlockAt(block.getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.cannot-break"));
                return;
            }
            
            // Check of dit een plugin-plant is (bovenste blok van double plant)
            if (type == org.bukkit.Material.DOUBLE_PLANT && plantModule.hasPlantBlockAt(block.getLocation().add(0, -1, 0))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(nl.djorr.mtwwiet.util.MessageUtil.getMessage("planting.cannot-break"));
                return;
            }
        }
    }
} 