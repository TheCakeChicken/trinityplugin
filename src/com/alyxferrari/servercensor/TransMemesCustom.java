package com.alyxferrari.servercensor;

import org.bukkit.plugin.java.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.enchantments.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TransMemesCustom extends JavaPlugin implements Listener {
	
	private ArrayList<PlayerOffense> offenses;
	private ArrayList<String> slurs;
	private ArrayList<PlayerKitState> hasKit;
	private ArrayList<Player> players;
	private ArrayList<PlayerKDR> kdr;
	
	private String[] announcements = {
			"Remember to set your pronouns with \u00A7d/pronouns\u00A76!",
			"You can use \u00A7d/report\u00A76 to report players who are not behaving appropriately, for example, bypassing the chat filter.",
			"Have you checked out the shop yet? You can use it anytime to buy valuable items with serotonin, our in-game currency. Try running \u00A7d/buy\u00A76 or \u00A7d/sell\u00A76!",
			"Want to have your build protected from griefers like the village? Message us on Discord, we'll be glad to help you!",
			"Want your building masterpiece on display? Message #plot-apps on the Discord server to apply to claim a plot in the village!",
			"Tell an admin if you have constructive criticism or a suggestion!",
			"Swearing is fine, but all slurs, including the T slur, are not allowed on this server, as they are a trigger for some people. ",
			"If you believe you've been muted or banned unfairly, message #ban-appeals on the Discord server.",
			"Use of tone indicators is highly encouraged! If you don't know what these are or need a refresher, check the trans__memes_ link in bio.",
			"Stuck in an area and can't get out? Try warping to the village with \u00A7d/warp spawn\u00A76.",
			"Did you know? We have skyblock! Run \u00A7d/island\u00A76 to create an island!",
			"Did you know? We have skyblock! Run \u00A7d/island\u00A76 to create an island!",
			"We have pride banners! Run \u00A7d/kit pride\u00A76 to receive them.",
			"Contact an admin if you think an item should be in the shop, but isn't!",
			"Everyone can redeem \u00A7d/kit starter\u00A76 once! It includes essential items for starting out in SMP.",
			"Withers are not allowed on the server. If you need a beacon, you can buy one in the shop.",
			"Think a moderator is abusing their power? Contact an admin or a different moderator on Discord.",
			"Try \u00A7d/kit daily\u00A76 and \u00A7d/kit monthly\u00A76 for items and serotonin!",
			"If you donate or have donated $5 or more to a charity or person in need, send us proof of donation and we'll give you the VIP rank, which includes access to \u00A7d/nick\u00A76 and particle trails!",
			"If you're in combat, don't spam your weapon! Java Edition has an attack cooldown, wait until the sword under your crosshair disappears before attacking again.",
			"We have a discord server! Check the trans__memes_ link in bio or ask someone for an invite link."
	};
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, (this));
		offenses = new ArrayList<PlayerOffense>();
		slurs = new ArrayList<String>();
		hasKit = new ArrayList<PlayerKitState>();
		players = new ArrayList<Player>();
		kdr = new ArrayList<PlayerKDR>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("servercensor.cfg")));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				String[] split = line.split(":");
				if (split.length == 2) {
					offenses.add(new PlayerOffense(split[0], Integer.parseInt(split[1])));
				}
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("slurs.cfg")));
			while ((line = reader.readLine()) != null) {
				slurs.add(line);
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("kdr.cfg")));
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				String[] split = line.split(":");
				if (split.length == 3) {
					kdr.add(new PlayerKDR(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2])));
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			this.setEnabled(false);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			this.setEnabled(false);
		}
		startThreads();
	}
	
	@Override
	public void onDisable() {
		try {
			if (!new File("servercensor.cfg").delete()) {
				throw new IOException();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("servercensor.cfg"));
			for (int i = 0; i < offenses.size(); i++) {
				writer.write(offenses.get(i).getUUID() + ":" + offenses.get(i).getOffenses() + "\n");
			}
			writer.close();
			if (!new File("kdr.cfg").delete()) {
				throw new IOException();
			}
			writer = new BufferedWriter(new FileWriter("kdr.cfg"));
			for (int i = 0; i < kdr.size(); i++) {
				writer.write(kdr.get(i).getUUID() + ":" + kdr.get(i).getKills() + ":" + kdr.get(i).getDeaths() + "\n");
			}
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		offenses = null;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		players.add(event.getPlayer());
		for (int i = 0; i < kdr.size(); i++) {
			if (kdr.get(i).getUUID().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
				return;
			}
		}
		kdr.add(new PlayerKDR(event.getPlayer().getUniqueId().toString(), 0, 0));
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "money create " + event.getPlayer().getName());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getUniqueId().toString().equals(event.getPlayer().getUniqueId().toString())) {
				players.remove(i);
				return;
			}
		}
	}
	
	private void startThreads() {
		new Thread() {
			@Override
			public void run() {
				int firstIndex = -1;
				int secondIndex = -1;
				int thirdIndex = -1;
				while (isEnabled()) {
					try {
						Thread.sleep(1000*60*8);
						int index = firstIndex;
						while (index == firstIndex || index == secondIndex || index == thirdIndex) {
							index = (int) (Math.random()*(announcements.length-1));
						}
						
						getServer().broadcastMessage("\u00A72[\u00A7aANNOUNCEMENT\u00A72]: \u00A76" + announcements[index]);
						
						firstIndex = secondIndex;
						secondIndex = thirdIndex;
						thirdIndex = index;
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				while (isEnabled()) {
					try {
						Thread.sleep(1000*60*5);
						for (int x = 0; x < players.size(); x++) {
							ItemStack[] contents = players.get(x).getInventory().getContents();
							for (int y = 0; y < contents.length; y++) {
								for (int z = 0; z < slurs.size(); z++) {
									try {
										if (contents[y].getItemMeta().getDisplayName().contains(slurs.get(z))) {
											contents[y].getItemMeta().setDisplayName("{slur removed}");
										}
									} catch (NullPointerException ex) {}
								}
							}
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				while (isEnabled()) {
					try {
						Thread.sleep(1000*60*5);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:kill @e[type=item]");
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	@EventHandler
	public void onPlayerChatted(AsyncPlayerChatEvent event) {
		if (checkSlur(event.getMessage())) {
			System.out.println("PLAYER SLUR WARNING: " + event.getPlayer().getName() + ": " + event.getMessage());
			event.setCancelled(true);
			incrementOffenses(event.getPlayer());
			punish(event.getPlayer());
		}
	}
	
	private boolean checkSlur(String toCheck) {
		for (int i = 0; i < slurs.size(); i++) {
			if (toCheck.contains(slurs.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	private void incrementOffenses(Player player) {
		for (int i = 0; i < offenses.size(); i++) {
			if (offenses.get(i).getUUID().equals(player.getUniqueId().toString())) {
				offenses.get(i).incrementOffenses();
				return;
			}
		}
		offenses.add(new PlayerOffense(player.getUniqueId().toString(), 1));
	}
	
	private void punish(Player player) {
		for (int i = 0; i < offenses.size(); i++) {
			PlayerOffense offense = offenses.get(i);
			if (offense.getUUID().equals(player.getUniqueId().toString())) {
				if (offense.getOffenses() >= 3) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getName() + " Slurs are not allowed on this server.\nDM @trans__memes_ to appeal this permanent ban.");
					// ban
				} else if (offense.getOffenses() >= 2) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + player.getName() + " 10m");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick " + player.getName() + " Slurs are not allowed on this server. Your next offense will result in an appealable permanent ban. You have been muted for 10 minutes.");
					// kick + mute
				} else if (offense.getOffenses() >= 1) {
					player.sendMessage("\u00A7cSlurs are not allowed on this server. Your message has not been sent. This is your first and only warning.");
					// warn
				}
			}
		}
	}
	
	@EventHandler
	public void onCommandPreprocessing(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase("pvp")) {
			if ((event.getMessage().startsWith("kit") || event.getMessage().startsWith("/kit")) && !event.getMessage().contains("kits")) {
				for (int i = 0; i < hasKit.size(); i++) {
					if (event.getPlayer().getUniqueId().toString().equals(hasKit.get(i).getUUID())) {
						if (hasKit.get(i).hasKit() && !event.getPlayer().getName().equalsIgnoreCase("alyxxx")) {
							event.setCancelled(true);
							event.getPlayer().sendMessage("\u00A74Error: \u00A7cYou cannot have two PvP kits at one time.");
							return;
						}
						hasKit.get(i).setHasKit(true);
						return;
					}
				}
				hasKit.add(new PlayerKitState(event.getPlayer().getUniqueId().toString(), true));
			}
		}
	}
	
	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		new Thread() {
			@Override
			public void run() {
				try {Thread.sleep(8000);} catch (InterruptedException ex) {}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear");
			}
		}.start();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getWorld().getName().equals("pvp")) {
			Player killer = getKiller(event);
			if (killer != null) {
				for (int i = 0; i < kdr.size(); i++) {
					if (kdr.get(i).getUUID().equalsIgnoreCase(event.getEntity().getUniqueId().toString())) {
						kdr.get(i).incrementDeaths();
						event.getEntity().sendMessage("\u00A7cYou were killed by \u00A7a" + killer.getName() + "\u00A7c.");
					}
					if (kdr.get(i).getUUID().equalsIgnoreCase(killer.getUniqueId().toString())) {
						kdr.get(i).incrementKills();
						killer.sendMessage("\u00A7aYou killed \u00A7c" + event.getEntity().getName() + "\u00A7a!");
						if ((kdr.get(i).getKills()+"").endsWith("00")) {
							killer.sendMessage("\u00A7aYou received $75 serotonin for achieving \u00A7c" + kdr.get(i).getKills() + " \u00A7akills!");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "money give " + killer.getName() + " 75");
						}
					}
				}
			}
			for (int i = 0; i < hasKit.size(); i++) {
				if (event.getEntity().getUniqueId().toString().equals(hasKit.get(i).getUUID())) {
					hasKit.get(i).setHasKit(false);
					return;
				}
			}
		}
	}
	
	public static Player getKiller(PlayerDeathEvent event) { // adapted from minecrell source
		EntityDamageEvent damage = event.getEntity().getLastDamageCause();
		if (damage != null && !damage.isCancelled() && damage instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
			if (damager instanceof Projectile) {
				LivingEntity entDamager = (LivingEntity) ((Projectile) damager).getShooter();
				if (entDamager != null && entDamager instanceof Player) {
					return (Player) entDamager;
				}
			}
			if (damager instanceof Player) {
				return (Player) damager;
			}
		}
		return null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("pronouns")) {
			if (args.length < 1 || args.length > 2 || !args[0].contains("/")) {
				sender.sendMessage("\u00A7cOh no! Your pronouns were not set. Correct usage:\n/pronouns <type pronouns here> [alignment]\nFor example: /pronouns he/they masculine\nAlignment is optional and can be masculine, feminine, or nb");
				return true;
			}
			if (args.length == 1) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta removeprefix 1");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta addprefix 1 \"&r[&6" + args[0].toLowerCase() + "&r] \"");
				sender.sendMessage("\u00A7aPronouns set!");
				return true;
			} else if (args.length == 2) {
				String color = "&6";
				if (args[1].equalsIgnoreCase("masculine")) {
					color = "&b";
				} else if (args[1].equalsIgnoreCase("feminine")) {
					color = "&d";
				}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta removeprefix 1");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta addprefix 1 \"&r[" + color + args[0].toLowerCase() + "&r] \"");
				sender.sendMessage("\u00A7aPronouns set!");
				return true;
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("report")) {
			if (args.length < 2) {
				sender.sendMessage("\u00A7cYour report was not filed. Correct usage:\n/report <username> <reason>\nFor example: /report alyxxx Being too sexy");
				return true;
			}
			int num = (int) (Math.random()*100000.0);
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/Alyx Ferrari/Desktop/report-" + num + ".txt"));
				String reason = "";
				for (int i = 1; i < args.length; i++) {
					reason += args[i] + " ";
				}
				writer.write("Report filer: " + sender.getName() + "\nReport subject: " + args[0] + "\nReason: " + reason);
				writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			sender.sendMessage("\u00A7aReport filed successfully!\nPlease refrain from filing multiple reports on the same person, it does not increase the likelyhood of the reportee being banned.");
			return true;
		} else if (command.getName().equalsIgnoreCase("spawn")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + sender.getName() + " warp spawn");
			return true;
		} else if (command.getName().equalsIgnoreCase("is") || command.getName().equalsIgnoreCase("island") || command.getName().equalsIgnoreCase("sb") || command.getName().equalsIgnoreCase("skyblock")) {
			String uuid = "";
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getName().equalsIgnoreCase(sender.getName())) {
					uuid = players.get(i).getUniqueId().toString();
				}
			}
			if (uuid.equals("")) {
				return true;
			}
			if (args.length == 0) {
				if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
					sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is delete - delete your island\n/is help - display this usage sheet");
				} else {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid + "_nether");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvcreate skyblock_" + uuid + "_nether nether");
					sender.sendMessage("\u00A7aSkyblock island created!");
					sender.sendMessage("\u00A7aRun \u00A7d/is go \u00A7ato teleport to your island.");
				}
				return true;
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("go")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + sender.getName() + " is");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("create")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						sender.sendMessage("\u00A7cYou already have a skyblock island. Are you sure you want to reset your island? Run \u00A76/is forcecreate\u00A7c to confirm.");
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid + "_nether");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvcreate skyblock_" + uuid + "_nether nether");
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
						sender.sendMessage("\u00A7aSkyblock island created!");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("forcecreate")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid + "_nether");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvcreate skyblock_" + uuid + "_nether nether");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
					sender.sendMessage("\u00A7aSkyblock island created!");
					return true;
				} else if (args[0].equalsIgnoreCase("delete")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						sender.sendMessage("\u00A7cAre you sure you want to delete your island? This action is irreversible.\nRun \u00A76/is forcedelete\u00A7c to confirm.");
					} else {
						sender.sendMessage("\u00A7cYou do not have a skyblock island.");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("forcedelete")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
						sender.sendMessage("\u00A7aSkyblock island deleted!");
					} else {
						sender.sendMessage("\u00A7cYou do not have a skyblock island.");
					}
					return true;
				} else {
					sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is delete - delete your island\n/is help - display this usage sheet");
				}
				return true;
			}
			sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is delete - delete your island\n/is help - display this usage sheet");
			return true;
		} else if (command.getName().equalsIgnoreCase("isnearest")) {
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getWorld().getName().equalsIgnoreCase("skyblocktp")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + players.get(i).getName() + " is go");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + players.get(i).getName() + " is go");
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("sell")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + sender.getName() + " sell:sell");
			return true;
		} else if (command.getName().equalsIgnoreCase("elytra")) {
			ItemStack elytra = new ItemStack(Material.ELYTRA);
			elytra.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			elytra.addUnsafeEnchantment(Enchantment.MENDING, 10);
			elytra.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getName().equalsIgnoreCase(sender.getName())) {
					players.get(i).getInventory().addItem(elytra);
					players.get(i).sendMessage("\u00A7cReminder: \u00A76This elytra is for moderators only and should never leave your inventory.");
					return true;
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("bow")) {
			ItemStack bow = new ItemStack(Material.BOW);
			bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemStack arrow = new ItemStack(Material.ARROW);
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getName().equalsIgnoreCase(sender.getName())) {
					players.get(i).getInventory().addItem(bow);
					players.get(i).getInventory().addItem(arrow);
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("crossbow")) {
			ItemStack crossbow = new ItemStack(Material.CROSSBOW);
			crossbow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemStack arrow = new ItemStack(Material.ARROW);
			for (int i = 0; i < players.size(); i++) {
				if (players.get(i).getName().equalsIgnoreCase(sender.getName())) {
					players.get(i).getInventory().addItem(crossbow);
					players.get(i).getInventory().addItem(arrow);
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("score")) {
			if (args.length == 0) {
				int index = -1;
				outer:
					for (int x = 0; x < players.size(); x++) {
						if (players.get(x).getName().equalsIgnoreCase(sender.getName())) {
							for (int y = 0; y < kdr.size(); y++) {
								if (kdr.get(y).getUUID().equalsIgnoreCase(players.get(x).getUniqueId().toString())) {
									index = y;
									break outer;
								}
							}
						}
					}
				if (index == -1) {
					return true;
				}
				sender.sendMessage("\u00A7aKitPvP stats for player \u00A7c" + sender.getName() + "\u00A7a:\nKills: \u00A7c" + kdr.get(index).getKills() + "\n\u00A7aDeaths: \u00A7c" + kdr.get(index).getDeaths() + "\n\u00A7aKDR: \u00A7c" + kdr.get(index).getKDR());
				return true;
			} else {
				int index = -1;
				outer:
				for (int x = 0; x < players.size(); x++) {
					if (players.get(x).getName().equalsIgnoreCase(args[0])) {
						for (int y = 0; y < kdr.size(); y++) {
							if (kdr.get(y).getUUID().equalsIgnoreCase(players.get(x).getUniqueId().toString())) {
								index = y;
								break outer;
							}
						}
					}
				}
				if (index == -1) {
					sender.sendMessage("\u00A7cThat player could not be found.");
					return true;
				}
				sender.sendMessage("\u00A7aKitPvP stats for player \u00A7c" + args[0] + "\u00A7a:\nKills: \u00A7c" + kdr.get(index).getKills() + "\n\u00A7aDeaths: \u00A7c" + kdr.get(index).getDeaths() + "\n\u00A7aKDR: \u00A7c" + kdr.get(index).getKDR());
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("trail") || command.getName().equalsIgnoreCase("trails")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sudo " + sender.getName() + " pp");
			return true;
		}
		return false;
	}
}
