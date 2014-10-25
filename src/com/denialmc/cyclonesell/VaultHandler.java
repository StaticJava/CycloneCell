package com.denialmc.cyclonesell;

import net.milkbowl.vault.economy.Economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler {

	private static Economy economy;
	
	public static boolean setupEconomy() {
		RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
		
		if (service != null) {
			economy = service.getProvider();
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean hasEconomy() {
		return economy != null;
	}
	
	public static Economy getEconomy() {
		return economy;
	}
	
	public static void depositPlayer(String player, double amount) {
		economy.depositPlayer(player, amount);
	}
    
    public static void withdrawPlayer(Player player, double amount, ItemStack itemStack, int number) {
        EconomyResponse response = economy.withdrawPlayer(player.getName(), amount * number);

        if (response.transactionSuccess()) {
            player.sendMessage(Config.getBuyMessage().replace("<value>", Utils.formatDouble(amount * number)));
            Material material = itemStack.getType();
            short durability = itemStack.getDurability();
            ItemStack newItemStack = new ItemStack(material, number, durability);
            player.getInventory().addItem(newItemStack);
        } else {
            player.sendMessage(Config.getNoMoneyMessage());
        }
    }
}