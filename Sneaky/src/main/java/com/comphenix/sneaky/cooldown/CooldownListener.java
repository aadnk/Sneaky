package com.comphenix.sneaky.cooldown;

import java.util.EventListener;

public interface CooldownListener extends EventListener {
	/**
	 * Invoked when the cooldown of a player has expired.
	 * <p>
	 * This event is optional.
	 * @param event - the player whose cooldown has expired.
	 */
	public void cooldownExpired(CooldownExpiredEvent event);
	
	/**
	 * Invoked when the cooldown of a given player has changed.
	 * @param event - the 
	 */
	public void cooldownChanged(CooldownChangedEvent event);
}
