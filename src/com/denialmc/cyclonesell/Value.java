package com.denialmc.cyclonesell;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Value {

	private double value;
	private List<ItemStack> remains;
	
	public Value(double value, List<ItemStack> remains) {
		this.value = value;
		this.remains = remains;
	}

	public double getValue() {
		return value;
	}

	public List<ItemStack> getRemains() {
		return remains;
	}
}