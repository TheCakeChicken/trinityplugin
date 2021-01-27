package com.alyxferrari.servercensor;
public class PlayerKitState {
	private String uuid;
	private boolean kit;
	public PlayerKitState(String uuid, boolean kit) {
		this.uuid = uuid;
		this.kit = kit;
	}
	public String getUUID() {
		return uuid;
	}
	public boolean hasKit() {
		return kit;
	}
	public void setHasKit(boolean kit) {
		this.kit = kit;
	}
}