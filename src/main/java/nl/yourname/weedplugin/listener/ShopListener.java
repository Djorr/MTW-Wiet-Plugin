package nl.yourname.weedplugin.listener;

import nl.yourname.weedplugin.item.CustomItems;
import nl.yourname.weedplugin.util.VaultUtil;
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
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        Plugin plugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
        int prijs = 0;
        ItemStack toGive = null;
        if (clicked.getType() == Material.MELON_SEEDS) {
            prijs = plugin.getConfig().getInt("shop.zaad");
            toGive = new ItemStack(Material.MELON_SEEDS);
        } else if (clicked.getType() == Material.PAPER) {
            prijs = plugin.getConfig().getInt("shop.zakje");
            toGive = CustomItems.getZakje();
        } else if (clicked.getType() == Material.BONE) {
            prijs = plugin.getConfig().getInt("shop.upgrade");
            toGive = new ItemStack(Material.BONE);
        }
        if (toGive != null && prijs > 0) {
            double saldo = VaultUtil.getEconomy().getBalance(player);
            if (saldo < prijs) {
                player.sendMessage("§cJe hebt niet genoeg geld!");
                return;
            }
            VaultUtil.getEconomy().withdrawPlayer(player, prijs);
            player.getInventory().addItem(toGive);
            player.sendMessage("§aAankoop gelukt!");
        }
    }
} 