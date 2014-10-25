package com.denialmc.cyclonesell;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Utils {

	private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
	public static String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	public static String stripColor(String string) {
		return ChatColor.stripColor(string);
	}
	
	public static int limitNumber(int number, int limit) {
		return number > limit ? limit : number;
	}
	
	public static String formatDouble(double value) {
		return decimalFormat.format(value);
	}
	
	public static ItemStack[] purifyItems(ItemStack[] items) {
		List<ItemStack> purified = new ArrayList<>();
	
		for (ItemStack item : items) {
			if (item != null) {
				purified.add(item);
			}
		}

        ItemStack[] itemStack = new ItemStack[purified.size()];
        itemStack = purified.toArray(itemStack);
		
		return itemStack;
	}
	
	public static Value calculateValue(ItemStack[] items, HashMap<Material, Double> rewards) {
        double value = 0.0;
        List<ItemStack> remains = new ArrayList<ItemStack>();

        for (ItemStack item : items) {
            if (item != null) {
                Material type = item.getType();
                Double reward = rewards.get(type);

                if (reward != null) {
                    value += (reward * item.getAmount());
                } else {
                    remains.add(item);
                }
            }
        }

        return new Value(value, remains);
    }

    public static Value calculateValue(HashMap<Material, Double> rewards, ItemStack... items) {
        double value = 0.0;
        List<ItemStack> remains = new ArrayList<ItemStack>();

        for (ItemStack item : items) {
            if (item != null) {
                Material type = item.getType();
                Double reward = rewards.get(type);

                if (reward != null) {
                    value += (reward * item.getAmount());
                } else {
                    remains.add(item);
                }
            }
        }

        return new Value(value, remains);
    }
}