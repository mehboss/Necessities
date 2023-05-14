package me.mehboss.necessities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Bathroom extends JavaPlugin implements Listener {

	/**
	 * Made by mehboss on 2/2/2017 TO DO: Create own data file for information
	 */

	public ArrayList<String> waitingforBathroom = new ArrayList<String>();

	File customYml = new File(getDataFolder() + "/players.yml");
	FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customYml);

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new Weight(this), this);
		this.getCommand("weight").setExecutor(new Weight(this));

		registerConfig();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceHolder(this).register();
		}
	}

	public void saveCustomYml(FileConfiguration ymlConfig, File ymlFile) {
		try {
			ymlConfig.save(ymlFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		saveCustomYml(customConfig, customYml);
		reloadConfig();
	}

	private void registerConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		getLogger();
	}

	public void message(String message, CommandSender p) {

		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Necessities");
		String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Prefix"));

		if (message.isEmpty()) {
			return;
		}
		if (prefix.isEmpty()) {
			prefix = "";
		}

		p.sendMessage(message.replaceAll("%prefix%", prefix));
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (cmd.getName().equalsIgnoreCase("usebathroom")) {

			if (sender instanceof Player) {

				Player player = (Player) sender;

				String bladder = customConfig.getString("Players." + player.getName() + ".Bladder");

				String BathroomUse = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.Use-Bathroom"));
				String EmptyBladder = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.Bladder-Empty"));
				String CauldronStand = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.Not-On-Cauldron"));
				String notenabled = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.Not-Enabled"));
				String automaticuse = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.Use-Bathroom-Automatic"));

				String block = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Bathroom-Use.Block"));

				Location l = player.getLocation();
				Location d = new Location(l.getWorld(), l.getX(), l.getY() - 1, l.getZ());

				if (player.hasPermission("necessities.bathroom")) {

					if (getConfig().getString("Bathroom-Use.Automatic-Use").equals("true")) {
						message(automaticuse, player);
						return false;
					}

					if (!(getConfig().getString("Toggle.Bladder-Enabled").equals("true"))) {
						message(notenabled, player);
						return false;
					}

					if (d.getBlock().getType() == Material.matchMaterial(block.toUpperCase())) {

						if (!bladder.equals("0")) {
							message(BathroomUse, player);

							customConfig.set("Players." + player.getName() + ".Bladder", "0");
							saveCustomYml(customConfig, customYml);

							if (getConfig().getString("Bathroom-Use.Player-Effect").equals("true")) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 30, 1));
								player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
							}

							if (!(getConfig().getString("Bathroom-Use.Sound").equalsIgnoreCase("none"))) {
								player.playSound(player.getLocation(),
										Sound.valueOf(getConfig().getString("Bathroom-Use.Sound").toUpperCase()), 1, 1);
							}

						} else {
							message(EmptyBladder, player);
						}
					} else {
						message(CauldronStand, player);
					}
				} else {
					String noPerm = ChatColor.translateAlternateColorCodes('&', getConfig()
							.getString("Messages.No-Perm").replaceAll("%PERMISSION%", "necessities.usebathroom"));

					message(noPerm, player);
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("bladder")) {

			Player player = (Player) sender;
			String notenabled = ChatColor.translateAlternateColorCodes('&',
					getConfig().getString("Messages.Not-Enabled"));
			String bladderlevel = customConfig.getString("Players." + player.getName() + ".Bladder");
			String bladdertotal = ChatColor.translateAlternateColorCodes('&',
					getConfig().getString("Messages.Bladder-Total").replaceAll("%AMOUNT%", bladderlevel));

			if (player.hasPermission("necessities.bladder")) {

				if (!(getConfig().getString("Toggle.Bladder-Enabled").equals("true"))) {
					message(notenabled, player);
					return false;
				}

				if (player instanceof Player) {
					message(bladdertotal, player);
				}
			} else {

				String noPerm = ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("Messages.No-Perm").replaceAll("%PERMISSION%", "necessities.bladder"));

				message(noPerm, player);
			}
		} else if (cmd.getName().equalsIgnoreCase("necessities")) {

			if (args.length == 0 || !(args[0].equalsIgnoreCase("reload"))) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&8&l&m|-------------------------------------------|&r"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "   &c&lNecessities &8(&av2.6&8)"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "     &dAuthor: &cMehBoss"));
				sender.sendMessage(" ");
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "     &a&lCommands:"));
				sender.sendMessage(
						ChatColor.translateAlternateColorCodes('&', "       &8'&c/weight&8' &7- checks your weight."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"       &8'&c/bladder&8' &7- checks your bladder."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"       &8'&c/usebathroom&8' &7- uses the bathroom."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"       &8'&c/necessities&8' &7- shows this help page."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"       &8'&c/necessities reload&8' &7- reloads the plugin."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&8&l&m|-------------------------------------------|&r"));

				return false;
			}

			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("necessities.reload")) {

					reloadConfig();
					saveCustomYml(customConfig, customYml);
					message(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Reload")),
							sender);
				} else {
					String noPerm = ChatColor.translateAlternateColorCodes('&',
							getConfig().getString("Messages.No-Perm").replaceAll("%PERMISSION%", "necessities.reload"));

					message(noPerm, sender);
				}
			}
		}
		return false;

	}

	@EventHandler
	public void onEat(PlayerItemConsumeEvent e) {

		Player player = e.getPlayer();

		String amountstring = getConfig().getString("Potion-Drink.Thirst");

		int amount = getConfig().getInt("Potion-Drink.Thirst");
		int current = customConfig.getInt("Players." + player.getName() + ".Bladder");

		String FullBladder = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("Messages.Full-Bladder"));
		String PredictFullBladder = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("Messages.Predict-Bladder-Increase"));
		String BladderIncrease = ChatColor.translateAlternateColorCodes('&',
				getConfig().getString("Messages.Bladder-Increase").replaceAll("%AMOUNT%", amountstring).toString());

		if (e.getItem().getType() == Material.POTION || e.getItem().getType() == Material.MILK_BUCKET) {

			if (!(getConfig().getString("Toggle.Bladder-Enabled").equals("true"))) {
				return;
			}

			if (customConfig.getString("Players." + player.getName() + ".Bladder").equals("100")) {
				e.setCancelled(true);
				message(FullBladder, player);

			} else {

				int predict = current + amount;

				if (predict > 100) {
					message(PredictFullBladder, player);
					e.setCancelled(true);

				} else {
					message(BladderIncrease, player);
					customConfig.set("Players." + player.getName() + ".Bladder", current + amount);
					saveCustomYml(customConfig, customYml);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		if (getConfig().getString("Bathroom-Use.Automatic-Use").equals("true")
				&& (getConfig().getString("Toggle.Bladder-Enabled").equals("true"))) {

			String block = getConfig().getString("Bathroom-Use.Block");
			Location l = p.getLocation();
			Location d = new Location(l.getWorld(), l.getX(), l.getY() - 1, l.getZ());
			String bladder = customConfig.getString("Players." + p.getName() + ".Bladder");

			if (!bladder.equals("0")) {

				if (d.getBlock().getType() != Material.matchMaterial(block.toUpperCase())) {
					if (waitingforBathroom.contains(p.getName())) {
						waitingforBathroom.remove(p.getName());
						return;
					}
				}

				if (d.getBlock().getType() == Material.matchMaterial(block.toUpperCase())) {

					String BathroomUse = ChatColor.translateAlternateColorCodes('&',
							getConfig().getString("Messages.Use-Bathroom"));
					String sound = getConfig().getString("Bathroom-Use.Sound");
					String timed = ChatColor.translateAlternateColorCodes('&',
							getConfig().getString("Messages.Time").replaceAll("%time%",
									String.valueOf(getConfig().getInt("Bathroom-Use.Automatic-Use-Seconds"))));

					if (!(waitingforBathroom.contains(p.getName()))) {
						message(timed, p);
						waitingforBathroom.add(p.getName());
					}

					Long timeset = getConfig().getLong("Bathroom-Use.Automatic-Use-Seconds") * 20;

					new BukkitRunnable() {

						public void run() {

							if (waitingforBathroom.contains(p.getName())) {
								message(BathroomUse, p);
								waitingforBathroom.remove(p.getName());

								customConfig.set("Players." + p.getName() + ".Bladder", "0");
								saveCustomYml(customConfig, customYml);

								if (!(getConfig().getString("Bathroom-Use.Sound").equals("none"))) {
									p.playSound(p.getLocation(), Sound.valueOf(sound.toUpperCase()), 1, 1);
								}
							}
						}
					}.runTaskLater(Bukkit.getPluginManager().getPlugin("Necessities"), timeset);
				}
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		Player player = e.getPlayer();

		if (customConfig.getString("Players." + player.getName()) == null) {
			customConfig.createSection("Players." + player.getName() + ".Bladder");
			customConfig.set("Players." + player.getName() + ".Bladder", "0");
			customConfig.createSection("Players." + player.getName() + ".Weight");
			customConfig.set("Players." + player.getName() + ".Weight", "0");

			saveCustomYml(customConfig, customYml);
		}
	}
}