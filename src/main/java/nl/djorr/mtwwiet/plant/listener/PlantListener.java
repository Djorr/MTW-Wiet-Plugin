package nl.djorr.mtwwiet.plant.listener;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.npc.NPCModule;
import nl.djorr.mtwwiet.plant.model.PlantData;
import nl.djorr.mtwwiet.plant.PlantModule;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.item.CustomItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import nl.djorr.mtwwiet.plant.factory.PlantFactory;
import nl.djorr.mtwwiet.config.ConfigManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;

/**
 * Luistert naar plant-gerelateerde events (plaatsen, groei, oogsten).
 */
public class PlantListener implements Listener {
    private final Plugin plugin;
    private final CustomItems customItems;
    private final PlantModule plantModule;

    public PlantListener(Plugin plugin) {
        this.plugin = plugin;
        this.customItems = PluginContext.getInstance(plugin).getService(CustomItems.class).orElse(null);
        this.plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
    }

    @EventHandler
    public void onPlantInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();
        if (!player.hasPermission("weedplugin.oogsten") && !player.isOp()) return;
        PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
        if (plantModule == null) return;
        PlantData data = null;
        for (PlantData pd : plantModule.getAllPlants()) {
            if (pd.stand != null && pd.stand.getUniqueId().equals(stand.getUniqueId())) {
                data = pd;
                break;
            }
        }
        if (data == null) return;
        event.setCancelled(true);
        if (!data.ready) {
            event.getPlayer().sendMessage(MessageUtil.getMessage("planting.not-ready"));
            return;
        }
        plantModule.startMinigameAt(stand.getLocation(), player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block clickedBlock = event.getClickedBlock();
        
        // Check of speler een wietzaadje gebruikt
        if (item != null && customItems != null) {
            if (!customItems.isWeedSeed(item)) {
                // Speler probeert te planten met een niet-legitiem zaadje
                if (item.getType() == Material.SEEDS) {
                    player.sendMessage(MessageUtil.getMessage("planting.invalid-seed"));
                }
                return;
            }
            // Alleen als het een echt custom zaadje is, verder gaan
            event.setCancelled(true);
            
            // Check of speler permissie heeft
            if (!player.hasPermission("weedplugin.plant")) {
                player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                return;
            }
            
            // Check of speler op een blok klikt
            if (clickedBlock == null) {
                player.sendMessage(MessageUtil.getMessage("planting.cannot-plant"));
                return;
            }
            
            // Check of het een gras blok is
            if (clickedBlock.getType() != Material.GRASS) {
                player.sendMessage(MessageUtil.getMessage("planting.grass-only"));
                return;
            }
            
            // Check of er al een plant staat
            Block plantBlock = clickedBlock.getRelative(0, 1, 0);
            if (plantBlock.getType() != Material.AIR) {
                player.sendMessage(MessageUtil.getMessage("planting.already-planted"));
                return;
            }
            
            // Check of er een dealer in de buurt staat
            if (isNearDealer(plantBlock.getLocation())) {
                player.sendMessage(MessageUtil.getMessage("planting.too-close-to-dealer"));
                return;
            }
            
            // Plant de wiet
            if (plantModule != null) {
                PlantData plantData = new PlantData();
                plantData.owner = player.getUniqueId();
                plantData.plantedAt = System.currentTimeMillis();
                plantData.growTime = plugin.getConfig().getLong("plant.grow-time-seconds", 3) * 1000;
                plantData.ready = false;
                plantData.plantBlockLocation = plantBlock.getLocation();
                
                plantModule.addPlant(plantBlock.getLocation(), plantData);
                
                // Verwijder 1 zaadje uit inventory
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
                
                player.sendMessage(MessageUtil.getMessage("planting.success"));
            }
        }
        
        // Check voor oogsten van volgroeide planten
        if (clickedBlock != null && plantModule != null) {
            PlantData data = plantModule.getPlant(clickedBlock.getLocation());
            if (data != null && data.ready && clickedBlock.getType() == Material.DOUBLE_PLANT && clickedBlock.getData() == (byte) 3) {
                // Cancel event IMMEDIATELY to prevent plant from disappearing
                event.setCancelled(true);

                if (!player.hasPermission("weedplugin.oogsten") && !player.isOp()) {
                    player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    return;
                }
                
                // Check of speler iets in handen heeft - alleen lege handen kunnen oogsten
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                    // Speler heeft iets in handen - kan niet oogsten
                    player.sendMessage(MessageUtil.getMessage("planting.cannot-harvest-with-item"));
                    return;
                }
                
                if (data.oogstSpeler != null && !data.oogstSpeler.equals(player.getUniqueId())) {
                    player.sendMessage(MessageUtil.getMessage("planting.already-harvesting"));
                    return;
                }
                
                // Cleanup bestaande holograms
                if (data.hologram != null) { 
                    data.hologram.delete(); 
                    data.hologram = null; 
                }
                
                // Update oogst hologram naar "bezig met oogsten" in plaats van verwijderen
                if (data.oogstHologram != null) {
                    data.oogstHologram.delete();
                }
                data.oogstHologram = HologramsAPI.createHologram(plugin, clickedBlock.getLocation().clone().add(0.5, 3.2, 0.5));
                data.oogstHologram.appendTextLine(MessageUtil.getMessage("harvesting.harvesting-text"));
                
                // Start minigame
                plantModule.startMinigameAt(clickedBlock.getLocation(), player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        // Check of dit een oogst minigame inventory was
        PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
        if (plantModule == null) return;
        
        // Zoek naar plant data voor deze speler
        for (PlantData data : plantModule.getAllPlants()) {
            if (data.oogstSpeler != null && data.oogstSpeler.equals(player.getUniqueId())) {
                // Check of dit de minigame inventory was die werd gesloten
                // We moeten controleren of de speler nog steeds in een minigame zit
                nl.djorr.mtwwiet.minigame.HarvestMinigameModule minigameModule = 
                    PluginContext.getInstance(plugin).getService(nl.djorr.mtwwiet.minigame.HarvestMinigameModule.class).orElse(null);
                
                if (minigameModule != null) {
                    nl.djorr.mtwwiet.minigame.model.IHarvestMinigame activeMinigame = minigameModule.getActiveMinigame(player);
                    
                    // Als er nog steeds een actieve minigame is, dan heeft de speler de inventory gesloten
                    if (activeMinigame != null && activeMinigame.isActive()) {
                        // Speler heeft inventory gesloten tijdens oogsten - oogst mislukt
                        // Laat de minigame zelf de failure afhandelen
                        activeMinigame.forceFail();
                        break;
                    }
                }
            }
        }
    }

    // Bescherm planten tegen block physics en decay
    @EventHandler
    public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.DOUBLE_PLANT) {
            // Check of dit een wietplant is
            PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
            if (plantModule != null) {
                PlantData data = plantModule.getPlant(block.getLocation());
                if (data != null) {
                    // Dit is een wietplant - cancel physics
                    event.setCancelled(true);
                }
            }
        }
    }
    
