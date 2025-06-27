package nl.yourname.weedplugin.manager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import nl.yourname.weedplugin.WeedPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import nl.yourname.weedplugin.model.PlantData;

/**
 * Utility voor het plaatsen van wietplanten.
 */
public class PlantManagerUtil {
    // Simpele implementatie: plant op het block waar de speler op staat
    public static boolean tryPlantWeed(Player player) {
        Block block = player.getTargetBlock((java.util.Set<Material>) null, 5);
        if (block == null || block.getType() != Material.GRASS) {
            player.sendMessage("§cJe kunt alleen op gras planten!");
            return false;
        }
        // Check of speler een custom seed met naam 'Wietzaadje' in de hand heeft
        org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.SEEDS || hand.getItemMeta() == null || !"§aWietzaadje".equals(hand.getItemMeta().getDisplayName())) {
            player.sendMessage("§cJe moet een wietzaadje in je hand hebben om te planten!");
            return false;
        }
        // Verwijder 1 zaadje uit de hand
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        // Locatie waar de plant komt: 1 blok boven grass
        Location plantLoc = block.getLocation().add(0, 1, 0);
        PlantManager manager = PlantManager.getInstance();
        if (manager.isPlantAt(plantLoc)) {
            player.sendMessage("§cHier staat al een plant!");
            return false;
        }
        PlantData data = new PlantData();
        data.owner = player.getUniqueId();
        data.plantedAt = System.currentTimeMillis();
        data.growTime = 10;
        data.ready = false;
        data.plantBlockLocation = plantLoc.clone();
        manager.addPlant(plantLoc, data);
        // Plaats sapling-blok (ID 6)
        Block plantBlock = plantLoc.getBlock();
        plantBlock.setType(Material.SAPLING);
        plantBlock.setData((byte) 0);
        // Hologram tijdens groei
        Hologram[] hologramRef = new Hologram[1];
        hologramRef[0] = HologramsAPI.createHologram(WeedPlugin.getPlugin(WeedPlugin.class), plantLoc.clone().add(0.5, 1.7, 0.5));
        hologramRef[0].appendTextLine("§aWiet groeit... 10s");
        data.hologram = hologramRef[0];
        // Groei-timer
        data.growthTask = new BukkitRunnable() {
            int secondsLeft = 10;
            @Override
            public void run() {
                plantBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, plantBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.5, 0.3, 0.05);
                if (secondsLeft <= 0) {
                    data.ready = true;
                    // Controleer of er ruimte is voor een double plant
                    Block topBlock = plantBlock.getRelative(0, 1, 0);
                    if (topBlock.getType() == Material.AIR) {
                        // Plaats onderste helft (data 3 = LARGE_FERN, bottom)
                        plantBlock.setType(Material.DOUBLE_PLANT);
                        plantBlock.setData((byte) 3);
                        // Plaats bovenste helft (data 11 = LARGE_FERN, top)
                        topBlock.setType(Material.DOUBLE_PLANT);
                        topBlock.setData((byte) 11);
                    } else {
                        // Geen ruimte, laat de sapling staan en markeer als klaar
                        plantBlock.setType(Material.SAPLING);
                        plantBlock.setData((byte) 0);
                        player.sendMessage("§cEr is te weinig ruimte boven de plant om te groeien!");
                    }
                    // Update bestaande hologram naar oogstbaar
                    if (hologramRef[0] != null) {
                        // Verwijder oude groei-hologram
                        hologramRef[0].delete();
                        // Maak nieuwe hologram op y+2.7
                        hologramRef[0] = HologramsAPI.createHologram(WeedPlugin.getPlugin(WeedPlugin.class), plantLoc.clone().add(0.5, 2.7, 0.5));
                        hologramRef[0].appendTextLine("§aKlaar om te oogsten!");
                        data.hologram = hologramRef[0];
                    }
                    this.cancel();
                    return;
                }
                // Check of er regels zijn voordat we proberen te verwijderen
                if (hologramRef[0].size() > 0) {
                    hologramRef[0].removeLine(0);
                }
                hologramRef[0].appendTextLine("§aWiet groeit... " + secondsLeft + "s");
                secondsLeft--;
            }
        };
        data.growthTask.runTaskTimer(WeedPlugin.getPlugin(WeedPlugin.class), 0L, 20L);
        return true;
    }

    public static void startGrowthCycle(Block block, PlantData data, Player player) {
        // Spawn Hologram boven het blok
        Hologram hologram = HologramsAPI.createHologram(WeedPlugin.getPlugin(WeedPlugin.class), block.getLocation().clone().add(0.5, 1.5, 0.5));
        hologram.appendTextLine("§aWiet groeit... 10s");
        data.hologram = hologram;
        data.ready = false;
        // Start groei-timer
        new BukkitRunnable() {
            int secondsLeft = 10;
            @Override
            public void run() {
                // Toon elke seconde groene villager particles
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1, 0.5), 10, 0.3, 0.5, 0.3, 0.05);
                if (secondsLeft <= 0) {
                    data.ready = true;
                    block.setType(Material.LONG_GRASS);
                    block.setData((byte) 1);
                    if (hologram != null) hologram.delete();
                    this.cancel();
                    return;
                }
                hologram.clearLines();
                hologram.appendTextLine("§aWiet groeit... " + secondsLeft + "s");
                secondsLeft--;
            }
        }.runTaskTimer(WeedPlugin.getPlugin(WeedPlugin.class), 0L, 20L);
    }

    // --- Voeg deze class toe om saplings/double plants te beschermen tegen physics ---

    // In 1.12.2 kunnen saplings/double plants verdwijnen door physics. We voorkomen dat voor plugin-planten.
    public static class PlantPhysicsListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
            org.bukkit.block.Block block = event.getBlock();
            org.bukkit.Material type = block.getType();
            if (type == Material.SAPLING || type == Material.DOUBLE_PLANT) {
                // Check of dit een plugin-plant is
                if (nl.yourname.weedplugin.manager.PlantManager.getInstance().isPlantAt(block.getLocation()) ||
                    (type == Material.DOUBLE_PLANT && nl.yourname.weedplugin.manager.PlantManager.getInstance().isPlantAt(block.getLocation().add(0, -1, 0)))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // --- Voeg deze class toe om planten te beschermen tegen kapotslaan ---

    // Voorkom dat planten kapotgeslagen kunnen worden tenzij ze uit de data zijn verwijderd
    public static class PlantBreakListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
            org.bukkit.block.Block block = event.getBlock();
            org.bukkit.Material type = block.getType();
            
            // Check of dit een plugin-plant is (onderste blok)
            if (nl.yourname.weedplugin.manager.PlantManager.getInstance().isPlantAt(block.getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cJe kunt deze plant niet kapotslaan! Oogst hem via de minigame.");
                return;
            }
            
            // Check of dit een plugin-plant is (bovenste blok van double plant)
            if (type == Material.DOUBLE_PLANT && nl.yourname.weedplugin.manager.PlantManager.getInstance().isPlantAt(block.getLocation().add(0, -1, 0))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cJe kunt deze plant niet kapotslaan! Oogst hem via de minigame.");
                return;
            }
        }
    }
} 