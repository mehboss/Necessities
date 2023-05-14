package me.mehboss.necessities;

/*
 * Mozilla Public License v2.0
 * 
 * Author: Mehboss
 * Copyright (c) 2023 Mehboss
 * Spigot: https://www.spigotmc.org/resources/authors/mehboss.139036/
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Weight implements Listener, CommandExecutor {

	/**
	 * Made by mehboss on 2/2/2017 TO DO: Create own data file for information
	 */

	HashMap<UUID, Location> location = new HashMap<UUID, Location>();

	private Bathroom plugin;

	public Weight(Bathroom plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (cmd.getName().equalsIgnoreCase("weight")) {
			if (sender instanceof Player) {

				Player player = (Player) sender;

				String weightlevel = plugin.customConfig.getString("Players." + player.getName() + ".Weight");
				String weighttotal = ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("Messages.Weight-Total").replaceAll("%AMOUNT%", weightlevel));
				String notenabled = ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("Messages.Not-Enabled"));

				if (!(plugin.getConfig().getString("Toggle.Weight-Enabled").equals("true"))) {
					message(notenabled, sender);
					return false;
				}

				if (!(player.hasPermission("necessities.weight"))) {
					String noPerm = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
							.getString("Messages.No-Perm").replaceAll("%PERMISSION%", "necessities.weight"));

					message(noPerm, sender);
					return false;
				}

				message(weighttotal, sender);
			}
		}
		return false;
	}

	public void message(String message, CommandSender p) {
		String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Prefix"));

		if (message.isEmpty()) {
			return;
		}
		if (prefix.isEmpty()) {
			prefix = "";
		}
		p.sendMessage(message.replaceAll("%prefix%", prefix));
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEat(PlayerItemConsumeEvent e) {

		Player player = e.getPlayer();

		int current = plugin.customConfig.getInt("Players." + player.getName() + ".Weight");
		int amount = plugin.getConfig().getInt("Items." + e.getItem().getType().name());

		if (plugin.getConfig().getString("Items." + e.getItem().getType().name()) == null) {
			return;
		}

		if (!(plugin.getConfig().getString("Toggle.Weight-Enabled").equals("true"))) {
			return;
		}

		String amountstring = plugin.getConfig().getString("Items." + e.getItem().getType().name());
		String MaxWeight = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("Messages.Max-Weight"));
		String PredictMaxWeight = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("Messages.Predict-Weight-Increase"));
		String WeightIncrease = ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
				.getString("Messages.Weight-Increase").replaceAll("%AMOUNT%", amountstring).toString());

		if (e.getItem().getType() == Material.POTION || e.getItem().getType() == Material.MILK_BUCKET) {
			return;
		}

		if (plugin.customConfig.getString("Players." + player.getName() + ".Weight").equals("100")) {
			e.setCancelled(true);
			message(MaxWeight, player);

		} else {

			if (plugin.getConfig().getString("Items." + e.getItem().getTypeId()) == null) {

				int predict = current + amount;

				if (predict > 100) {
					message(PredictMaxWeight, player);
					e.setCancelled(true);

				} else {
					message(WeightIncrease, player);
					plugin.customConfig.set("Players." + player.getName() + ".Weight", current + amount);
					plugin.saveCustomYml(plugin.customConfig, plugin.customYml);
				}
			} else {

				int predict = current + amount;

				if (predict > 100) {
					message(PredictMaxWeight, player);
					e.setCancelled(true);

				} else {
					message(WeightIncrease, player);
					plugin.customConfig.set("Players." + player.getName() + ".Weight", current + amount);
					plugin.saveCustomYml(plugin.customConfig, plugin.customYml);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {

		Player player = e.getPlayer();

		int current = plugin.customConfig.getInt("Players." + player.getName() + ".Weight");
		int amount = plugin.getConfig().getInt("Blocks-Walked.Weight-Removed");

		if (plugin.getConfig().getString("Toggle.Weight-Enabled").equals("true")) {
			if (!(plugin.customConfig.getString("Players." + player.getName() + ".Weight").equals("0"))) {
				if (!(plugin.getConfig().getString("Toggle.Weight-Enabled").equals("false"))) {
					Location lastloc = location.get(player.getUniqueId());
					int blocks = plugin.getConfig().getInt("Blocks-Walked.Walked-Blocks");

					if (location.get(player.getUniqueId()) == null || player.getLocation().distance(lastloc) > blocks) {
						plugin.customConfig.set("Players." + player.getName() + ".Weight", current - amount);
						location.put(player.getUniqueId(), player.getLocation());
						plugin.saveCustomYml(plugin.customConfig, plugin.customYml);

					}

					if (plugin.customConfig.getInt("Players." + player.getName() + ".Weight") >= 67
							&& plugin.customConfig.getInt("Players." + player.getName() + ".Weight") <= 100) {

						player.removePotionEffect(PotionEffectType.SLOW);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2), true);

					} else if (plugin.customConfig.getInt("Players." + player.getName() + ".Weight") >= 33
							&& plugin.customConfig.getInt("Players." + player.getName() + ".Weight") <= 66) {

						player.removePotionEffect(PotionEffectType.SLOW);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1), true);

					} else if (plugin.customConfig.getInt("Players." + player.getName() + ".Weight") > 0
							&& plugin.customConfig.getInt("Players." + player.getName() + ".Weight") <= 32) {

						player.removePotionEffect(PotionEffectType.SLOW);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0), true);

					}
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (location.get(p.getUniqueId()) != null) {
			location.put(p.getUniqueId(), null);

		}

		plugin.saveCustomYml(plugin.customConfig, plugin.customYml);
	}
}
