package nl.djorr.mtwwiet.minigame.model;

import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.plant.PlantModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import nl.djorr.mtwwiet.MTWWiet;
import nl.djorr.mtwwiet.plant.model.PlantData;

import java.util.HashSet;
import java.util.Set;

/**
 * Oogst-minigame: klik op alle groene wool binnen de tijd.
 */
public class HarvestMinigame implements IHarvestMinigame {
    private static final int SIZE = 27;
    private static final int GREEN_COUNT = 5;
    private static final int RED_COUNT = 4;

    private final Player player;
    private final Inventory inv;
    private final Set<Integer> greenSlots = new HashSet<>();
    private final long startTime;
    private final long timeLimitMillis;
    private BukkitRunnable timerTask;
    private final PlantData plant;
    private final org.bukkit.Location plantBlockLoc;
    private boolean finished = false;
    private boolean active = true;

    public HarvestMinigame(Player player, PlantData plant) {
        this.player = player;
        this.plant = plant;
        this.inv = Bukkit.createInventory(null, SIZE, MessageUtil.getMessage("harvesting.inventory-title"));
        
        // Haal time limit op uit config (standaard 15 seconden)
        long timeLimitSeconds = 15; // Default
        try {
            nl.djorr.mtwwiet.config.ConfigManager configManager = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(nl.djorr.mtwwiet.config.ConfigManager.class).orElse(null);
            if (configManager != null) {
                timeLimitSeconds = configManager.getConfig().getLong("oogst-minigame.time-limit-seconds", 15);
            }
        } catch (Exception e) {
            // Gebruik default als config niet beschikbaar is
        }

        this.timeLimitMillis = timeLimitSeconds * 1000L; // Convert to milliseconds
        this.startTime = System.currentTimeMillis();
        
        if (plant.stand != null) {
            this.plantBlockLoc = plant.stand.getLocation();
        } else if (plant.hologram != null) {
            this.plantBlockLoc = plant.hologram.getLocation().clone().subtract(0.5, 1, 0.5).getBlock().getLocation();
        } else {
            this.plantBlockLoc = player.getLocation(); // fallback
        }
        setupInventory();
    }

