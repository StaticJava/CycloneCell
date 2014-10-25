package com.denialmc.cyclonesell;

import java.util.HashMap;

import org.bukkit.Material;

public class SellShop {

	private String sellTitle;
	private String priceTitle;
    private String buyTitle;
	private int slots;
	private HashMap<Material, Double> prices;
    private HashMap<Material, Double> buyPrices;
	
	public SellShop(String sellTitle, String priceTitle, String buyTitle, int slots, HashMap<Material, Double> prices, HashMap<Material, Double> buyPrices) {
		this.sellTitle = sellTitle;
		this.priceTitle = priceTitle;
        this.buyTitle = buyTitle;
		this.slots = slots;
		this.prices = prices;
        this.buyPrices = buyPrices;
	}
	
	public String getSellTitle() {
		return sellTitle;
	}
	
	public String getPriceTitle() {
        return priceTitle;
    }

    public String getBuyTitle() {
        return buyTitle;
    }
	
	public int getSlots() {
		return slots;
	}
	
	public HashMap<Material, Double> getPrices() {
		return prices;
	}

    public HashMap<Material, Double> getBuyPrices() {
        return buyPrices;
    }
	
	public Double getPrice(Material material) {
		return prices.get(material);
	}
}