package nl.djorr.mtwwiet.npc.conversation;

import nl.djorr.mtwwiet.config.ConfigManager;
import nl.djorr.mtwwiet.core.PluginContext;
import nl.djorr.mtwwiet.util.MessageUtil;
import nl.djorr.mtwwiet.util.VaultUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Arrays;

/**
 * Handelt chat-gebaseerde dealer conversaties af.
 */
public class DealerConversation {
    private final Plugin plugin;
    private final Map<UUID, ConversationState> activeConversations = new HashMap<>();
    private final Map<UUID, Integer> currentPurchaseAmounts = new HashMap<>();
    private final Map<UUID, Integer> currentPurchaseCosts = new HashMap<>();
    private final Random random = new Random();
    
    // Config values
    private final int minAmount = 1;
    private final int maxAmount = 50;
    private final int conversationRange = 5;
    
    // Random price ranges
    private final int minSeedAmount = 26;
    private final int maxSeedAmount = 50;
    private final int minPricePerSeed = 40;
    private final int maxPricePerSeed = 60;

    private int stock;
    
    public enum ConversationState {
        GREETING,
        OFFER,
        ASKING_AMOUNT,
        CONFIRMING_PURCHASE,
        COMPLETED
    }
    
    public DealerConversation(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start een nieuwe conversatie met een speler.
     */
    public void startConversation(Player player, Entity dealer) {
        UUID playerId = player.getUniqueId();

        stock = getRandomSeedAmount();
        
        // Clean up any existing conversation first
        if (activeConversations.containsKey(playerId)) {
            plugin.getLogger().info("Cleaning up existing conversation for player: " + player.getName());
            activeConversations.remove(playerId);
            currentPurchaseAmounts.remove(playerId);
            currentPurchaseCosts.remove(playerId);
        }
        
        // Voeg spatie toe aan het begin van het gesprek
        player.sendMessage("");
        
        activeConversations.put(playerId, ConversationState.GREETING);
        plugin.getLogger().info("Started new conversation for player: " + player.getName());
        sendGreeting(player);
    }
    
    /**
     * Handle chat input van speler tijdens conversatie.
     */
    public void handleChatInput(Player player, String message) {
        UUID playerId = player.getUniqueId();
        ConversationState state = activeConversations.get(playerId);
        
        if (state == null) {
            return; // Niet in conversatie
        }
        
        // Check of speler nog in range is
        if (!isPlayerInRange(player)) {
            endConversation(player, "dealer.conversation.left-range");
            return;
        }
        
        // Handle cancel/annuleren
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("annuleren")) {
            endConversation(player, "dealer.conversation.cancelled");
            return;
        }
        
        switch (state) {
            case GREETING:
                handleGreetingResponse(player, message);
                break;
            case OFFER:
                handleOfferResponse(player, message);
                break;
            case ASKING_AMOUNT:
                handleAmountInput(player, message);
                break;
            case CONFIRMING_PURCHASE:
                handlePurchaseConfirmation(player, message);
                break;
        }
    }
    
