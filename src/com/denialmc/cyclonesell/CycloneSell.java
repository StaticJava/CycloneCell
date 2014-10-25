package com.denialmc.cyclonesell;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CycloneSell extends JavaPlugin implements Listener {

	private static HashMap<String, ShopIdentifier> shops = new HashMap<String, ShopIdentifier>();
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		if (!VaultHandler.setupEconomy()) {
			getLogger().severe("Couldn't integrate with Vault.");
			getLogger().severe("Please make sure you have an economy plugin.");
			getLogger().severe("Disabling plugin.");
			
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		getServer().getPluginManager().registerEvents(this, this);

        reloadConfig();

        getLogger().info("CycloneSell has been enabled.");
	}

    @Override
    public void onDisable() {
        this.saveConfig();

        getLogger().info("CycloneSell has been disabled.");
    }

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		Config.reloadValues(getConfig());
	}
	
	public static HashMap<String, ShopIdentifier> getShops() {
		return shops;
	}
	
	public static boolean isShopping(String uuid) {
		return shops.containsKey(uuid);
	}
	
	public static ShopIdentifier getShopIdentifier(String uuid) {
		return shops.get(uuid);
	}
	
	public static void setShopIdentifier(String uuid, ShopIdentifier identifier) {
		synchronized (shops) {
			shops.put(uuid, identifier);
		}
	}
	
	public static void removeShopIdentifier(String uuid) {
		if (isShopping(uuid)) {
			synchronized (shops) {
				shops.remove(uuid);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getOpenInventory().getType() == InventoryType.CREATIVE || player.getOpenInventory().getType() == InventoryType.CRAFTING) {
            if (event.hasBlock()) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Block block = event.getClickedBlock();

                    if (block.getState() instanceof Sign) {
                        Sign sign = (Sign) block.getState();
                        String header = sign.getLine(0);
                        String name = sign.getLine(3);

                        if (header.equals(Config.getSellSignHeader())) {
                            if (player.hasPermission("cyclonesell.sign.use.sell")) {
                                SellShop shop = Config.getSellShop(name);

                                if (shop != null) {
                                    String uuid = player.getUniqueId().toString();
                                    Inventory inventory = getServer().createInventory(null, shop.getSlots(), shop.getSellTitle());

                                    event.setCancelled(true);
                                    shops.put(uuid, new ShopIdentifier(ShopType.SELL, name));
                                    player.openInventory(inventory);
                                }
                            }
                        } else if (header.equals(Config.getPriceSignHeader())) {
                            if (player.hasPermission("cyclonesell.sign.use.price")) {
                                SellShop shop = Config.getSellShop(name);

                                if (shop != null) {
                                    String uuid = player.getUniqueId().toString();
                                    Inventory inventory = getServer().createInventory(null, shop.getSlots(), shop.getPriceTitle());

                                    event.setCancelled(true);
                                    shops.put(uuid, new ShopIdentifier(ShopType.PRICE, name));
                                    player.openInventory(inventory);
                                }
                            }
                        } else if (header.equals(Config.getBuySignHeader())) {
                            if (player.hasPermission("cyclonesell.sign.use.buy")) {
                                String uuid = player.getUniqueId().toString();
                                int slots = Utils.limitNumber(getConfig().getInt("settings.sellShops." + name + ".buyRows") * 9, 54);
                                String title = Utils.color(getConfig().getString("settings.sellShops." + name + ".buyTitle"));
                                Inventory inventory = getServer().createInventory(null, slots, title);

                                event.setCancelled(true);
                                shops.put(uuid, new ShopIdentifier(ShopType.BUY, name));

                                ConfigurationSection section = this.getConfig().getConfigurationSection("settings.sellShops." + name + ".buyPrices");

                                int i = 0;

                                for (String key : section.getKeys(false)) {
                                    Material material = Material.getMaterial(key);
                                    ItemStack itemStack = new ItemStack(material);

                                    int cost = section.getInt(key);

                                    ItemMeta iMeta = itemStack.getItemMeta();
                                    iMeta.setLore(Arrays.asList(Utils.color("&aCost: &c$" + Integer.toString(cost))));

                                    itemStack.setItemMeta(iMeta);

                                    inventory.setItem(i, itemStack);

                                    i++;
                                }

                                player.openInventory(inventory);
                            }
                        }
                    }
                }
            }
        }
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			final Player player = (Player) event.getPlayer();
			String uuid = player.getUniqueId().toString();
			ShopIdentifier shop = getShopIdentifier(uuid);
			
			if (shop != null) {
				if (shop.getShopType() == ShopType.SELL) {
					SellShop sellShop = Config.getSellShop(shop.getName());
					
					if (sellShop != null) {
						Value value = Utils.calculateValue(event.getInventory().getContents(), sellShop.getPrices());
						double amount = value.getValue();
						List<ItemStack> remains = value.getRemains();
						
						if (!remains.isEmpty()) {
							Location location = player.getLocation();
							World world = location.getWorld();
							
							for (ItemStack item : player.getInventory().addItem(remains.toArray(new ItemStack[remains.size()])).values()) {
								world.dropItemNaturally(location, item); 
							}

                            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                                @Override
                                public void run() {
                                    player.updateInventory();
                                }
                            }, 20L);
						}
						
						if (amount > 0.0) {
							VaultHandler.depositPlayer(player.getName(), amount);
							player.sendMessage(Config.getSellMessage().replace("<value>", Utils.formatDouble(amount)));
						}
					}
				} else if (shop.getShopType() == ShopType.PRICE) {
                    SellShop sellShop = Config.getSellShop(shop.getName());

                    if (sellShop != null) {
                        ItemStack[] items = Utils.purifyItems(event.getInventory().getContents());
                        double value = Utils.calculateValue(items, sellShop.getPrices()).getValue();

                        player.getInventory().addItem(items);

                        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
                        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                player.updateInventory();
                            }
                        }, 1L);

                        if (items.length == 0) {
                            player.sendMessage(Config.getNoItemsInPriceGUI());
                        } else {
                            player.sendMessage(Config.getPriceMessage().replace("<value>", Utils.formatDouble(value)));
                        }
                    }
                }
			}
			
			removeShopIdentifier(uuid);
		}
	}

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ClickType clickType = event.getClick();
            Player player = (Player) event.getWhoClicked();
            String uuid = player.getUniqueId().toString();
            ShopIdentifier shop = getShopIdentifier(uuid);

            if (shop != null) {
                if (shop.getShopType() == ShopType.BUY) {
                    event.setCancelled(true);

                    SellShop sellShop = Config.getSellShop(shop.getName());

                    if (sellShop != null) {
                        Value value = Utils.calculateValue(sellShop.getBuyPrices(), event.getCurrentItem());
                        double amount = value.getValue();

                        int number;

                        if (clickType == ClickType.SHIFT_LEFT) {
                            number = 64;
                        } else {
                            number = event.getCurrentItem().getAmount();
                        }

                        if (amount > 0.0) {
                            VaultHandler.withdrawPlayer(player, amount, event.getCurrentItem(), number);
                        }
                    }
                }
            }
        }
    }

	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		String header = Utils.stripColor(event.getLine(0));
		Player player = event.getPlayer();
		
		if (header.equalsIgnoreCase(Config.getRawSellSignHeader())) {
			event.setLine(0, player.hasPermission("cyclonesell.sign.create.sell") ? Config.getSellSignHeader() : Config.getRawSellSignHeader());
		} else if (header.equalsIgnoreCase(Config.getRawPriceSignHeader())) {
			event.setLine(0, player.hasPermission("cyclonesell.sign.create.price") ? Config.getPriceSignHeader() : Config.getRawPriceSignHeader());
		} else if (header.equalsIgnoreCase(Config.getRawBuySignHeader())) {
            event.setLine(0, player.hasPermission("cyclonesell.sign.create.buy") ? Config.getBuySignHeader() : Config.getRawBuySignHeader());
        }
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String uuid = event.getPlayer().getUniqueId().toString();
		
		removeShopIdentifier(uuid);
	}

	public void sendHelpMessage(CommandSender sender) {
		sender.sendMessage("§6§lCYCLONESELL§f | §7/cyclonesell");
		sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
		sender.sendMessage("§2/cyclonesell help §f- §aGet command help");
		sender.sendMessage("§2/cyclonesell reload §f- §faReload the plugin");
		sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
	}
	
	@Override
  	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
  		if (commandLabel.equalsIgnoreCase("cyclonesell")) {
  			if (args.length == 0) {
  				sendHelpMessage(sender);
  			} else if (args[0].equalsIgnoreCase("reload")) {
  				if (sender.hasPermission("cyclonesell.reload")) {
  					reloadConfig();
  					sender.sendMessage(Config.getReloadMessage());
  				} else {
  					sender.sendMessage(Config.getNoPermMessage());
  				}
  			} else {
  				sendHelpMessage(sender);
  			}
  		}

  		return true;
  	}
}