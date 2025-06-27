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
            sender.sendMessage("§aGebruik: /wiet [plant|balans|geefgeld|npc|winkel|reload|cleanuparmorstands|zaadje]");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "plant":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cAlleen spelers kunnen planten.");
                    break;
                }
                Player player = (Player) sender;
                // Probeer plant te plaatsen via PlantManager
                boolean success = nl.yourname.weedplugin.manager.PlantManagerUtil.tryPlantWeed(player);
                if (success) {
                    player.sendMessage("§aJe hebt een wietplant geplaatst!");
                } else {
                    player.sendMessage("§cJe kunt hier geen wiet planten.");
                }
                break;
            case "balans":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cAlleen spelers hebben een balans.");
                    break;
                }
                Player balPlayer = (Player) sender;
                double saldo = nl.yourname.weedplugin.util.VaultUtil.getEconomy().getBalance(balPlayer);
                balPlayer.sendMessage("§aJe saldo: §e" + saldo);
                break;
            case "geefgeld":
                if (args.length < 3) {
                    sender.sendMessage("§cGebruik: /wiet geefgeld <speler> <bedrag>");
                    break;
                }
                Player target = org.bukkit.Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cSpeler niet gevonden.");
                    break;
                }
                double bedrag;
                try {
                    bedrag = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cOngeldig bedrag.");
                    break;
                }
                nl.yourname.weedplugin.util.VaultUtil.getEconomy().depositPlayer(target, bedrag);
                sender.sendMessage("§a" + bedrag + " gegeven aan " + target.getName());
                target.sendMessage("§aJe hebt " + bedrag + " ontvangen!");
                break;
            case "npc":
                if (args.length < 2 || !args[1].equalsIgnoreCase("spawn")) {
                    sender.sendMessage("§cGebruik: /wiet npc spawn");
                    break;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cAlleen spelers kunnen NPC's spawnen.");
                    break;
                }
                Player npcPlayer = (Player) sender;
                net.citizensnpcs.api.npc.NPCRegistry registry = net.citizensnpcs.api.CitizensAPI.getNPCRegistry();
                net.citizensnpcs.api.npc.NPC npc = registry.createNPC(org.bukkit.entity.EntityType.PLAYER, "§aWiet Verkoper");
                npc.spawn(npcPlayer.getLocation());
                sender.sendMessage("§aNPC gespawned!");
                break;
            case "winkel":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cAlleen spelers kunnen de winkel openen.");
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
                nl.yourname.weedplugin.WeedPlugin plugin = (nl.yourname.weedplugin.WeedPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("WeedPlugin");
                if (plugin != null) {
                    plugin.reloadConfig();
                    MessageUtil.reloadMessages(plugin);
                    sender.sendMessage("§aConfig herladen!");
                } else {
                    sender.sendMessage("§cPlugin niet gevonden!");
                }
                break;
            case "cleanuparmorstands":
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
                sender.sendMessage("§a" + removed + " invisible ArmorStands verwijderd.");
                break;
            case "zaadje":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cAlleen spelers kunnen zaadjes krijgen.");
                    break;
                }
                Player zPlayer = (Player) sender;
                org.bukkit.inventory.ItemStack zaadje = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SEEDS);
                org.bukkit.inventory.meta.ItemMeta zMeta = zaadje.getItemMeta();
                zMeta.setDisplayName("§aWietzaadje");
                zaadje.setItemMeta(zMeta);
                zPlayer.getInventory().addItem(zaadje);
                zPlayer.sendMessage("§aJe hebt een wietzaadje ontvangen!");
                break;
            default:
                sender.sendMessage("§cOnbekend subcommando.");
        }
        return true;
    }
} 