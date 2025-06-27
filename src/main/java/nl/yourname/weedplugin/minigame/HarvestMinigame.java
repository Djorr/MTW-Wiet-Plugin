package nl.yourname.weedplugin.minigame;

import nl.yourname.weedplugin.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import nl.yourname.weedplugin.WeedPlugin;
import nl.yourname.weedplugin.listener.PlantListener;
import nl.yourname.weedplugin.manager.PlantManager;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import nl.yourname.weedplugin.model.PlantData;
import nl.yourname.weedplugin.minigame.IHarvestMinigame;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Oogst-minigame: klik op alle groene wool binnen de tijd.
 */
public class HarvestMinigame implements IHarvestMinigame {
    private static final int SIZE = 27;
    private static final int GREEN_COUNT = 5;
    private static final int RED_COUNT = 4;

    private final Player player;
    private final Inventory inv;
    private final Set<Integer> greenSlots = new HashSet<>();
    private int timeLeft;
    private BukkitRunnable timerTask;
    private final PlantData plantData;
    private final org.bukkit.Location plantBlockLoc;
    private boolean finished = false;

    public HarvestMinigame(Player player, PlantData plantData) {
        this.player = player;
        this.plantData = plantData;
        this.inv = Bukkit.createInventory(null, SIZE, MessageUtil.getMessage("harvesting.inventory-title"));
        this.timeLeft = 15 * (60 * 20); // 15 seconds
        if (plantData.stand != null) {
            this.plantBlockLoc = plantData.stand.getLocation();
        } else if (plantData.hologram != null) {
            this.plantBlockLoc = plantData.hologram.getLocation().clone().subtract(0.5, 1, 0.5).getBlock().getLocation();
        } else {
            this.plantBlockLoc = player.getLocation(); // fallback
        }
        setupInventory();
        open();
    }

    private void setupInventory() {
        java.util.List<Integer> slots = new java.util.ArrayList<>();
        for (int i = 0; i < SIZE; i++) slots.add(i);
        java.util.Collections.shuffle(slots);
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, createGlass((byte)14, MessageUtil.getMessage("harvesting.wrong-block")));
        }
        for (int i = 0; i < GREEN_COUNT; i++) {
            int slot = slots.remove(0);
            inv.setItem(slot, createGlass((byte)5, MessageUtil.getMessage("harvesting.correct-block")));
            greenSlots.add(slot);
        }
    }

    private ItemStack createGlass(byte color, String name) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(name);
        glass.setItemMeta(meta);
        return glass;
    }

    private void open() {
        player.openInventory(inv);
        startTimer();
    }

    private void startTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished) {
                    cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    fail();
                    cancel();
                    return;
                }
                timeLeft--;
            }
        };
        timerTask.runTaskTimer(WeedPlugin.getPlugin(WeedPlugin.class), 0L, 20L);
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        // Altijd cancellen, geen item movement mogelijk
        event.setCancelled(true);
        // Alleen clicks in het minigame-inventory zelf verwerken
        if (!event.getInventory().equals(inv) || finished) return;
        if (event.getClickedInventory() != event.getInventory()) return;
        int slot = event.getRawSlot();
        if (greenSlots.contains(slot)) {
            inv.setItem(slot, null);
            greenSlots.remove(slot);
            if (greenSlots.isEmpty()) {
                success();
            }
        } else if (slot >= 0 && slot < SIZE) {
            fail();
        }
    }

    private void success() {
        if (finished) return;
        finished = true;
        timerTask.cancel();
        if (plantData.hologram != null) plantData.hologram.delete();
        if (plantData.growthTask != null) plantData.growthTask.cancel();
        if (plantData.oogstHologram != null) { plantData.oogstHologram.delete(); plantData.oogstHologram = null; }
        player.closeInventory();
        player.sendMessage(MessageUtil.getMessage("harvesting.success"));
        // Geef enchanted lilypad (ID 111)
        ItemStack weed = new ItemStack(Material.WATER_LILY);
        ItemMeta meta = weed.getItemMeta();
        meta.setDisplayName(MessageUtil.getMessage("items.weed-name"));
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        weed.setItemMeta(meta);
        player.getInventory().addItem(weed);
        // Verwijder plant uit manager en blok
        org.bukkit.block.Block block = plantData.plantBlockLocation.getWorld().getBlockAt(plantData.plantBlockLocation);
        block.getWorld().playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, block.getType());
        // Verwijder altijd beide blokken (onderste en bovenste), ongeacht type
        block.setType(org.bukkit.Material.AIR);
        org.bukkit.block.Block topBlock = block.getRelative(0, 1, 0);
        topBlock.setType(org.bukkit.Material.AIR);
        nl.yourname.weedplugin.manager.PlantManager.getInstance().removePlant(plantData.plantBlockLocation);
        // Verwijder oogstSpeler lock
        plantData.oogstSpeler = null;
    }

    private void fail() {
        if (finished) return;
        finished = true;
        timerTask.cancel();
        if (plantData.hologram != null) plantData.hologram.delete();
        if (plantData.growthTask != null) plantData.growthTask.cancel();
        if (plantData.oogstHologram != null) { plantData.oogstHologram.delete(); plantData.oogstHologram = null; }
        player.closeInventory();
        player.sendMessage(MessageUtil.getMessage("harvesting.failed"));
        // Block break animatie en verwijder blok
        org.bukkit.block.Block block = plantData.plantBlockLocation.getWorld().getBlockAt(plantData.plantBlockLocation);
        block.getWorld().playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, block.getType());
        // Verwijder altijd beide blokken (onderste en bovenste), ongeacht type
        block.setType(org.bukkit.Material.AIR);
        org.bukkit.block.Block topBlock = block.getRelative(0, 1, 0);
        topBlock.setType(org.bukkit.Material.AIR);
        nl.yourname.weedplugin.manager.PlantManager.getInstance().removePlant(plantData.plantBlockLocation);
        // Geef lichte schade
        player.damage(2.0);
    }

    @Override
    public void forceFail() {
        if (!finished) fail();
    }

    @Override
    public void forceCleanup() {
        if (timerTask != null) timerTask.cancel();
        if (plantData.hologram != null) plantData.hologram.delete();
        if (plantData.growthTask != null) plantData.growthTask.cancel();
    }
} 