    /**
     * Stuur greeting message.
     */
    private void sendGreeting(Player player) {
        List<String> greetings = getConfigMessages("dealer-greeting");
        String greeting = getRandomMessage(greetings);
        player.sendMessage(greeting);
        
        // Wacht even en stuur dan de offer
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (isInConversation(player)) {
                sendOffer(player);
            }
        }, 20L); // 1 seconde
    }
    
    /**
     * Handle response op greeting.
     */
    private void handleGreetingResponse(Player player, String message) {
        // Toon speler bericht in chat
        player.sendMessage("§e[" + player.getName() + "] §f" + message);
        
        // Accepteer elke response en ga door naar offer
        List<String> responses = getConfigMessages("player-interested");
        String response = getRandomMessage(responses);
        response = response.replace("{player}", player.getName());
        player.sendMessage(response);
        
        activeConversations.put(player.getUniqueId(), ConversationState.OFFER);
        
        // Wacht even en stuur dan de offer
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (isInConversation(player)) {
                sendOffer(player);
            }
        }, 20L);
    }
    
    /**
     * Stuur offer message.
     */
    private void sendOffer(Player player) {
        List<String> offers = getConfigMessages("dealer-offer");
        String offer = getRandomMessage(offers);
        
        // Vervang price placeholder met random prijs
        int price = getRandomWeedSeedPrice();
        offer = offer.replace("{price}", price + " euro");
        
        player.sendMessage(offer);
        
        // Stuur stock info met random hoeveelheid
        List<String> stockMessages = getConfigMessages("dealer-stock");
        String stockMessage = getRandomMessage(stockMessages);
        stockMessage = stockMessage.replace("{stock}", String.valueOf(stock));
        player.sendMessage(stockMessage);
        
        activeConversations.put(player.getUniqueId(), ConversationState.ASKING_AMOUNT);
        
        // Vraag hoeveel ze willen - gebruik config berichten
        List<String> askAmountMessages = getConfigMessages("dealer-ask-amount");
        String askMessage = getRandomMessage(askAmountMessages);
        askMessage = askMessage.replace("{min}", String.valueOf(minAmount))
                              .replace("{max}", String.valueOf(stock)); // Gebruik random stock als max
        player.sendMessage(askMessage);
    }
    
    /**
     * Handle response op offer.
     */
    private void handleOfferResponse(Player player, String message) {
        // Toon speler bericht in chat
        player.sendMessage("§e[" + player.getName() + "] §f" + message);
        
        // Accepteer elke response en vraag hoeveel ze willen
        List<String> responses = getConfigMessages("player-ask-price");
        String response = getRandomMessage(responses);
        response = response.replace("{player}", player.getName());
        player.sendMessage(response);
        
        activeConversations.put(player.getUniqueId(), ConversationState.ASKING_AMOUNT);
        
        // Vraag hoeveel ze willen - gebruik config berichten
        List<String> askAmountMessages = getConfigMessages("dealer-ask-amount");
        String askMessage = getRandomMessage(askAmountMessages);
        askMessage = askMessage.replace("{min}", String.valueOf(minAmount))
                              .replace("{max}", String.valueOf(maxAmount));
        player.sendMessage(askMessage);
    }
    
    /**
     * Handle amount input van speler.
     */
    private void handleAmountInput(Player player, String message) {
        // Toon speler bericht in chat
        player.sendMessage("§e[" + player.getName() + "] §f" + message);
        
        try {
            int amount = Integer.parseInt(message.trim());
            
            if (amount < minAmount || amount > maxAmount) {
                List<String> invalidAmountMessages = getConfigMessages("dealer-invalid-amount");
                String invalidMessage = getRandomMessage(invalidAmountMessages);
                invalidMessage = invalidMessage.replace("{min}", String.valueOf(minAmount))
                                             .replace("{max}", String.valueOf(maxAmount));
                player.sendMessage(invalidMessage);
                return;
            }
            
            int stock = getCurrentStock();
            if (amount > stock) {
                List<String> insufficientStock = getConfigMessages("dealer-insufficient-stock");
                String stockMessage = getRandomMessage(insufficientStock);
                stockMessage = stockMessage.replace("{stock}", String.valueOf(stock));
                player.sendMessage(stockMessage);
                return;
            }
            
            int price = getWeedSeedPrice();
            int totalCost = amount * price;
            
            // Check of speler genoeg geld heeft
            if (!VaultUtil.getEconomy().has(player, totalCost)) {
                List<String> insufficientFunds = getConfigMessages("dealer-insufficient-funds");
                String fundsMessage = getRandomMessage(insufficientFunds);
                fundsMessage = fundsMessage.replace("{player_money}", String.valueOf(VaultUtil.getEconomy().getBalance(player)));
                player.sendMessage(fundsMessage);
                return;
            }
            
            // Store current purchase details
            UUID playerId = player.getUniqueId();
            currentPurchaseAmounts.put(playerId, amount);
            currentPurchaseCosts.put(playerId, totalCost);
            
            // Bevestig aankoop
            activeConversations.put(playerId, ConversationState.CONFIRMING_PURCHASE);
            List<String> confirmMessages = getConfigMessages("dealer-confirm-purchase");
            String confirmMessage = getRandomMessage(confirmMessages);
            confirmMessage = confirmMessage.replace("{amount}", String.valueOf(amount))
                                         .replace("{total_cost}", String.valueOf(totalCost));
            player.sendMessage(confirmMessage);
            
        } catch (NumberFormatException e) {
            List<String> invalidNumberMessages = getConfigMessages("dealer-invalid-number");
            String invalidMessage = getRandomMessage(invalidNumberMessages);
            invalidMessage = invalidMessage.replace("{min}", String.valueOf(minAmount))
                                         .replace("{max}", String.valueOf(maxAmount));
            player.sendMessage(invalidMessage);
        }
    }
    
    /**
     * Handle purchase confirmation.
     */
    private void handlePurchaseConfirmation(Player player, String message) {
        // Toon speler bericht in chat
        player.sendMessage("§e[" + player.getName() + "] §f" + message);
        
        UUID playerId = player.getUniqueId();
        Integer currentAmount = currentPurchaseAmounts.get(playerId);
        Integer currentTotalCost = currentPurchaseCosts.get(playerId);
        
        if (currentAmount == null || currentTotalCost == null) {
            endConversation(player, null);
            return;
        }
        
        if (message.equalsIgnoreCase("ja") || message.equalsIgnoreCase("yes") || 
            message.equalsIgnoreCase("bevestig") || message.equalsIgnoreCase("confirm")) {
            
            // Process payment automatically
            processPayment(player);
        } else if (message.equalsIgnoreCase("nee") || message.equalsIgnoreCase("no")) {
            // Annuleer aankoop
            List<String> cancelledMessages = getConfigMessages("dealer-purchase-cancelled");
            String cancelledMessage = getRandomMessage(cancelledMessages);
            player.sendMessage(cancelledMessage);
            endConversation(player, null);
        } else {
            // Vraag opnieuw om bevestiging
            List<String> confirmMessages = getConfigMessages("dealer-confirm-purchase");
            String confirmMessage = getRandomMessage(confirmMessages);
            confirmMessage = confirmMessage.replace("{amount}", String.valueOf(currentAmount))
                                         .replace("{total_cost}", String.valueOf(currentTotalCost));
            player.sendMessage(confirmMessage);
        }
    }
    
    /**
     * Process payment automatically.
     */
    private void processPayment(Player player) {
        UUID playerId = player.getUniqueId();
        Integer currentAmount = currentPurchaseAmounts.get(playerId);
        Integer currentTotalCost = currentPurchaseCosts.get(playerId);
        
        if (currentAmount == null || currentTotalCost == null) {
            endConversation(player, null);
            return;
        }
        
        try {
            // Check if player actually has the money
            if (!VaultUtil.getEconomy().has(player, currentTotalCost)) {
                // Player is trying to scam
                List<String> scamMessages = getConfigMessages("dealer-scam-detected");
                String scamMessage = getRandomMessage(scamMessages);
                scamMessage = scamMessage.replace("{total_cost}", String.valueOf(currentTotalCost));
                player.sendMessage(scamMessage);
                
                // End conversation after a short delay to show the scam message
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    endConversation(player, null);
                }, 40L); // 2 seconden
                return;
            }
            
            // Simulate player giving money using config message
            List<String> playerPaymentMessages = getConfigMessages("player-payment-confirmation");
            String playerPaymentMessage = getRandomMessage(playerPaymentMessages);
            playerPaymentMessage = playerPaymentMessage.replace("{player}", player.getName())
                                                      .replace("{total_cost}", String.valueOf(currentTotalCost));
            player.sendMessage(playerPaymentMessage);
            
            // Process payment
            VaultUtil.getEconomy().withdrawPlayer(player, currentTotalCost);
            
            // Give weed seeds
            nl.djorr.mtwwiet.item.CustomItems customItems = nl.djorr.mtwwiet.core.PluginContext.getInstance(plugin)
                .getService(nl.djorr.mtwwiet.item.CustomItems.class).orElse(null);
            
            if (customItems != null) {
                for (int i = 0; i < currentAmount; i++) {
                    player.getInventory().addItem(customItems.createWeedSeed());
                }
            }
            
            // Success message
            List<String> successMessages = getConfigMessages("dealer-payment-success");
            String successMessage = getRandomMessage(successMessages);
            successMessage = successMessage.replace("{amount}", String.valueOf(currentAmount));
            player.sendMessage(successMessage);
            
            activeConversations.put(playerId, ConversationState.COMPLETED);
            
            // End conversation after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                List<String> completedMessages = getConfigMessages("dealer-conversation-completed");
                String completedMessage = getRandomMessage(completedMessages);
                player.sendMessage(completedMessage);
                
                // Voeg spatie toe na het completed bericht
                player.sendMessage("");
                endConversation(player, null);
            }, 40L); // 2 seconden
            
        } catch (Exception e) {
            // Error handling
            List<String> errorMessages = getConfigMessages("dealer-payment-error");
            String errorMessage = getRandomMessage(errorMessages);
            player.sendMessage(errorMessage);
            
            // End conversation after error
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                endConversation(player, null);
            }, 40L); // 2 seconden
        }
    }
    
    /**
     * End conversation.
     */
    public void endConversation(Player player, String endMessageKey) {
        UUID playerId = player.getUniqueId();
        
        // Remove from all conversation maps
        activeConversations.remove(playerId);
        currentPurchaseAmounts.remove(playerId);
        currentPurchaseCosts.remove(playerId);
        
        // Log for debugging
        plugin.getLogger().info("Ended conversation for player: " + player.getName() + " (UUID: " + playerId + ")");
        
        if (endMessageKey != null) {
            // Gebruik config berichten
            switch (endMessageKey) {
                case "dealer.conversation.cancelled":
                    List<String> cancelledMessages = getConfigMessages("dealer-purchase-cancelled");
                    player.sendMessage(getRandomMessage(cancelledMessages));
                    break;
                case "dealer.conversation.left-range":
                    List<String> leftRangeMessages = getConfigMessages("dealer-left-range");
                    player.sendMessage(getRandomMessage(leftRangeMessages));
                    break;
                case "dealer.conversation.already-in-conversation":
                    List<String> alreadyInMessages = getConfigMessages("dealer-already-in-conversation");
                    player.sendMessage(getRandomMessage(alreadyInMessages));
                    break;
            }
        }
    }
    
    /**
     * Check of speler nog in conversatie is.
     */
    public boolean isInConversation(Player player) {
        return activeConversations.containsKey(player.getUniqueId());
    }
    
    /**
     * Check of speler nog in range is van dealer.
     */
    private boolean isPlayerInRange(Player player) {
        // Dit zou je kunnen implementeren door de dealer locatie op te slaan
        // Voor nu returnen we true
        return true;
    }
    
    /**
     * Haal config messages op.
     */
    private List<String> getConfigMessages(String key) {
        ConfigManager configManager = PluginContext.getInstance(plugin).getService(ConfigManager.class).orElse(null);
        if (configManager != null) {
            return configManager.getConfig().getStringList("npc-dealer.messages." + key);
        }
        return Arrays.asList("Default message");
    }
    
    /**
     * Kies random message uit lijst.
     */
    private String getRandomMessage(List<String> messages) {
        if (messages.isEmpty()) return "Default message";
        return messages.get(random.nextInt(messages.size()));
    }
    
    /**
     * Haal weed seed price op uit config.
     */
    private int getWeedSeedPrice() {
        return plugin.getConfig().getInt("npc-dealer.prices.weed-seed", 50);
    }
    
    /**
     * Get random weed seed price between min and max.
     */
    private int getRandomWeedSeedPrice() {
        return random.nextInt(maxPricePerSeed - minPricePerSeed + 1) + minPricePerSeed;
    }
    
    /**
     * Get random seed amount between min and max.
     */
    private int getRandomSeedAmount() {
        return random.nextInt(maxSeedAmount - minSeedAmount + 1) + minSeedAmount;
    }
    
    /**
     * Haal current stock op uit config.
     */
    private int getCurrentStock() {
        return plugin.getConfig().getInt("npc-dealer.stock.max-weed-seeds", 100);
    }
    
    /**
     * End all active conversations.
     */
    public void endAllConversations() {
        activeConversations.clear();
    }
    
    /**
     * Send random dealer message.
     */
    public void sendRandomDealerMessage(Player player, String messageType) {
        List<String> messages = getConfigMessages(messageType);
        String message = getRandomMessage(messages);
        player.sendMessage(message);
    }
    
    /**
     * Send random player message.
     */
    public void sendRandomPlayerMessage(Player player, String messageType) {
        List<String> messages = getConfigMessages(messageType);
        String message = getRandomMessage(messages);
        player.sendMessage(message);
    }
} 