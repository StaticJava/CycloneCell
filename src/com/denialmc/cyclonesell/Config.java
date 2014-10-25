package com.denialmc.cyclonesell;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

	private static String reloadMessage;
	private static String noPermMessage;
	private static String sellMessage;
	private static String priceMessage;
    private static String buyMessage;
	private static String sellSignHeader;
	private static String rawSellSignHeader;
	private static String priceSignHeader;
	private static String rawPriceSignHeader;
    private static String buySignHeader;
    private static String rawBuySignHeader;
    private static String noMoneyMessage;
    private static String noItemsInPriceGUI;
	private static HashMap<String, SellShop> sellShops = new HashMap<>();

	public static void reloadValues(FileConfiguration config) {
		reloadMessage = Utils.color(config.getString("general.reloadMessage"));
		noPermMessage = Utils.color(config.getString("general.noPermMessage"));
		sellMessage = Utils.color(config.getString("general.sellMessage"));
		priceMessage = Utils.color(config.getString("general.priceMessage"));
        buyMessage = Utils.color(config.getString("general.buyMessage"));
		sellSignHeader = Utils.color(config.getString("settings.sellSignHeader"));
		priceSignHeader = Utils.color(config.getString("settings.priceSignHeader"));
        buySignHeader = Utils.color(config.getString("settings.buySignHeader"));
		rawSellSignHeader = Utils.stripColor(sellSignHeader);
		rawPriceSignHeader = Utils.stripColor(priceSignHeader);
        rawBuySignHeader = Utils.stripColor(buySignHeader);
        noMoneyMessage = Utils.color(config.getString("general.noMoneyMessage"));
        noItemsInPriceGUI = Utils.color(config.getString("general.noItemsInPriceGUI"));
		
		loadSellShops(config);
	}
	
	public static void loadSellShops(FileConfiguration config) {
		sellShops.clear();
		
		ConfigurationSection section = config.getConfigurationSection("settings.sellShops");
		
		if (section != null) {
			for (String shop : section.getKeys(false)) {
				ConfigurationSection shopSection = section.getConfigurationSection(shop);
				String sellTitle = Utils.color(shopSection.getString("sellTitle", ""));
				String priceTitle = Utils.color(shopSection.getString("priceTitle", ""));
                String buyTitle = Utils.color(shopSection.getString("buyTitle", ""));
				int slots = Utils.limitNumber(shopSection.getInt("rows") * 9, 54);
				
				if (!sellTitle.isEmpty() && !priceTitle.isEmpty() && !buyTitle.isEmpty() && slots > 0) {
					ConfigurationSection priceSection = shopSection.getConfigurationSection("prices");
                    ConfigurationSection buyPriceSection = shopSection.getConfigurationSection("buyPrices");
					
					if (priceSection != null) {
						HashMap<Material, Double> prices = new HashMap<>();
                        HashMap<Material, Double> buyPrices = new HashMap<>();
						
						for (String materialName : priceSection.getKeys(false)) {
							Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_"));
							
							if (material != null && material != Material.AIR) {
								double price = priceSection.getDouble(materialName);
								
								prices.put(material, price);
							}
						}

                        for (String materialName : buyPriceSection.getKeys(false)) {
                            Material material = Material.getMaterial(materialName.toUpperCase().replace(" ", "_"));

                            if (material != null && material != Material.AIR) {
                                double price = buyPriceSection.getDouble(materialName);

                                buyPrices.put(material, price);
                            }
                        }
						
						if (!prices.isEmpty()) {
							sellShops.put(shop, new SellShop(sellTitle, priceTitle, buyTitle, slots, prices, buyPrices));
						}
					}
				}
			}
		}
	}

	public static String getReloadMessage() {
		return reloadMessage;
	}

	public static String getNoPermMessage() {
		return noPermMessage;
	}
	
	public static String getSellMessage() {
		return sellMessage;
	}
	
	public static String getPriceMessage() {
		return priceMessage;
	}

    public static String getBuyMessage() {
        return buyMessage;
    }

	public static String getSellSignHeader() {
		return sellSignHeader;
	}

	public static String getRawSellSignHeader() {
		return rawSellSignHeader;
	}

	public static String getPriceSignHeader() {
		return priceSignHeader;
	}

	public static String getRawPriceSignHeader() {
		return rawPriceSignHeader;
	}

    public static String getBuySignHeader() {
        return buySignHeader;
    }

    public static String getRawBuySignHeader() {
        return rawBuySignHeader;
    }

    public static String getNoItemsInPriceGUI() {
        return noItemsInPriceGUI;
    }
	
	public static HashMap<String, SellShop> getSellShops() {
		return sellShops;
	}

    public static String getNoMoneyMessage() {
        return noMoneyMessage;
    }
	
	public static SellShop getSellShop(String name) {
		return sellShops.get(name);
	}
}