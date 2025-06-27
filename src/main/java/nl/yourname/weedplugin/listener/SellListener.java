package nl.yourname.weedplugin.listener;

import nl.yourname.weedplugin.item.CustomItems;
import nl.yourname.weedplugin.util.MessageUtil;
import nl.yourname.weedplugin.util.VaultUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Location;

/**
 * Regelt verkoop van wietzakken aan deuren en NPC's.
 */
public class SellListener implements Listener {
    private final Random random = new Random();
    private final Map<UUID, Integer> doorHitCount = new HashMap<>(); // speler -> aantal hits
    private final Map<String, Long> doorCooldowns = new HashMap<>(); // deur locatie -> timestamp
    private final Map<UUID, String> playerLastDoor = new HashMap<>(); // speler -> laatste deur locatie
    private final Map<Player, String> activeConversations = new HashMap<>(); // Track actieve gesprekken
    private final Map<Player, Location> conversationLocations = new HashMap<>(); // Track deur locaties
    private static final int HITS_NEEDED = 3;
    private static final long COOLDOWN_TIME = 5 * 60 * 1000; // 5 minuten in milliseconden

    @EventHandler
    public void onDoorSell(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !block.getType().name().contains("DOOR")) return;
        
        Player player = event.getPlayer();
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isWeedItem(hand)) return;
        
        event.setCancelled(true);
        
        // Normaliseer deur locatie (behandel beide blokken als één deur)
        String doorLocation = normalizeDoorLocation(block.getLocation());
        UUID playerId = player.getUniqueId();
        
        // Check cooldown
        if (doorCooldowns.containsKey(doorLocation)) {
            long lastTime = doorCooldowns.get(doorLocation);
            if (System.currentTimeMillis() - lastTime < COOLDOWN_TIME) {
                player.sendMessage(MessageUtil.getMessage("verkoop.deur.cooldown"));
                return;
            }
        }
        
        // Reset hit count if different door
        if (!doorLocation.equals(playerLastDoor.get(playerId))) {
            doorHitCount.put(playerId, 0);
            playerLastDoor.put(playerId, doorLocation);
        }
        
        // Increment hit count
        int hits = doorHitCount.getOrDefault(playerId, 0) + 1;
        doorHitCount.put(playerId, hits);
        
        if (hits >= HITS_NEEDED) {
            // Start selling process
            startSellingProcess(player, doorLocation);
            doorHitCount.remove(playerId);
            playerLastDoor.remove(playerId);
        } else {
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.hit-count", 
                "hits", String.valueOf(hits), 
                "needed", String.valueOf(HITS_NEEDED)));
        }
    }
    
    private String normalizeDoorLocation(org.bukkit.Location location) {
        // Normaliseer naar de onderste deur blok (of bovenste als het een single deur is)
        org.bukkit.Location normalized = location.clone();
        if (location.getBlock().getRelative(0, -1, 0).getType().name().contains("DOOR")) {
            normalized = location.clone().add(0, -1, 0);
        }
        return normalized.getWorld().getName() + "," + normalized.getBlockX() + "," + normalized.getBlockY() + "," + normalized.getBlockZ();
    }
    
    private boolean isWeedItem(ItemStack item) {
        if (item == null || item.getType() != Material.WATER_LILY) return false;
        if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return false;
        return "§aWeed".equals(item.getItemMeta().getDisplayName());
    }
    
    private int countWeedInInventory(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isWeedItem(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private void startSellingProcess(Player player, String doorLocation) {
        // Sla de deur locatie op voor afstand controle
        String[] coords = doorLocation.split(",");
        Location doorLoc = new Location(player.getWorld(), 
            Integer.parseInt(coords[1]), 
            Integer.parseInt(coords[2]), 
            Integer.parseInt(coords[3]));
        conversationLocations.put(player, doorLoc);
        activeConversations.put(player, doorLocation);
        
        player.sendMessage(""); // Lege regel
        player.sendMessage(MessageUtil.getMessage("verkoop.deur.greeting"));
        
        // Start afstand controle
        startDistanceCheck(player, doorLocation);
        
        // Simuleer een korte delay voor realistische dialoog
        org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
            // Check of het gesprek nog actief is
            if (!activeConversations.containsKey(player)) {
                return; // Gesprek is afgebroken
            }
            
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.offer", "player", player.getName()));
            
            // Haal config waarden op
            Plugin weedPlugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
            double politieKans = weedPlugin.getConfig().getDouble("verkoop.deur.politie-kans", 0.3);
            
            // Random kans dat de persoon de politie belt
            if (random.nextDouble() < politieKans) {
                // Politie scenario
                org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                    if (!activeConversations.containsKey(player)) return;
                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.police-scenario"));
                    
                    org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                        if (!activeConversations.containsKey(player)) return;
                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.police-call"));
                        
                        org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                            if (!activeConversations.containsKey(player)) return;
                            player.sendMessage(MessageUtil.getMessage("verkoop.deur.police-leave"));
                            
                            // Einde gesprek
                            endConversation(player, doorLocation);
                        }, 40L); // 2 seconden delay
                    }, 40L); // 2 seconden delay
                }, 20L); // 1 seconde delay
            } else {
                // Normale verkoop scenario
                org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                    if (!activeConversations.containsKey(player)) return;
                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.normal-scenario"));
                    
                    org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                        if (!activeConversations.containsKey(player)) return;
                        int playerWeedCount = countWeedInInventory(player);
                        
                        // Haal config waarden op voor prijs per stuk
                        int minPrijsPerStuk = weedPlugin.getConfig().getInt("verkoop.deur.min-prijs-per-stuk", 10);
                        int maxPrijsPerStuk = weedPlugin.getConfig().getInt("verkoop.deur.max-prijs-per-stuk", 20);
                        int pricePerPiece = minPrijsPerStuk + random.nextInt(maxPrijsPerStuk - minPrijsPerStuk + 1);
                        
                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.weed-count", 
                            "player", player.getName(),
                            "count", String.valueOf(playerWeedCount),
                            "price", String.valueOf(pricePerPiece)));
                        
                        org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                            if (!activeConversations.containsKey(player)) return;
                            // Haal config waarden op
                            Plugin plugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
                            int minAmount = plugin.getConfig().getInt("verkoop.deur.min-hoeveelheid", 3);
                            int maxAmount = plugin.getConfig().getInt("verkoop.deur.max-hoeveelheid", 5);
                            int requestedAmount = minAmount + random.nextInt(maxAmount - minAmount + 1);
                            
                            player.sendMessage(MessageUtil.getMessage("verkoop.deur.request", "amount", String.valueOf(requestedAmount)));
                            
                            org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                                if (!activeConversations.containsKey(player)) return;
                                
                                // Controleer opnieuw hoeveel wiet de speler heeft (mogelijk weggegooid tijdens gesprek)
                                int currentWeedCount = countWeedInInventory(player);
                                
                                if (currentWeedCount == 0) {
                                    // Speler heeft helemaal geen wiet meer
                                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.no-weed", "player", player.getName()));
                                    
                                    org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                                        if (!activeConversations.containsKey(player)) return;
                                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.no-offer"));
                                        
                                        // Einde gesprek
                                        endConversation(player, doorLocation);
                                    }, 20L);
                                } else if (currentWeedCount < requestedAmount) {
                                    // Speler heeft niet genoeg wiet
                                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.insufficient-weed", 
                                        "player", player.getName(),
                                        "count", String.valueOf(currentWeedCount)));
                                    
                                    org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                                        if (!activeConversations.containsKey(player)) return;
                                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.insufficient-offer", "amount", String.valueOf(requestedAmount)));
                                        
                                        // Einde gesprek
                                        endConversation(player, doorLocation);
                                    }, 20L);
                                } else {
                                    // Speler heeft genoeg wiet
                                    int totalPrice = requestedAmount * pricePerPiece;
                                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.total-price", 
                                        "player", player.getName(),
                                        "total", String.valueOf(totalPrice)));
                                    
                                    org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                                        if (!activeConversations.containsKey(player)) return;
                                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.perfect"));
                                        
                                        org.bukkit.Bukkit.getScheduler().runTaskLater(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
                                            if (!activeConversations.containsKey(player)) return;
                                            
                                            // Controleer nog een laatste keer of de speler nog genoeg wiet heeft
                                            int finalWeedCount = countWeedInInventory(player);
                                            if (finalWeedCount < requestedAmount) {
                                                player.sendMessage(MessageUtil.getMessage("verkoop.deur.no-offer"));
                                                endConversation(player, doorLocation);
                                                return;
                                            }
                                            
                                            // Verwijder wiet en geef geld
                                            removeWeedFromInventory(player, requestedAmount);
                                            VaultUtil.getEconomy().depositPlayer(player, totalPrice);
                                            
                                            // Succesbericht met lege regel
                                            player.sendMessage("");
                                            player.sendMessage(MessageUtil.getMessage("verkoop.deur.success", 
                                                "amount", String.valueOf(requestedAmount),
                                                "total", String.valueOf(totalPrice)));
                                            
                                            // Einde gesprek
                                            endConversation(player, doorLocation);
                                        }, 20L);
                                    }, 20L);
                                }
                            }, 20L); // 1 seconde delay
                        }, 20L); // 1 seconde delay
                    }, 20L); // 1 seconde delay
                }, 20L); // 1 seconde delay
            }
        }, 20L); // 1 seconde delay
    }
    
    private void startDistanceCheck(Player player, String doorLocation) {
        // Haal config waarden op
        Plugin weedPlugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
        int maxAfstand = weedPlugin.getConfig().getInt("verkoop.deur.max-afstand", 6);
        
        // Check elke seconde of de speler te ver weg is
        org.bukkit.Bukkit.getScheduler().runTaskTimer(player.getServer().getPluginManager().getPlugin("WeedPlugin"), () -> {
            if (!activeConversations.containsKey(player)) {
                return; // Gesprek is al afgebroken
            }
            
            Location doorLoc = conversationLocations.get(player);
            if (doorLoc != null && player.getLocation().distance(doorLoc) > maxAfstand) {
                // Speler is te ver weg
                player.sendMessage(MessageUtil.getMessage("verkoop.deur.too-far"));
                endConversation(player, doorLocation);
            }
        }, 20L, 20L); // Start na 1 seconde, herhaal elke seconde
    }
    
    private void endConversation(Player player, String doorLocation) {
        activeConversations.remove(player);
        conversationLocations.remove(player);
        doorCooldowns.put(doorLocation, System.currentTimeMillis());
    }
    
    private void removeWeedFromInventory(Player player, int amount) {
        int remainingToRemove = amount;
        
        for (int i = 0; i < player.getInventory().getSize() && remainingToRemove > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isWeedItem(item)) {
                int toRemove = Math.min(remainingToRemove, item.getAmount());
                if (item.getAmount() <= toRemove) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                }
                remainingToRemove -= toRemove;
            }
        }
    }

    @EventHandler
    public void onNpcSell(NPCRightClickEvent event) {
        Player player = event.getClicker();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!hand.isSimilar(CustomItems.getGevuldeWietzak())) return;
        Plugin plugin = player.getServer().getPluginManager().getPlugin("WeedPlugin");
        int prijs = plugin.getConfig().getInt("verkoop.npc.prijs-per-zak");
        VaultUtil.getEconomy().depositPlayer(player, prijs);
        player.sendMessage(MessageUtil.getMessage("verkoop.npc.success", "amount", String.valueOf(prijs)));
        hand.setAmount(hand.getAmount() - 1);
    }

    // Placeholder voor NPC verkoop (Citizens integratie)
    // TODO: Implementeer interactie met Citizens NPC's
} 