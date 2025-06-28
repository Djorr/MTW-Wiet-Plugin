package nl.djorr.mtwwiet.command;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.plant.PlantModule;
import nl.djorr.mtwwiet.plant.factory.PlantFactory;
import nl.djorr.mtwwiet.config.ConfigManager;
import nl.djorr.mtwwiet.shop.ShopModule;
import nl.djorr.mtwwiet.npc.NPCModule;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import nl.djorr.mtwwiet.MTWWiet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import nl.djorr.mtwwiet.item.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.Map;

/**
 * Hoofdcommando voor de wiet plugin.
 */
public class WietCommand implements CommandExecutor {
    private final org.bukkit.plugin.Plugin plugin;
    
    public WietCommand(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "zaadje":
                handleZaadje(sender, args);
                break;
            case "balans":
                handleBalans(sender);
                break;
            case "geefgeld":
                handleGeefGeld(sender, args);
                break;
            case "npc":
                handleNPC(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "cleanuparmorstands":
                handleCleanupArmorStands(sender);
                break;
            case "prijs":
                handlePrijs(sender, args);
                break;
            case "plant":
                handlePlant(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("weedplugin.help") && !sender.isOp()) {
            // Toon plugin informatie voor spelers zonder permissions
            sender.sendMessage("§a=== MTW Wiet Plugin ===");
            sender.sendMessage("§ePlugin: §fMTW-Wiet");
            sender.sendMessage("§eVersie: §f" + plugin.getDescription().getVersion());
            sender.sendMessage("§eAuteur: §f" + plugin.getDescription().getAuthors());
            sender.sendMessage("§eDiscord: §fhttps://discord.rubixdevelopment.nl");
            sender.sendMessage("§7Je hebt geen permissions om de commands te zien.");
            return;
        }

        sender.sendMessage("§a=== MTW Wiet Commands ===");
        sender.sendMessage("§e/wiet zaadje <speler> <aantal> §7- Geef wietzaadjes");
        sender.sendMessage("§e/wiet balans §7- Bekijk je saldo");
        sender.sendMessage("§e/wiet geefgeld <speler> <bedrag> §7- Geef geld");
        sender.sendMessage("§e/wiet npc <spawn|despawn|remove|list> §7- Beheer NPCs");
        sender.sendMessage("§e/wiet reload §7- Herlaad configuratie");
        sender.sendMessage("§e/wiet cleanuparmorstands §7- Verwijder onzichtbare armor stands");
        sender.sendMessage("§e/wiet prijs <item> <prijs> §7- Stel prijzen in");
        sender.sendMessage("§e/wiet plant §7- Plaats een wietplant");
    }
    
    private void handleZaadje(CommandSender sender, String[] args) {
        if (!sender.hasPermission("weedplugin.zaadje") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cGebruik: /wiet zaadje <speler> <aantal>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.getMessage("commands.player-not-found"));
            return;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cOngeldig aantal!");
            return;
        }
        
        CustomItems customItems = PluginContext.getInstance(plugin).getService(CustomItems.class).orElse(null);
        if (customItems != null) {
            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(customItems.createWeedSeed());
            }
            sender.sendMessage("§a" + amount + " wietzaadjes gegeven aan " + target.getName());
            target.sendMessage("§aJe hebt " + amount + " wietzaadjes ontvangen!");
        }
    }
    
    private void handleBalans(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.getMessage("commands.balance-console"));
            return;
        }
        
        if (!sender.hasPermission("weedplugin.balans") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        Player player = (Player) sender;
        double balance = VaultUtil.getEconomy().getBalance(player);
        player.sendMessage(MessageUtil.getMessage("commands.balance", "amount", String.valueOf(balance)));
    }
    
    private void handleGeefGeld(CommandSender sender, String[] args) {
        if (!sender.hasPermission("weedplugin.geefgeld") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage(MessageUtil.getMessage("commands.give-money-usage"));
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.getMessage("commands.player-not-found"));
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.getMessage("commands.invalid-amount"));
            return;
        }
        
        VaultUtil.getEconomy().depositPlayer(target, amount);
        sender.sendMessage(MessageUtil.getMessage("commands.money-given", "amount", String.valueOf(amount), "player", target.getName()));
        target.sendMessage(MessageUtil.getMessage("commands.money-received", "amount", String.valueOf(amount)));
    }
    
    private void handleNPC(CommandSender sender, String[] args) {
        if (!sender.hasPermission("weedplugin.npc") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.getMessage("commands.npc-usage"));
            return;
        }
        
        NPCModule npcModule = PluginContext.getInstance(plugin).getService(NPCModule.class).orElse(null);
        if (npcModule == null) {
            sender.sendMessage("§cNPC module niet beschikbaar!");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-players-only"));
                    return;
                }
                Player npcPlayer = (Player) sender;
                npcModule.spawnWeedNPC(npcPlayer.getLocation());
                sender.sendMessage(MessageUtil.getMessage("commands.npc-spawned"));
                break;
                
            case "despawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-players-only"));
                    return;
                }
                Player despawnPlayer = (Player) sender;
                Entity despawnTarget = null;
                for (Entity e : despawnPlayer.getNearbyEntities(5, 5, 5)) {
                    if (e.getType() == EntityType.PLAYER && despawnPlayer.hasLineOfSight(e)) {
                        double dot = despawnPlayer.getLocation().getDirection().normalize().dot(e.getLocation().toVector().subtract(despawnPlayer.getEyeLocation().toVector()).normalize());
                        if (dot > 0.95) {
                            despawnTarget = e;
                            break;
                        }
                    }
                }
                if (despawnTarget != null) {
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(despawnTarget);
                    if (npc != null && npc.getName().contains("Dealer")) {
                        if (npcModule.removeWeedNPC(npc.getUniqueId())) {
                            sender.sendMessage(MessageUtil.getMessage("commands.npc-despawned"));
                        } else {
                            sender.sendMessage(MessageUtil.getMessage("commands.npc-not-found"));
                        }
                    } else {
                        sender.sendMessage(MessageUtil.getMessage("commands.npc-not-found"));
                    }
                } else {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-no-entity"));
                }
                break;
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-remove-usage"));
                    return;
                }
                try {
                    UUID npcId = UUID.fromString(args[2]);
                    boolean removed = npcModule.removeWeedNPC(npcId);
                    if (removed) {
                        sender.sendMessage(MessageUtil.getMessage("commands.npc-removed"));
                    } else {
                        sender.sendMessage(MessageUtil.getMessage("commands.npc-not-found"));
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-invalid-id"));
                }
                break;
                
            case "list":
                java.util.Map<UUID, NPC> npcs = npcModule.getAllWeedNPCs();
                if (npcs.isEmpty()) {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-none-found"));
                } else {
                    sender.sendMessage(MessageUtil.getMessage("commands.npc-list-header"));
                    for (Map.Entry<UUID, NPC> entry : npcs.entrySet()) {
                        NPC listNpc = entry.getValue();
                        Location loc = listNpc.getEntity().getLocation();
                        sender.sendMessage("§e- " + listNpc.getName() + " §7(ID: " + entry.getKey() + ") §7at " + 
                                         String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                    }
                }
                break;
                
            default:
                sender.sendMessage(MessageUtil.getMessage("commands.npc-usage"));
                break;
        }
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("weedplugin.reload") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        // Reload config
        plugin.reloadConfig();
        sender.sendMessage("§aConfiguratie herladen!");
    }
    
    private void handleCleanupArmorStands(CommandSender sender) {
        if (!sender.hasPermission("weedplugin.cleanuparmorstands") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        int count = 0;
        
        // Loop through all worlds and remove invisible armor stands
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    entity.remove();
                    count++;
                }
            }
        }
        
        sender.sendMessage(MessageUtil.getMessage("commands.armorstands-removed", "count", String.valueOf(count)));
    }
    
    private void handlePrijs(CommandSender sender, String[] args) {
        if (!sender.hasPermission("weedplugin.prijs") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§cGebruik: /wiet prijs <item> <prijs>");
            return;
        }
        
        String item = args[1];
        double price;
        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cOngeldige prijs!");
            return;
        }
        
        // Save price to config
        plugin.getConfig().set("prices." + item, price);
        plugin.saveConfig();
        sender.sendMessage("§aPrijs voor " + item + " ingesteld op €" + price);
    }
    
    private void handlePlant(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.getMessage("commands.players-only"));
            return;
        }
        
        Player player = (Player) sender;
        if (!sender.hasPermission("weedplugin.plant") && !sender.isOp()) {
            sender.sendMessage(MessageUtil.getMessage("commands.no-permission"));
            return;
        }
        
        boolean success = tryPlantWeed(player);
        if (success) {
            player.sendMessage(MessageUtil.getMessage("planting.success"));
        } else {
            player.sendMessage(MessageUtil.getMessage("planting.cannot-plant"));
        }
    }

    /**
     * Probeer een wietplant te plaatsen voor de gegeven speler.
     */
    private boolean tryPlantWeed(Player player) {
        org.bukkit.block.Block block = player.getTargetBlock((java.util.Set<org.bukkit.Material>) null, 5);
        if (block == null || block.getType() != org.bukkit.Material.GRASS) {
            player.sendMessage(MessageUtil.getMessage("planting.grass-only"));
            return false;
        }
        // Check of speler een custom seed met naam 'Wietzaadje' in de hand heeft
        org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != org.bukkit.Material.SEEDS || hand.getItemMeta() == null || !"§aWietzaadje".equals(hand.getItemMeta().getDisplayName())) {
            player.sendMessage(MessageUtil.getMessage("planting.need-seed"));
            return false;
        }
        
        // Get services
        PlantModule plantModule = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(PlantModule.class).orElse(null);
        ConfigManager configManager = PluginContext.getInstance(MTWWiet.getPlugin(MTWWiet.class)).getService(ConfigManager.class).orElse(null);
        
        if (plantModule == null || configManager == null) {
            return false;
        }
        
        // Check if already planted
        if (plantModule.isPlantAt(block.getLocation())) {
            player.sendMessage(MessageUtil.getMessage("planting.already-planted"));
            return false;
        }
        
        // Create plant using factory
        long growTime = configManager.getConfig().getLong("planting.grow-time", 300000);
        nl.djorr.mtwwiet.plant.model.PlantData data = PlantFactory.createPlant(player.getUniqueId(), block.getLocation(), growTime);
        plantModule.addPlant(block.getLocation(), data);
        
        // Verwijder 1 zaadje uit de hand
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        return true;
    }
} 