package com.comphenix.sneaky.cooldown;

/*
 *  Sneaky - A simple plugin that allows players to toggle automatic sneaking.
 *  Copyright (C) 2013 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

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
