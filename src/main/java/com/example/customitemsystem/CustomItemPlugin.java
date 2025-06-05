package com.example.customitemsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import com.example.customitemsystem.slayer.SlayerManager;
import com.example.customitemsystem.wardrobe.WardrobeManager;
import com.example.customitemsystem.AuctionHouse;
import com.example.customitemsystem.ManaManager;
import com.example.customitemsystem.stats.StatsManager;

public class CustomItemPlugin extends JavaPlugin {

    private AbilityManager abilityManager;
    private SlayerManager slayerManager;
    private ManaManager manaManager;
    private WardrobeManager wardrobeManager;
    private AuctionHouse auctionHouse;
    private com.example.customitemsystem.morph.MorphSetManager morphManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        statsManager = new StatsManager();
        manaManager = new ManaManager(this, statsManager);
        abilityManager = new AbilityManager(this, manaManager, statsManager);
        getCommand("addability").setTabCompleter(new AbilityTabCompleter());
        slayerManager = new SlayerManager(this);
        wardrobeManager = new WardrobeManager(this);
        auctionHouse = new AuctionHouse(this);
        morphManager = new com.example.customitemsystem.morph.MorphSetManager(this, statsManager);
    }

    @Override
    public void onDisable() {
        if (auctionHouse != null) {
            auctionHouse.saveData();
        }
        if (wardrobeManager != null) {
            wardrobeManager.saveAll();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("addability")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("Usage: /" + label + " <ability>");
                return true;
            }
            Ability ability = Ability.fromString(args[0]);
            if (ability == null) {
                sender.sendMessage("Unknown ability.");
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            abilityManager.addAbility(item, ability);
            sender.sendMessage("Added ability " + ability.name() + " to item.");
            return true;
        } else if (command.getName().equalsIgnoreCase("slayer")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            slayerManager.openMainMenu(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("armor")) {
            if (!(sender instanceof Player player)) return true;
            wardrobeManager.open(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("ah")) {
            if (!(sender instanceof Player player)) return true;
            auctionHouse.open(player, 0);
            return true;
        } else if (command.getName().equalsIgnoreCase("ahsell")) {
            if (!(sender instanceof Player player)) return true;
            if (args.length != 1) {
                player.sendMessage("Usage: /ahsell <price>");
                return true;
            }
            double price;
            try { 
                price = Double.parseDouble(args[0]); 
            } catch (NumberFormatException ex) { 
                player.sendMessage("Invalid price"); 
                return true; 
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            auctionHouse.listItem(player, item, price);
            return true;
        } else if (command.getName().equalsIgnoreCase("givemorph")) {
            if (!(sender instanceof Player player)) return true;
            for (com.example.customitemsystem.morph.MorphItem item : com.example.customitemsystem.morph.MorphItem.values()) {
                ItemStack stack = new ItemStack(item.getMaterial());
                org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(item.getDisplayName());
                    java.util.List<String> lore = new java.util.ArrayList<>();
                    lore.add(ChatColor.GRAY + "Requires Level " + item.getLevelReq());
                    lore.add("");
                    lore.add(ChatColor.LIGHT_PURPLE + "Morph Set Bonuses:");
                    lore.add(ChatColor.AQUA + "+5 All Skills");
                    lore.add(ChatColor.AQUA + "+8 Mana Regeneration /5s");
                    lore.add(ChatColor.AQUA + "+20% Walk Speed");
                    lore.add(ChatColor.AQUA + "+260 Health Regeneration");
                    lore.add(ChatColor.AQUA + "+182 Spell Damage");
                    lore.add(ChatColor.AQUA + "+176 Melee Damage");
                    lore.add("");
                    lore.add(ChatColor.GREEN + "Pieces: 0/" + com.example.customitemsystem.morph.MorphItem.values().length);
                    lore.add(ChatColor.GRAY + "Next bonus at 2 pieces");
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                }
                player.getInventory().addItem(stack);
            }
            morphManager.refreshPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Given Morph set items.");
            return true;
        } else if (command.getName().equalsIgnoreCase("skills")) {
            if (!(sender instanceof Player player)) return true;
            statsManager.showStats(player);
            return true;
        }
        return false;
    }
}
