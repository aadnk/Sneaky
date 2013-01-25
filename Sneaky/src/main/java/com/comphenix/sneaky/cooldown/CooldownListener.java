package com.comphenix.sneaky.cooldown;

import java.util.EventListener;

/**
 * Represents a listener that can recieve cooldown expire and changed events.
 * 
 * @author Kristian
 */
public interface CooldownListener extends EventListener {
	/**
	 * Invoked when the cooldown of a player has expired.
	 * <p>
	 * Note: This event is optional.
	 * @param event - the player whose cooldown has expired.
	 */
	public void cooldownExpired(CooldownExpiredEvent event);
	
	/**
	 * Invoked when the cooldown of a given player has changed.
	 * @param event - the 
	 */
	public void cooldownChanged(CooldownChangedEvent event);
}
