package nl.djorr.mtwwiet.sell.listener;

import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.sell.SellModule;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.item.CustomItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

/**
 * Luistert naar verkoop-gerelateerde events (deur kloppen, verkoop gesprekken).
 */
public class SellListener implements Listener {
    private final Plugin plugin;
    private final CustomItems customItems;
    private final Map<UUID, DoorConversation> doorConversations = new HashMap<>();
    
    // Nieuwe maps voor 3-klop systeem
    private final Map<UUID, DoorKnockData> doorKnockData = new HashMap<>();
    private final Map<Location, Long> doorKnockTimers = new HashMap<>();
    
    // Per deur cooldown systeem
    private final Map<String, Map<UUID, Long>> doorCooldowns = new HashMap<>();
    private File doorsFile;
    private FileConfiguration doorsConfig;

    public SellListener(Plugin plugin, CustomItems customItems) {
        this.plugin = plugin;
        this.customItems = customItems;
        loadDoorsConfig();
    }
    
    private void loadDoorsConfig() {
        try {
            doorsFile = new File(plugin.getDataFolder(), "doors.yml");
            if (!doorsFile.exists()) {
                plugin.saveResource("doors.yml", false);
            }
            doorsConfig = YamlConfiguration.loadConfiguration(doorsFile);
            
            // Load existing cooldowns
            if (doorsConfig.contains("cooldowns")) {
                for (String doorKey : doorsConfig.getConfigurationSection("cooldowns").getKeys(false)) {
                    Map<UUID, Long> playerCooldowns = new HashMap<>();
                    for (String playerId : doorsConfig.getConfigurationSection("cooldowns." + doorKey).getKeys(false)) {
                        long cooldownTime = doorsConfig.getLong("cooldowns." + doorKey + "." + playerId);
                        playerCooldowns.put(UUID.fromString(playerId), cooldownTime);
                    }
                    doorCooldowns.put(doorKey, playerCooldowns);
                }
            }
            
            plugin.getLogger().info("Doors cooldown system loaded successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load doors.yml: " + e.getMessage());
        }
    }
    
