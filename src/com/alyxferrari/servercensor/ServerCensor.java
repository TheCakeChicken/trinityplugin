package com.alyxferrari.servercensor;
import org.bukkit.plugin.java.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
public class ServerCensor extends JavaPlugin implements Listener {
	private ArrayList<PlayerOffense> offenses;
	private ArrayList<String> slurs;
	private ArrayList<PlayerKitState> hasKit;
	private ArrayList<Player> players;
	private String[] announcements = {
			"Remember to set your pronouns with /pronouns!",
			"You can use /report to report players who are not behaving appropriately, for example, bypassing the chat filter.",
			"Have you checked out the shop yet? You can use it anytime to buy valuable items with serotonin, our in-game currency. Try running /buy or /sell!",
			"Want to have your build protected from griefers like the village? Ask an admin, we'll be glad to help you!",
			"Want your building masterpiece on display? Ask an admin to apply to claim a plot in the village!",
			"Tell an admin if you have constructive criticism, a suggestion, or if you've found a bug on the server!",
			"Swearing is fine, but all slurs, including the T and Q slur, are not allowed on this server, as they are a trigger for some people.",
			"If you believe you've been muted or banned unfairly, contact an admin or moderator.",
			"If you accidentally declined the server resource pack, you can accept it if you edit the server on your server list.",
			"Did you know? There are four secret areas hidden throughout the village. Find them to claim 200 serotonin each!",
			"Use of tone indicators is highly encouraged! If you don't know what these are or need a refresher, check our Linktree.", 
			"Stuck in an area and can't get out? Try warping to the village with /warp spawn!"};
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, (this));
		offenses = new ArrayList<PlayerOffense>();
		slurs = new ArrayList<String>();
		hasKit = new ArrayList<PlayerKitState>();
		players = new ArrayList<Player>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("servercensor.cfg")));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(":");
				if (split.length == 2) {
					offenses.add(new PlayerOffense(split[0], Integer.parseInt(split[1])));
				} else {
					this.setEnabled(false);
					reader.close();
					return;
				}
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("slurs.cfg")));
			while ((line = reader.readLine()) != null) {
				slurs.add(line);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			this.setEnabled(false);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			this.setEnabled(false);
		}
		announcementThread();
	}
	@Override
	public void onDisable() {
		try {
			if (!new File("servercensor.cfg").delete()) {
				throw new IOException();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(("servercensor.cfg")));
			for (int i = 0; i < offenses.size(); i++) {
				writer.write(offenses.get(i).getUUID() + ":" + offenses.get(i).getOffenses());
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
	private void announcementThread() {
		new Thread() {
			@Override
			public void run() {
				while (isEnabled()) {
					try {
						Thread.sleep(1000*60*10);
						int index = (int) (Math.random()*(announcements.length-1));
						for (int i = 0; i < players.size(); i++) {
							players.get(i).sendMessage("\u00A72[\u00A7aANNOUNCEMENT\u00A72]: \u00A76" + announcements[index]);
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
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
		if (event.getMessage().contains("kit") && event.getPlayer().getWorld().getName().equalsIgnoreCase("pvp")) {
			for (int i = 0; i < hasKit.size(); i++) {
				if (event.getPlayer().getUniqueId().toString().equals(hasKit.get(i).getUUID())) {
					if (hasKit.get(i).hasKit()) {
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
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity().getWorld().getName().equals("pvp")) {
			for (int i = 0; i < hasKit.size(); i++) {
				if (event.getEntity().getUniqueId().toString().equals(hasKit.get(i).getUUID())) {
					hasKit.get(i).setHasKit(false);
					return;
				}
			}
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("pronouns")) {
			if (args.length < 1 || args.length > 2 || !args[0].contains("/")) {
				sender.sendMessage("\u00A7cOh no! Your pronouns were not set. Correct usage:\n/pronouns <type pronouns here> [alignment]\nFor example: /pronouns he/they masculine\nAlignment is optional and can be masculine, feminine, or nb");
				return true;
			}
			if (args.length == 1) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " permission set \"prefix.1.&r[&6" + args[0].toLowerCase() + "&r] \"");
				sender.sendMessage("\u00A7aPronouns set!");
				return true;
			} else if (args.length == 2) {
				String color = "&6";
				if (args[1].equalsIgnoreCase("masculine")) {
					color = "&b";
				} else if (args[1].equalsIgnoreCase("feminine")) {
					color = "&d";
				}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " permission set \"prefix.1.&r[" + color + args[0].toLowerCase() + "&r] \"");
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
					sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is help - display this usage sheet");
					return true;
				} else {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
					sender.sendMessage("\u00A7aSkyblock island created!");
					sender.sendMessage("\u00A7aRun \u00A7d/is go \u00A7ato teleport to your island.");
					return true;
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("go")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
					} else {
						sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is help - display this usage sheet");
					}
				} else if (args[0].equalsIgnoreCase("create")) {
					if (Files.isDirectory(Paths.get("skyblock_" + uuid))) {
						sender.sendMessage("\u00A7cYou already have a skyblock island. Are you sure you want to reset your island? Run \u00A76/is forcecreate\u00A7c to confirm.");
					} else {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
						sender.sendMessage("\u00A7aSkyblock island created!");
					}
				} else if (args[0].equalsIgnoreCase("forcecreate")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvdelete skyblock_" + uuid);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvclone skyblock skyblock_" + uuid);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + sender.getName() + " skyblock_" + uuid);
					sender.sendMessage("\u00A7aSkyblock island created!");
				} else {
					sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is help - display this usage sheet");
				}
				return true;
			}
			sender.sendMessage("\u00A7cCorrect usage:\n/is create - create a new island\n/is go - teleport to your island\n/is help - display this usage sheet");
			return true;
		}
		return false;
	}
}