package com.alyxferrari.servercensor;
import org.bukkit.plugin.java.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import java.io.*;
import java.util.*;
public class ServerCensor extends JavaPlugin {
	private ArrayList<PlayerOffense> offenses;
	private ArrayList<String> slurs;
	@Override
	public void onEnable() {
		System.out.println(System.getProperty("user.dir"));
		offenses = new ArrayList<PlayerOffense>();
		slurs = new ArrayList<String>();
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
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		if (checkSlur(event.getMessage())) {
			event.setCancelled(false);
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
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getName() + " Slurs are not allowed on this server. DM @trans__memes_ to appeal this permanent ban.");
					// ban
				} else if (offense.getOffenses() >= 2) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + player.getName() + " 10m");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick " + player.getName() + " Slurs are not allowed on this server. Your next offense will result in a permanent ban. You have been muted for 10 minutes.");
					// kick + mute
				} else if (offense.getOffenses() >= 1) {
					player.sendMessage("\u00A7f[\u00A77Console\u00A7f] \u00A7cSlurs are not allowed on this server. Your message has not been sent. This is your first and only warning.");
					// warn
				}
			}
		}
	}
}