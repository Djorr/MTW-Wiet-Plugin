package nl.yourname.weedplugin.listener;

import nl.yourname.weedplugin.item.CustomItems;
import nl.yourname.weedplugin.util.VaultUtil;
import nl.yourname.weedplugin.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Handelt aankopen in de Growshop GUI af.
 */
public class ShopListener implements Listener {
    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle() == null || !event.getView().getTitle().contains("Growshop")) return;
        
        // Check permissie voor shop gebruik
        if (!player.hasPermission("weedplugin.shop") && !player.isOp()) {
            player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        Plugin plugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
        int prijs = 0;
        ItemStack toGive = null;
        
        if (clicked.getType() == Material.SEEDS && "§aWietzaadje".equals(clicked.getItemMeta().getDisplayName())) {
            prijs = plugin.getConfig().getInt("shop.zaad");
            toGive = new ItemStack(Material.SEEDS);
            org.bukkit.inventory.meta.ItemMeta meta = toGive.getItemMeta();
            meta.setDisplayName("§aWietzaadje");
            toGive.setItemMeta(meta);
        } else if (clicked.getType() == Material.PAPER && "§fZakje".equals(clicked.getItemMeta().getDisplayName())) {
            prijs = plugin.getConfig().getInt("shop.zakje");
            toGive = CustomItems.getZakje();
        }
        
        if (toGive != null && prijs > 0) {
            double saldo = VaultUtil.getEconomy().getBalance(player);
            if (saldo < prijs) {
                player.sendMessage(MessageUtil.getMessage("shop.not-enough-money"));
                return;
            }
            VaultUtil.getEconomy().withdrawPlayer(player, prijs);
            player.getInventory().addItem(toGive);
            player.sendMessage(MessageUtil.getMessage("shop.purchase-success"));
        }
    }
} 