    // Bescherm planten tegen block break
    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.DOUBLE_PLANT) {
            // Check of dit een wietplant is
            PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
            if (plantModule != null) {
                PlantData data = plantModule.getPlant(block.getLocation());
                if (data != null) {
                    // Dit is een wietplant - alleen eigenaar kan breken
                    Player player = event.getPlayer();
                    if (!data.owner.equals(player.getUniqueId()) && !player.hasPermission("weedplugin.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(MessageUtil.getMessage("planting.cannot-break-others"));
                        return;
                    }
                    
                    // Eigenaar breekt plant - cleanup
                    plantModule.removePlant(block.getLocation());
                }
            }
        }
        
        // Check ook het blok eronder (basis van de plant)
        Block blockBelow = block.getRelative(0, -1, 0);
        if (blockBelow.getType() == Material.DOUBLE_PLANT) {
            PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
            if (plantModule != null) {
                PlantData data = plantModule.getPlant(blockBelow.getLocation());
                if (data != null) {
                    // Dit is een wietplant - alleen eigenaar kan breken
                    Player player = event.getPlayer();
                    if (!data.owner.equals(player.getUniqueId()) && !player.hasPermission("weedplugin.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(MessageUtil.getMessage("planting.cannot-break-others"));
                        return;
                    }
                    
                    // Eigenaar breekt plant - cleanup
                    plantModule.removePlant(blockBelow.getLocation());
                }
            }
        }
    }
    
    // Bescherm planten tegen explosions
    @EventHandler
    public void onBlockExplode(org.bukkit.event.block.BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (block.getType() == Material.DOUBLE_PLANT) {
                PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
                if (plantModule != null) {
                    PlantData data = plantModule.getPlant(block.getLocation());
                    return data != null; // Remove if it's a weed plant
                }
            }
            return false;
        });
    }
    
    // Bescherm planten tegen entity explosions
    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (block.getType() == Material.DOUBLE_PLANT) {
                PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
                if (plantModule != null) {
                    PlantData data = plantModule.getPlant(block.getLocation());
                    return data != null; // Remove if it's a weed plant
                }
            }
            return false;
        });
    }
    
    // Bescherm planten tegen pistons
    @EventHandler
    public void onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.DOUBLE_PLANT) {
                PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
                if (plantModule != null) {
                    PlantData data = plantModule.getPlant(block.getLocation());
                    if (data != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.DOUBLE_PLANT) {
                PlantModule plantModule = PluginContext.getInstance(plugin).getService(PlantModule.class).orElse(null);
                if (plantModule != null) {
                    PlantData data = plantModule.getPlant(block.getLocation());
                    if (data != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private boolean isNearDealer(org.bukkit.Location location) {
        int dealerDistance = plugin.getConfig().getInt("planting.dealer-distance", 10);
        NPCModule npcModule = PluginContext.getInstance(plugin).getService(NPCModule.class).orElse(null);
        if (npcModule == null) return false;
        
        for (net.citizensnpcs.api.npc.NPC npc : npcModule.getAllWeedNPCs().values()) {
            if (npc.getStoredLocation() != null && npc.getStoredLocation().distance(location) <= dealerDistance) {
                return true;
            }
        }
        return false;
    }

    private String getWeedSeedNameFromConfig() {
        ConfigManager configManager = PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            return configManager.getConfig().getString("custom-items.weed-seed.name", "§aWietzaadje");
        }
        return "§aWietzaadje";
    }
} 