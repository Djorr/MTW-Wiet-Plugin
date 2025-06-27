package nl.yourname.weedplugin.listener;

import nl.yourname.weedplugin.item.CustomItems;
import nl.yourname.weedplugin.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Regelt het vullen van zakjes met wiet in een Dropper.
 */
public class BagListener implements Listener {
    @EventHandler
    public void onDropperInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.DROPPER) return;
        Player player = event.getPlayer();
        player.openInventory(((org.bukkit.block.Dropper) block.getState()).getInventory());
    }

    @EventHandler
    public void onDropperCraft(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof org.bukkit.block.Dropper)) return;
        // Check of er een Wiet Top en een Zakje in zit
        boolean hasTop = false, hasZakje = false;
        int topSlot = -1, zakjeSlot = -1;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            if (item.isSimilar(CustomItems.getWietTop(1))) { hasTop = true; topSlot = i; }
            if (item.isSimilar(CustomItems.getZakje())) { hasZakje = true; zakjeSlot = i; }
        }
        if (hasTop && hasZakje) {
            inv.setItem(topSlot, null);
            inv.setItem(zakjeSlot, null);
            inv.addItem(CustomItems.getGevuldeWietzak());
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(MessageUtil.getMessage("bag.created"));
        }
    }
} 