    private void saveDoorsConfig() {
        try {
            // Clear existing cooldowns
            doorsConfig.set("cooldowns", null);
            
            // Save current cooldowns
            for (Map.Entry<String, Map<UUID, Long>> doorEntry : doorCooldowns.entrySet()) {
                String doorKey = doorEntry.getKey();
                Map<UUID, Long> playerCooldowns = doorEntry.getValue();
                
                for (Map.Entry<UUID, Long> playerEntry : playerCooldowns.entrySet()) {
                    doorsConfig.set("cooldowns." + doorKey + "." + playerEntry.getKey().toString(), playerEntry.getValue());
                }
            }
            
            doorsConfig.save(doorsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save doors.yml: " + e.getMessage());
        }
    }
    
    private String getDoorKey(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
    
    private boolean isDoorOnCooldown(Player player, Location doorLocation) {
        String doorKey = getDoorKey(doorLocation);
        Map<UUID, Long> playerCooldowns = doorCooldowns.get(doorKey);
        
        if (playerCooldowns == null) return false;
        
        Long lastKnock = playerCooldowns.get(player.getUniqueId());
        if (lastKnock == null) return false;
        
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getConfig().getLong("verkoop.deur.cooldown-time", 300000); // 5 minuten default
        
        return (currentTime - lastKnock) < cooldownTime;
    }
    
    private void setDoorCooldown(Player player, Location doorLocation) {
        String doorKey = getDoorKey(doorLocation);
        doorCooldowns.computeIfAbsent(doorKey, k -> new HashMap<>()).put(player.getUniqueId(), System.currentTimeMillis());
        saveDoorsConfig();
    }
    
    private void removeDoorCooldown(Player player, Location doorLocation) {
        String doorKey = getDoorKey(doorLocation);
        Map<UUID, Long> playerCooldowns = doorCooldowns.get(doorKey);
        if (playerCooldowns != null) {
            playerCooldowns.remove(player.getUniqueId());
            if (playerCooldowns.isEmpty()) {
                doorCooldowns.remove(doorKey);
            }
            saveDoorsConfig();
        }
    }

    @EventHandler
    public void onDoorKnock(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Check of het een deur is (elke blok met "DOOR" in de naam)
        String blockName = block.getType().name();
        if (!blockName.contains("DOOR")) return;
        
        Player player = event.getPlayer();

        // NIEUW: Check of speler een custom wiet-item in zijn hand heeft
        if (!customItems.isWeed(player.getInventory().getItemInMainHand())) return;
        
        // Check of speler al in een deur conversatie zit
        if (doorConversations.containsKey(player.getUniqueId())) {
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.already-in-conversation"));
            event.setCancelled(true);
            return;
        }
        
        // (optioneel) Check of speler wiet heeft in inventory (voor andere logica)
        if (!customItems.hasWeedItems(player)) {
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.no-weed"));
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Location doorLocation = block.getLocation();
        long currentTime = System.currentTimeMillis();
        
        // Check cooldown voor deze speler
        if (isDoorOnCooldown(player, doorLocation)) {
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.cooldown"));
            return;
        }
        
        // Haal knock data op voor deze speler
        DoorKnockData knockData = doorKnockData.get(playerId);
        if (knockData == null) {
            // Eerste klop
            knockData = new DoorKnockData(doorLocation, currentTime);
            doorKnockData.put(playerId, knockData);
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.knock-1"));
        } else {
            // Check of het dezelfde deur is
            if (!knockData.doorLocation.equals(doorLocation)) {
                // Andere deur - reset
                knockData = new DoorKnockData(doorLocation, currentTime);
                doorKnockData.put(playerId, knockData);
                player.sendMessage(MessageUtil.getMessage("verkoop.deur.knock-1"));
            } else {
                // Zelfde deur - check tijd
                long timeSinceFirstKnock = currentTime - knockData.firstKnockTime;
                long maxTimeBetweenKnocks = plugin.getConfig().getLong("verkoop.deur.max-time-between-knocks", 2000); // 2 seconden default
                
                if (timeSinceFirstKnock > maxTimeBetweenKnocks) {
                    // Te lang gewacht - reset
                    knockData = new DoorKnockData(doorLocation, currentTime);
                    doorKnockData.put(playerId, knockData);
                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.knock-1"));
                } else {
                    // Binnen tijd - verhoog klop teller
                    knockData.knockCount++;
                    knockData.lastKnockTime = currentTime;
                    
                    if (knockData.knockCount == 2) {
                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.knock-2"));
                    } else if (knockData.knockCount >= 3) {
                        // 3 keer geklopt - start verkoop gesprek
                        startDoorConversation(player, doorLocation);
                        setDoorCooldown(player, doorLocation);
                        doorKnockData.remove(playerId); // Reset knock data
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        
        // Update laatste klop tijd
        knockData.lastKnockTime = currentTime;
        
        // Start timer om knock data te resetten als speler te lang wacht
        startKnockResetTimer(playerId, doorLocation);
    }
    
    private void startKnockResetTimer(UUID playerId, Location doorLocation) {
        // Cancel bestaande timer als die er is
        if (doorKnockTimers.containsKey(doorLocation)) {
            plugin.getServer().getScheduler().cancelTask(doorKnockTimers.get(doorLocation).intValue());
        }
        
        // Start nieuwe timer
        long maxTimeBetweenKnocks = plugin.getConfig().getLong("verkoop.deur.max-time-between-knocks", 2000);
        long timerId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Reset knock data als timer afloopt
            DoorKnockData knockData = doorKnockData.get(playerId);
            if (knockData != null && knockData.doorLocation.equals(doorLocation)) {
                doorKnockData.remove(playerId);
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.knock-timeout"));
                }
            }
            doorKnockTimers.remove(doorLocation);
        }, maxTimeBetweenKnocks / 50).getTaskId(); // Convert to ticks
        
        doorKnockTimers.put(doorLocation, timerId);
    }
    
    private void startDoorConversation(Player player, Location doorLocation) {
        DoorConversation conversation = new DoorConversation(player, doorLocation);
        doorConversations.put(player.getUniqueId(), conversation);
        
        // Eerste bericht - "Wat doe je hier?"
        player.sendMessage(MessageUtil.getMessage("verkoop.deur.greeting"));
        
        // Start automatische conversatie flow
        startAutomaticConversation(player, conversation);
    }
    
    private void startAutomaticConversation(Player player, DoorConversation conversation) {
        // Simuleer NPC antwoord na 2 seconden
        conversation.startDelayedTimer(() -> {
            // Check of speler nog steeds wiet heeft
            int weedCount = customItems.countWeedItems(player);
            if (weedCount == 0) {
                // Geen wiet meer - scam detection
                player.sendMessage("§e[Persoon achter deur] §fWat doe je hier? Probeer je me te belazeren? Wegwezen!");
                doorConversations.remove(player.getUniqueId());
                return;
            }
            
            // NPC vraagt wat de speler wil
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.offer"));
            
            // Speler antwoordt na 2 seconden
            conversation.startDelayedTimer(() -> {
                // Check opnieuw of speler nog steeds wiet heeft
                int currentWeedCount = customItems.countWeedItems(player);
                if (currentWeedCount == 0) {
                    player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet gebleven? Probeer je me te scammen? Wegwezen!");
                    doorConversations.remove(player.getUniqueId());
                    return;
                }
                
                // Simuleer speler response
                player.sendMessage("§e[" + player.getName() + "] §fIk wil wiet verkopen.");
                
                // NPC vraagt hoeveel na 2 seconden
                conversation.startDelayedTimer(() -> {
                    // Check opnieuw
                    int weedCount2 = customItems.countWeedItems(player);
                    if (weedCount2 == 0) {
                        player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te belazeren? Wegwezen!");
                        doorConversations.remove(player.getUniqueId());
                        return;
                    }
                    
                    // NPC vraagt hoeveel per stuk
                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.normal-scenario"));
                    
                    // Speler antwoordt met hoeveelheid en prijs na 2 seconden
                    conversation.startDelayedTimer(() -> {
                        // Check opnieuw
                        int weedCount3 = customItems.countWeedItems(player);
                        if (weedCount3 == 0) {
                            player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet? Probeer je me te scammen? Wegwezen!");
                            doorConversations.remove(player.getUniqueId());
                            return;
                        }
                        
                        int pricePerItem = (int)(Math.random() * 11) + 10; // 10-20 euro per stuk
                        
                        // Speler biedt aan
                        String weedCountMessage = MessageUtil.getMessage("verkoop.deur.weed-count", 
                            "count", String.valueOf(weedCount3), 
                            "price", String.valueOf(pricePerItem));
                        weedCountMessage = weedCountMessage.replace("{player}", player.getName());
                        player.sendMessage(weedCountMessage);
                        
                        // NPC koopt wiet na 2 seconden
                        conversation.startDelayedTimer(() -> {
                            // Check opnieuw
                            int weedCount4 = customItems.countWeedItems(player);
                            if (weedCount4 == 0) {
                                player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te belazeren? Wegwezen!");
                                doorConversations.remove(player.getUniqueId());
                                return;
                            }
                            
                            int amount = Math.min(weedCount4, (int)(Math.random() * 3) + 3); // 3-5 items
                            int totalPrice = amount * pricePerItem;
                            
                            // NPC wil kopen
                            player.sendMessage(MessageUtil.getMessage("verkoop.deur.request", "amount", String.valueOf(amount)));
                            
                            // Speler bevestigt na 2 seconden
                            conversation.startDelayedTimer(() -> {
                                // Check opnieuw
                                int weedCount5 = customItems.countWeedItems(player);
                                if (weedCount5 == 0) {
                                    player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet gebleven? Probeer je me te scammen? Wegwezen!");
                                    doorConversations.remove(player.getUniqueId());
                                    return;
                                }
                                
                                if (weedCount5 < amount) {
                                    player.sendMessage("§e[Persoon achter deur] §fJe hebt maar " + weedCount5 + " wiet! Probeer je me te belazeren? Wegwezen!");
                                    doorConversations.remove(player.getUniqueId());
                                    return;
                                }
                                
                                String totalPriceMessage = MessageUtil.getMessage("verkoop.deur.total-price", 
                                    "total", String.valueOf(totalPrice));
                                totalPriceMessage = totalPriceMessage.replace("{player}", player.getName());
                                player.sendMessage(totalPriceMessage);
                                
                                // NPC bevestigt na 2 seconden
                                conversation.startDelayedTimer(() -> {
                                    // Final check
                                    int weedCount6 = customItems.countWeedItems(player);
                                    if (weedCount6 == 0) {
                                        player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te scammen? Wegwezen!");
                                        doorConversations.remove(player.getUniqueId());
                                        return;
                                    }
                                    
                                    if (weedCount6 < amount) {
                                        player.sendMessage("§e[Persoon achter deur] §fJe hebt maar " + weedCount6 + " wiet! Probeer je me te belazeren? Wegwezen!");
                                        doorConversations.remove(player.getUniqueId());
                                        return;
                                    }
                                    
                                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.perfect"));
                                    
                                    // Transactie voltooid na 1 seconde
                                    conversation.startDelayedTimer(() -> {
                                        // Final final check
                                        int weedCount7 = customItems.countWeedItems(player);
                                        if (weedCount7 == 0) {
                                            player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te scammen? Wegwezen!");
                                            doorConversations.remove(player.getUniqueId());
                                            return;
                                        }
                                        
                                        if (weedCount7 < amount) {
                                            player.sendMessage("§e[Persoon achter deur] §fJe hebt maar " + weedCount7 + " wiet! Probeer je me te belazeren? Wegwezen!");
                                            doorConversations.remove(player.getUniqueId());
                                            return;
                                        }
                                        
                                        // Verwijder wiet uit inventory
                                        removeWeedFromInventory(player, amount);
                                        
                                        // Geef geld
                                        nl.djorr.mtwwiet.util.VaultUtil.getEconomy().depositPlayer(player, totalPrice);
                                        
                                        // Success message
                                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.success", 
                                            "amount", String.valueOf(amount), 
                                            "total", String.valueOf(totalPrice)));
                                        
                                        // Politie melding (kans)
                                        if (Math.random() < plugin.getConfig().getDouble("verkoop.deur.politie-kans", 0.3)) {
                                            sendPoliceNotification(player, conversation.getDoorLocation());
                                        }
                                        
                                        doorConversations.remove(player.getUniqueId());
                                    }, 1000L); // 1 seconde
                                }, 2000L); // 2 seconden
                            }, 2000L); // 2 seconden
                        }, 2000L); // 2 seconden
                    }, 2000L); // 2 seconden
                }, 2000L); // 2 seconden
            }, 2000L); // 2 seconden
        }, 2000L); // 2 seconden
    }
    
    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        DoorConversation conversation = doorConversations.get(player.getUniqueId());
        if (conversation == null) return;
        
        event.setCancelled(true);
        String message = event.getMessage().toLowerCase();
        
        if (message.contains("wiet") || message.contains("weed") || message.contains("verkopen")) {
            // Speler biedt wiet aan - start automatische verkoop
            handleWeedOffer(player, conversation);
        } else if (message.contains("nee") || message.contains("niet") || message.contains("stop")) {
            // Speler stopt gesprek
            doorConversations.remove(player.getUniqueId());
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.leave"));
        } else {
            // Onbekend bericht
            player.sendMessage(MessageUtil.getMessage("verkoop.deur.unknown"));
        }
    }
    
    private void handleWeedOffer(Player player, DoorConversation conversation) {
        int weedCount = customItems.countWeedItems(player);
        
        if (weedCount == 0) {
            // Geen wiet meer
            player.sendMessage("§e[Persoon achter deur] §fWat doe je hier? Probeer je me te belazeren? Wegwezen!");
            doorConversations.remove(player.getUniqueId());
            return;
        }
        
        // Bied wiet aan
        player.sendMessage(MessageUtil.getMessage("verkoop.deur.offer"));
        
        // Speler antwoordt na 2 seconden
        conversation.startDelayedTimer(() -> {
            // Check opnieuw
            int weedCount2 = customItems.countWeedItems(player);
            if (weedCount2 == 0) {
                player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet gebleven? Probeer je me te scammen? Wegwezen!");
                doorConversations.remove(player.getUniqueId());
                return;
            }
            
            // Simuleer speler response
            player.sendMessage("§e[" + player.getName() + "] §fIk wil wiet verkopen.");
            
            // NPC vraagt hoeveel na 2 seconden
            conversation.startDelayedTimer(() -> {
                // Check opnieuw
                int weedCount3 = customItems.countWeedItems(player);
                if (weedCount3 == 0) {
                    player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te belazeren? Wegwezen!");
                    doorConversations.remove(player.getUniqueId());
                    return;
                }
                
                // NPC vraagt hoeveel per stuk
                player.sendMessage(MessageUtil.getMessage("verkoop.deur.normal-scenario"));
                
                // Speler antwoordt met hoeveelheid en prijs na 2 seconden
                conversation.startDelayedTimer(() -> {
                    // Check opnieuw
                    int weedCount4 = customItems.countWeedItems(player);
                    if (weedCount4 == 0) {
                        player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet? Probeer je me te scammen? Wegwezen!");
                        doorConversations.remove(player.getUniqueId());
                        return;
                    }
                    
                    int pricePerItem = (int)(Math.random() * 11) + 10; // 10-20 euro per stuk
                    
                    // Speler biedt aan
                    String weedCountMessage = MessageUtil.getMessage("verkoop.deur.weed-count", 
                        "count", String.valueOf(weedCount4), 
                        "price", String.valueOf(pricePerItem));
                    weedCountMessage = weedCountMessage.replace("{player}", player.getName());
                    player.sendMessage(weedCountMessage);
                    
                    // NPC koopt wiet na 2 seconden
                    conversation.startDelayedTimer(() -> {
                        // Check opnieuw
                        int weedCount5 = customItems.countWeedItems(player);
                        if (weedCount5 == 0) {
                            player.sendMessage("§e[Persoon achter deur] §fJe hebt geen wiet meer! Probeer je me te belazeren? Wegwezen!");
                            doorConversations.remove(player.getUniqueId());
                            return;
                        }
                        
                        int amount = Math.min(weedCount5, (int)(Math.random() * 3) + 3); // 3-5 items
                        int totalPrice = amount * pricePerItem;
                        
                        // NPC wil kopen
                        player.sendMessage(MessageUtil.getMessage("verkoop.deur.request", "amount", String.valueOf(amount)));
                        
                        // Speler bevestigt na 2 seconden
                        conversation.startDelayedTimer(() -> {
                            // Check opnieuw
                            int weedCount6 = customItems.countWeedItems(player);
                            if (weedCount6 == 0) {
                                player.sendMessage("§e[Persoon achter deur] §fWaar is je wiet gebleven? Probeer je me te scammen? Wegwezen!");
                                doorConversations.remove(player.getUniqueId());
                                return;
                            }
                            
                            if (weedCount6 < amount) {
                                player.sendMessage("§e[Persoon achter deur] §fJe hebt maar " + weedCount6 + " wiet! Probeer je me te belazeren? Wegwezen!");
                                doorConversations.remove(player.getUniqueId());
                                return;
                            }
                            
                            String totalPriceMessage = MessageUtil.getMessage("verkoop.deur.total-price", 
                                "total", String.valueOf(totalPrice));
                            totalPriceMessage = totalPriceMessage.replace("{player}", player.getName());
                            player.sendMessage(totalPriceMessage);
                            
                            // NPC bevestigt na 2 seconden
                            conversation.startDelayedTimer(() -> {
                                player.sendMessage(MessageUtil.getMessage("verkoop.deur.perfect"));
                                
                                // Transactie voltooid na 1 seconde
                                conversation.startDelayedTimer(() -> {
                                    // Verwijder wiet uit inventory
                                    removeWeedFromInventory(player, amount);
                                    
                                    // Geef geld
                                    nl.djorr.mtwwiet.util.VaultUtil.getEconomy().depositPlayer(player, totalPrice);
                                    
                                    // Success message
                                    player.sendMessage(MessageUtil.getMessage("verkoop.deur.success", 
                                        "amount", String.valueOf(amount), 
                                        "total", String.valueOf(totalPrice)));
                                    
                                    // Politie melding (kans)
                                    if (Math.random() < plugin.getConfig().getDouble("verkoop.deur.politie-kans", 0.3)) {
                                        sendPoliceNotification(player, conversation.getDoorLocation());
                                    }
                                    
                                    doorConversations.remove(player.getUniqueId());
                                }, 1000L); // 1 seconde
                            }, 2000L); // 2 seconden
                        }, 2000L); // 2 seconden
                    }, 2000L); // 2 seconden
                }, 2000L); // 2 seconden
            }, 2000L); // 2 seconden
        }, 2000L); // 2 seconden
    }
    
    private void removeWeedFromInventory(Player player, int amount) {
        int remaining = amount;
        org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            org.bukkit.inventory.ItemStack item = contents[i];
            if (customItems.isWeed(item)) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
    }
    
    private void sendPoliceNotification(Player player, Location doorLocation) {
        String message = MessageUtil.getMessage("verkoop.deur.police-notification",
            "x", String.valueOf(doorLocation.getBlockX()),
            "y", String.valueOf(doorLocation.getBlockY()),
            "z", String.valueOf(doorLocation.getBlockZ()),
            "world", doorLocation.getWorld().getName());
        
        // Stuur naar alle staff/ops
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("weedplugin.admin") || onlinePlayer.isOp()) {
                onlinePlayer.sendMessage(message);
            }
        }
    }
    
    // Nieuwe class voor knock data
    private static class DoorKnockData {
        final Location doorLocation;
        final long firstKnockTime;
        long lastKnockTime;
        int knockCount;
        
        DoorKnockData(Location doorLocation, long firstKnockTime) {
            this.doorLocation = doorLocation;
            this.firstKnockTime = firstKnockTime;
            this.lastKnockTime = firstKnockTime;
            this.knockCount = 1;
        }
    }
    
    private static class DoorConversation {
        private final Player player;
        private final Location doorLocation;
        private org.bukkit.scheduler.BukkitTask responseTimer;
        
        public DoorConversation(Player player, Location doorLocation) {
            this.player = player;
            this.doorLocation = doorLocation;
        }
        
        public void startResponseTimer(Runnable onTimeout) {
            if (responseTimer != null) {
                responseTimer.cancel();
            }
            
            long responseTime = 30000; // 30 seconden default
            responseTimer = nl.djorr.mtwwiet.MTWWiet.getPlugin(nl.djorr.mtwwiet.MTWWiet.class).getServer().getScheduler().runTaskLater(
                nl.djorr.mtwwiet.MTWWiet.getPlugin(nl.djorr.mtwwiet.MTWWiet.class), onTimeout, responseTime / 50);
        }
        
        public void startDelayedTimer(Runnable onTimeout, long delayMillis) {
            if (responseTimer != null) {
                responseTimer.cancel();
            }
            
            responseTimer = nl.djorr.mtwwiet.MTWWiet.getPlugin(nl.djorr.mtwwiet.MTWWiet.class).getServer().getScheduler().runTaskLater(
                nl.djorr.mtwwiet.MTWWiet.getPlugin(nl.djorr.mtwwiet.MTWWiet.class), onTimeout, delayMillis / 50);
        }
        
        public Location getDoorLocation() {
            return doorLocation;
        }
    }
} 