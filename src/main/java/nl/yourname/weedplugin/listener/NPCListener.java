package nl.yourname.weedplugin.listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import nl.yourname.weedplugin.manager.NPCManager;
import nl.yourname.weedplugin.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handelt interacties met wiet NPCs af.
 */
public class NPCListener implements Listener {
    
    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        
        // Check permissie voor NPC shop gebruik
        if (!player.hasPermission("weedplugin.shop") && !player.isOp()) {
            player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        // Check of dit een wiet NPC is
        if (NPCManager.getInstance().isWeedNPC(event.getNPC().getUniqueId())) {
            // Open de shop GUI
            NPCManager.getInstance().openShop(player);
        }
    }
} 