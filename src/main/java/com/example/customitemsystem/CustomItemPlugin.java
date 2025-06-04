package com.example.customitemsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.customitemsystem.slayer.SlayerManager;

public class CustomItemPlugin extends JavaPlugin {

    private AbilityManager abilityManager;
    private SlayerManager slayerManager;

    @Override
    public void onEnable() {
        abilityManager = new AbilityManager(this);
        slayerManager = new SlayerManager(this);
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
        }
        return false;
    }
}
