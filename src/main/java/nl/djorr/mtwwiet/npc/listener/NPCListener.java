package nl.djorr.mtwwiet.npc.listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.npc.NPCModule;
import nl.djorr.mtwwiet.shop.ShopModule;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.MTWWiet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

/**
 * Handelt interacties met wiet NPCs en dealer conversaties af.
 */
public class NPCListener implements Listener {
    private final Plugin plugin;
    private final NPCModule npcModule;
    
    public NPCListener(Plugin plugin) {
        this.plugin = plugin;
        this.npcModule = PluginContext.getInstance(plugin).getService(NPCModule.class).orElse(null);
    }
    
    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        
        // Check permissie voor NPC shop gebruik
        if (!player.hasPermission("weedplugin.shop") && !player.isOp()) {
            player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        // Check of dit een wiet NPC is
        if (npcModule != null && npcModule.isWeedNPC(event.getNPC().getUniqueId())) {
            // Start conversatie in plaats van shop menu
            npcModule.startConversation(player, event.getNPC().getEntity());
        }
    }
    
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        // Check of dit een dealer NPC is
        if ("Dealer".equalsIgnoreCase(entity.getCustomName())) {
            
            event.setCancelled(true);
            
            // Start conversatie
            if (npcModule != null) {
                npcModule.startConversation(player, entity);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Check of speler in conversatie is
        if (npcModule != null && npcModule.isInConversation(player)) {
            // Cancel de chat en verwerk conversatie op main thread
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                npcModule.handlePlayerChat(player, message);
            });
        }
    }
} 