    private void setupInventory() {
        java.util.List<Integer> slots = new java.util.ArrayList<>();
        for (int i = 0; i < SIZE; i++) slots.add(i);
        java.util.Collections.shuffle(slots);
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, createGlass((byte)14, MessageUtil.getMessage("harvesting.wrong-block")));
        }
        for (int i = 0; i < GREEN_COUNT; i++) {
            int slot = slots.remove(0);
            inv.setItem(slot, createGlass((byte)5, MessageUtil.getMessage("harvesting.correct-block")));
            greenSlots.add(slot);
        }

        open();
    }

    private ItemStack createGlass(byte color, String name) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(name);
        glass.setItemMeta(meta);
        return glass;
    }

    private void open() {
        player.openInventory(inv);
        startTimer();
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished) {
                    cancel();
                    return;
                }
                
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - startTime;

                if (elapsed >= timeLimitMillis) {
                    fail();
                    cancel();
                    return;
                }
            }
        };
        timerTask.runTaskTimer(MTWWiet.getPlugin(MTWWiet.class), 0L, 20L); // Check every second
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        // Altijd cancellen, geen item movement mogelijk
        event.setCancelled(true);
        
        // Alleen clicks in het minigame-inventory zelf verwerken
        if (!event.getInventory().equals(inv) || finished) return;
        if (event.getClickedInventory() != event.getInventory()) return;
        
        int slot = event.getRawSlot();
        
        // Check of de slot binnen de geldige range is
        if (slot < 0 || slot >= SIZE) return;
        
        // Check of er een item in de slot staat
        if (inv.getItem(slot) == null) return;
        
        if (greenSlots.contains(slot)) {
            // Correcte groene slot geklikt
            inv.setItem(slot, null);
            greenSlots.remove(slot);
            if (greenSlots.isEmpty()) {
                success();
            }
        } else {
            // Verkeerde slot geklikt - faal
            fail();
        }
    }

    private void success() {
        if (finished) return;
        finished = true;
        active = false;
        
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        
        // Stop minigame in module with success status
        nl.djorr.mtwwiet.minigame.HarvestMinigameModule minigameModule = 
            PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(nl.djorr.mtwwiet.minigame.HarvestMinigameModule.class).orElse(null);
        if (minigameModule != null) {
            minigameModule.endMinigame(player, true);
        }
        
        // Close inventory first to prevent inventory close event from triggering failure
        player.closeInventory();
        
        // Small delay to ensure inventory close event is processed
        MTWWiet.getPlugin(MTWWiet.class).getServer().getScheduler().runTaskLater(MTWWiet.getPlugin(MTWWiet.class), () -> {
            player.sendMessage(MessageUtil.getMessage("harvesting.success"));
            
            // Geef echt custom weed item met NBT
            nl.djorr.mtwwiet.item.CustomItems customItems = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(nl.djorr.mtwwiet.item.CustomItems.class).orElse(null);
            if (customItems != null) {
                player.getInventory().addItem(customItems.createWeed());
            }
            
            // Cleanup plant volledig (inclusief blokken) bij success
            cleanupPlant();
        }, 2L); // 2 ticks delay
    }

    private void fail() {
        if (finished) return;
        finished = true;
        active = false;
        
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        
        // Stop minigame in module with failure status
        nl.djorr.mtwwiet.minigame.HarvestMinigameModule minigameModule = 
            PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(nl.djorr.mtwwiet.minigame.HarvestMinigameModule.class).orElse(null);
        if (minigameModule != null) {
            minigameModule.endMinigame(player, false);
        }
        
        // Close inventory first to prevent inventory close event from triggering failure
        player.closeInventory();
        
        // Small delay to ensure inventory close event is processed
        MTWWiet.getPlugin(MTWWiet.class).getServer().getScheduler().runTaskLater(MTWWiet.getPlugin(MTWWiet.class), () -> {
            player.sendMessage(MessageUtil.getMessage("harvesting.failed"));
            
            // Geef geen schade (0 damage)
            player.damage(0.0);
            
            // Cleanup plant volledig (inclusief blokken) bij failure
            cleanupPlant();
        }, 2L); // 2 ticks delay
    }
    
    private void cleanupPlant() {
        // Cleanup holograms
        if (plant.hologram != null) {
            plant.hologram.delete();
            plant.hologram = null;
        }
        if (plant.oogstHologram != null) {
            plant.oogstHologram.delete();
            plant.oogstHologram = null;
        }
        
        // Cleanup timers
        if (plant.growthTask != null) {
            plant.growthTask.cancel();
            plant.growthTask = null;
        }
        if (plant.countdownTask != null) {
            plant.countdownTask.cancel();
            plant.countdownTask = null;
        }
        
        // Verwijder plant blokken
        if (plant.plantBlockLocation != null) {
            org.bukkit.block.Block plantBlock = plant.plantBlockLocation.getBlock();
            org.bukkit.block.Block topBlock = plantBlock.getRelative(0, 1, 0);
            
            // Block break animatie
            plantBlock.getWorld().playEffect(plantBlock.getLocation(), org.bukkit.Effect.STEP_SOUND, plantBlock.getType());
            
            // Verwijder altijd beide blokken (onderste en bovenste), ongeacht type
            plantBlock.setType(org.bukkit.Material.AIR);
            topBlock.setType(org.bukkit.Material.AIR);
            
            // Verwijder uit PlantModule
            PlantModule plantModule = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(PlantModule.class).orElse(null);
            if (plantModule != null) {
                plantModule.removePlant(plant.plantBlockLocation);
            }
        }
        
        // Verwijder oogstSpeler lock
        plant.oogstSpeler = null;
    }

    @Override
    public void forceFail() {
        if (!finished) {
            finished = true;
            active = false;
            
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            
            // Stop minigame in module with failure status
            nl.djorr.mtwwiet.minigame.HarvestMinigameModule minigameModule = 
                PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(nl.djorr.mtwwiet.minigame.HarvestMinigameModule.class).orElse(null);
            if (minigameModule != null) {
                minigameModule.endMinigame(player, false);
            }
            
            fail();
        }
    }

    @Override
    public void forceCleanup() {
        if (timerTask != null) timerTask.cancel();
        if (plant.hologram != null) plant.hologram.delete();
        if (plant.growthTask != null) plant.growthTask.cancel();
        if (plant.oogstHologram != null) plant.oogstHologram.delete();
    }

    @Override
    public boolean isActive() {
        return active && !finished;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public PlantData getPlant() {
        return plant;
    }
} 