package com.comphenix.sneaky.cooldown;

public interface CooldownListenerSource {
	/**
	 * Add a given listener to this event source.
	 * @param listener - the listener to add.
	 */
	public void addCooldownListener(CooldownListener listener);
	
	/**
	 * Remove a given listener to the event source.
	 * @param listener - the listener to remove.
	 */
	public void removeCooldownListener(CooldownListener listener);
}
