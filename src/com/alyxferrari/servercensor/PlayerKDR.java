package com.alyxferrari.servercensor;
public class PlayerKDR {
	private String uuid;
	private int kills;
	private int deaths;
	public PlayerKDR(String uuid, int kills, int deaths) {
		this.uuid = uuid;
		this.kills = kills;
		this.deaths = deaths;
	}
	public String getUUID() {
		return uuid;
	}
	public int getKills() {
		return kills;
	}
	public int getDeaths() {
		return deaths;
	}
	public float getKDR() {
		float kills = (float) this.kills;
		float deaths = (float) this.deaths;
		if (deaths < 1.0f) {
			deaths = 1.0f;
		}
		return kills / deaths;
	}
	public void incrementKills() {
		kills++;
	}
	public void incrementDeaths() {
		deaths++;
	}
}