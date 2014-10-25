package com.denialmc.cyclonesell;

public class ShopIdentifier {

	private ShopType shopType;
	private String name;
	
	public ShopIdentifier(ShopType shopType, String name) {
		this.shopType = shopType;
		this.name = name;
	}

	public ShopType getShopType() {
		return shopType;
	}

	public String getName() {
		return name;
	}
}