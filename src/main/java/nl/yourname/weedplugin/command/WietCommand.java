package nl.yourname.weedplugin.command;

import nl.yourname.weedplugin.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handler voor het /wiet commando en subcommando's.
 */
public class WietCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageUtil.getMessage("commands.usage"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "plant":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.players-only"));
                    break;
                }
                Player player = (Player) sender;
                // Check permissie
                if (!sender.hasPermission("weedplugin.plant") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                // Probeer plant te plaatsen via PlantManager
                boolean success = nl.yourname.weedplugin.manager.PlantManagerUtil.tryPlantWeed(player);
                if (success) {
                    player.sendMessage(MessageUtil.getMessage("planting.success"));
                } else {
                    player.sendMessage(MessageUtil.getMessage("planting.cannot-plant"));
                }
                break;
            case "balans":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.balance-console"));
                    break;
                }
                // Check permissie
                if (!sender.hasPermission("weedplugin.balans") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                Player balPlayer = (Player) sender;
                double saldo = nl.yourname.weedplugin.util.VaultUtil.getEconomy().getBalance(balPlayer);
                balPlayer.sendMessage(MessageUtil.getMessage("commands.balance", "amount", String.valueOf(saldo)));
                break;
            case "geefgeld":
                // Check permissie
                if (!sender.hasPermission("weedplugin.geefgeld") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                if (args.length < 3) {
                    sender.sendMessage(MessageUtil.getMessage("commands.give-money-usage"));
                    break;
                }
                Player target = org.bukkit.Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(MessageUtil.getMessage("commands.player-not-found"));
                    break;
                }
                double bedrag;
                try {
                    bedrag = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtil.getMessage("commands.invalid-amount"));
                    break;
                }
                nl.yourname.weedplugin.util.VaultUtil.getEconomy().depositPlayer(target, bedrag);
                sender.sendMessage(MessageUtil.getMessage("commands.money-given", "amount", String.valueOf(bedrag), "player", target.getName()));
                target.sendMessage(MessageUtil.getMessage("commands.money-received", "amount", String.valueOf(bedrag)));
                break;
            case "npc":
                // Check permissie
                if (!sender.hasPermission("weedplugin.npc") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                if (args.length < 2 || !args[1].equalsIgnoreCase("spawn")) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-usage"));
                    break;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-players-only"));
                    break;
                }
                Player npcPlayer = (Player) sender;
                net.citizensnpcs.api.npc.NPCRegistry registry = net.citizensnpcs.api.CitizensAPI.getNPCRegistry();
                net.citizensnpcs.api.npc.NPC npc = registry.createNPC(org.bukkit.entity.EntityType.PLAYER, "§aWiet Verkoper");
                npc.spawn(npcPlayer.getLocation());
                sender.sendMessage(MessageUtil.getMessage("commands.npc-spawned"));
                break;
            case "winkel":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.shop-players-only"));
                    break;
                }
                // Check permissie
                if (!sender.hasPermission("weedplugin.winkel") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                Player shopPlayer = (Player) sender;
                org.bukkit.inventory.Inventory shop = org.bukkit.Bukkit.createInventory(null, 9, "§aGrowshop");
                org.bukkit.plugin.Plugin pluginRef = org.bukkit.Bukkit.getPluginManager().getPlugin("WeedPlugin");
                org.bukkit.configuration.file.FileConfiguration config = pluginRef.getConfig();
                org.bukkit.inventory.ItemStack zaad = new org.bukkit.inventory.ItemStack(org.bukkit.Material.MELON_SEEDS);
                org.bukkit.inventory.meta.ItemMeta zaadMeta = zaad.getItemMeta();
                zaadMeta.setDisplayName("§aWietzaadje");
                java.util.List<String> zaadLore = new java.util.ArrayList<>();
                zaadLore.add("§7Prijs: §e" + config.getInt("shop.zaad"));
                zaadMeta.setLore(zaadLore);
                zaad.setItemMeta(zaadMeta);
                shop.setItem(0, zaad);
                org.bukkit.inventory.ItemStack zakje = nl.yourname.weedplugin.item.CustomItems.getZakje();
                org.bukkit.inventory.meta.ItemMeta zakjeMeta = zakje.getItemMeta();
                java.util.List<String> zakjeLore = new java.util.ArrayList<>();
                zakjeLore.add("§7Prijs: §e" + config.getInt("shop.zakje"));
                zakjeMeta.setLore(zakjeLore);
                zakje.setItemMeta(zakjeMeta);
                shop.setItem(1, zakje);
                org.bukkit.inventory.ItemStack upgrade = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BONE);
                org.bukkit.inventory.meta.ItemMeta upgradeMeta = upgrade.getItemMeta();
                upgradeMeta.setDisplayName("§bUpgrade");
                java.util.List<String> upgradeLore = new java.util.ArrayList<>();
                upgradeLore.add("§7Prijs: §e" + config.getInt("shop.upgrade"));
                upgradeMeta.setLore(upgradeLore);
                upgrade.setItemMeta(upgradeMeta);
                shop.setItem(2, upgrade);
                shopPlayer.openInventory(shop);
                break;
            case "reload":
                // Check permissie
                if (!sender.hasPermission("weedplugin.reload") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                nl.yourname.weedplugin.WeedPlugin plugin = (nl.yourname.weedplugin.WeedPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("WeedPlugin");
                if (plugin != null) {
                    plugin.reloadConfig();
                    MessageUtil.reloadMessages(plugin);
                    sender.sendMessage(MessageUtil.getMessage("commands.config-reloaded"));
                } else {
                    sender.sendMessage(MessageUtil.getMessage("commands.plugin-not-found"));
                }
                break;
            case "cleanuparmorstands":
                // Check permissie
                if (!sender.hasPermission("weedplugin.cleanup") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                // TODO: Verwijder deze tijdelijke cleanup-code in productie
                int removed = 0;
                for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity instanceof org.bukkit.entity.ArmorStand) {
                            org.bukkit.entity.ArmorStand as = (org.bukkit.entity.ArmorStand) entity;
                            if (!as.isVisible()) {
                                as.remove();
                                removed++;
                            }
                        }
                    }
                }
                sender.sendMessage(MessageUtil.getMessage("commands.armorstands-removed", "count", String.valueOf(removed)));
                break;
            case "zaadje":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.seed-players-only"));
                    break;
                }
                // Check permissie
                if (!sender.hasPermission("weedplugin.zaadje") && !sender.isOp()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
                    break;
                }
                Player zPlayer = (Player) sender;
                org.bukkit.inventory.ItemStack zaadje = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SEEDS);
                org.bukkit.inventory.meta.ItemMeta zMeta = zaadje.getItemMeta();
                zMeta.setDisplayName("§aWietzaadje");
                zaadje.setItemMeta(zMeta);
                zPlayer.getInventory().addItem(zaadje);
                zPlayer.sendMessage(MessageUtil.getMessage("commands.seed-received"));
                break;
            default:
                sender.sendMessage(MessageUtil.getMessage("commands.unknown-subcommand"));
        }
        return true;
    }
} 