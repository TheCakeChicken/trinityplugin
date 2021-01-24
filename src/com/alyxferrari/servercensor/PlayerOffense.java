package com.alyxferrari.servercensor;
public class PlayerOffense {
	private String uuid;
	private int offenses;
	public PlayerOffense(String uuid, int offenses) {
		this.uuid = uuid;
		this.offenses = offenses;
	}
	public String getUUID() {
		return uuid;
	}
	public int getOffenses() {
		return offenses;
	}
	public void setOffenses(int offenses) {
		this.offenses = offenses;
	}
	public void incrementOffenses() {
		this.offenses++;
	}
}