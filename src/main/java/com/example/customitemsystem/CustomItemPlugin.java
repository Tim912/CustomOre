package com.example.customitemsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.customitemsystem.slayer.SlayerManager;
import com.example.customitemsystem.ArmorMenu;
import com.example.customitemsystem.AuctionHouse;
import com.example.customitemsystem.ManaManager;

public class CustomItemPlugin extends JavaPlugin {

    private AbilityManager abilityManager;
    private SlayerManager slayerManager;
    private ManaManager manaManager;
    private ArmorMenu armorMenu;
    private AuctionHouse auctionHouse;

    @Override
    public void onEnable() {
        manaManager = new ManaManager(this);
        abilityManager = new AbilityManager(this, manaManager);
        slayerManager = new SlayerManager(this);
        armorMenu = new ArmorMenu(this, abilityManager);
        auctionHouse = new AuctionHouse(this);
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
            armorMenu.open(player, 0);
            return true;
        } else if (command.getName().equalsIgnoreCase("ah")) {
            if (!(sender instanceof Player player)) return true;
            auctionHouse.open(player, 0);
            return true;
        } else if (command.getName().equalsIgnoreCase("ahsell")) {
            if (!(sender instanceof Player player)) return true;
            ItemStack item = player.getInventory().getItemInMainHand();
            auctionHouse.listItem(player, item);
            return true;
        }
        return false;
    }
}
