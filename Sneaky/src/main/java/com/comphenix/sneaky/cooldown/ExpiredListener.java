package com.comphenix.sneaky.cooldown;

import java.util.EventListener;

public interface ExpiredListener extends EventListener {
	/**
	 * Invoked when the cooldown of a player has expired.
	 * @param event - the player whose cooldown has expired.
	 */
	public void cooldownExpired(ExpiredEvent event);
}
