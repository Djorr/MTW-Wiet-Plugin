package nl.djorr.mtwwiet.listener;

import nl.djorr.mtwwiet.item.CustomItems;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Beheert de winkel GUI voor het kopen van wietzaadjes.
 */
public class ShopListener implements Listener {
    
    private static final String SHOP_TITLE = "§aWietwinkel";
    private static final int Wietzaadje_PRICE = 50;
    
    public static void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, SHOP_TITLE);
        
        // Wietzaadje
        ItemStack wietzaadje = CustomItems.getWietzaadje();
        shop.setItem(13, wietzaadje);
        
        // Prijs bord
        ItemStack priceSign = new ItemStack(Material.SIGN);
        org.bukkit.inventory.meta.ItemMeta meta = priceSign.getItemMeta();
        meta.setDisplayName("§ePrijs: §a" + Wietzaadje_PRICE + " euro");
        priceSign.setItemMeta(meta);
        shop.setItem(22, priceSign);
        
        player.openInventory(shop);
    }
    
    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SHOP_TITLE)) return;
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Check permissie
        if (!player.hasPermission("weedplugin.winkel") && !player.isOp()) {
            player.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        
        if (clicked.isSimilar(CustomItems.getWietzaadje())) {
            // Koop wietzaadje
            if (VaultUtil.getEconomy().has(player, Wietzaadje_PRICE)) {
                VaultUtil.getEconomy().withdrawPlayer(player, Wietzaadje_PRICE);
                player.getInventory().addItem(CustomItems.getWietzaadje());
                player.sendMessage(MessageUtil.getMessage("shop.purchase-success"));
            } else {
                player.sendMessage(MessageUtil.getMessage("shop.not-enough-money"));
            }
        }
    }
} 