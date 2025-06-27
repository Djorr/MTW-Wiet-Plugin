package nl.djorr.mtwwiet.listener;

import nl.djorr.mtwwiet.MTWWiet;
import nl.djorr.mtwwiet.manager.NPCManager;
import nl.djorr.mtwwiet.manager.PlantManagerUtil;
import nl.djorr.mtwwiet.util.MessageUtil;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import nl.djorr.mtwwiet.manager.PlantManager;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import nl.djorr.mtwwiet.model.PlantData;

/**
 * Luistert naar interacties met wietplanten (plaatsen, oogsten, minigame).
 */
public class PlantListener implements Listener {
    /**
     * Wordt aangeroepen als een speler op een ArmorStand klikt.
     */
    @EventHandler
    public void onPlantInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();
        
        // Check permissie voor oogsten
        if (!player.hasPermission("weedplugin.oogsten") && !player.isOp()) {
            return;
        }
        
        // Zoek bijbehorende plant
        PlantData data = null;
        for (PlantData pd : PlantManager.getInstance().getAllPlants()) {
            if (pd.stand != null && pd.stand.getUniqueId().equals(stand.getUniqueId())) {
                data = pd;
                break;
            }
        }
        if (data == null) return;
        event.setCancelled(true);
        if (!data.ready) {
            player.sendMessage(MessageUtil.getMessage("planting.not-ready"));
            return;
        }
        // Start minigame via PlantManager
        PlantManager.getInstance().startMinigameAt(stand.getLocation(), player);
    }

    @EventHandler
    public void onSeedOrPlantBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
        
        // Eerst: planten op GRASS
        if (block.getType() == Material.GRASS && hand != null && hand.getType() == Material.SEEDS && hand.getItemMeta() != null && "§aWietzaadje".equals(hand.getItemMeta().getDisplayName())) {
            // Check permissie voor planten
            if (!player.hasPermission("weedplugin.plant") && !player.isOp()) {
                player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                return;
            }
            
            // Check of speler te dicht bij een dealer is
            if (isNearDealer(block.getLocation())) {
                player.sendMessage(MessageUtil.getMessage("planting.too-close-to-dealer"));
                return;
            }
            
            boolean success = PlantManagerUtil.tryPlantWeed(player);
            if (success) event.setCancelled(true);
            return;
        }
        
        // Daarna: oogsten/minigame starten op DOUBLE_PLANT
        PlantData data = PlantManager.getInstance().getPlant(block.getLocation());
        if (data == null || !data.ready) return;
        if (block.getType() == Material.DOUBLE_PLANT && block.getData() == (byte) 3) {
            // Check permissie voor oogsten
            if (!player.hasPermission("weedplugin.oogsten") && !player.isOp()) {
                player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                return;
            }
            
            if (data.oogstSpeler != null && !data.oogstSpeler.equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(MessageUtil.getMessage("planting.already-harvesting"));
                return;
            }
            event.setCancelled(true);
            // Zet oogst-hologram
            if (data.hologram != null) { data.hologram.delete(); data.hologram = null; }
            if (data.oogstHologram != null) data.oogstHologram.delete();
            data.oogstHologram = HologramsAPI.createHologram(MTWWiet.getPlugin(MTWWiet.class), block.getLocation().clone().add(0.5, 1.7, 0.5));
            data.oogstHologram.appendTextLine(MessageUtil.getMessage("harvesting.harvesting-text"));
            PlantManager.getInstance().startMinigameAt(block.getLocation(), player);
        }
    }
    
    /**
     * Controleert of een locatie te dicht bij een dealer NPC is.
     */
    private boolean isNearDealer(org.bukkit.Location location) {
        int dealerDistance = MTWWiet.getPlugin(MTWWiet.class).getConfig().getInt("planting.dealer-distance", 10);
        
        for (net.citizensnpcs.api.npc.NPC npc : NPCManager.getInstance().getAllWeedNPCs().values()) {
            if (npc.getStoredLocation() != null && npc.getStoredLocation().distance(location) <= dealerDistance) {
                return true;
            }
        }
        return false;
    }
} 