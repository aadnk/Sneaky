package com.comphenix.sneaky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class AutoSneakers implements ConfigurationSerializable {
	// Where the list of sneaking players is stored
	private static final String SNEAKERS_KEY = "sneakers";
	
	// Store whether or not a player is auto sneaking or not
	private Set<String> autoSneakers;
	
	public AutoSneakers() {
		// Initialize empty list
		this(new HashSet<String>());
	}
	
	public AutoSneakers(Set<String> sneakers) {
		this.autoSneakers = sneakers;
	}
	
	public void setAutoSneaking(Player player, boolean value) {
		if (value) {
			autoSneakers.add(player.getName());
		} else {
			autoSneakers.remove(player.getName());
		}
	}
		
	public boolean isAutoSneaking(Player player) {
		return autoSneakers.contains(player.getName());
	}
	
	/**
	 * Toggle the sneaking of a given player.
	 * @param player - the player to toggle.
	 * @return TRUE if the player is now sneaking, FALSE otherwise.
	 */
	public boolean toggleAutoSneaking(Player player) {
		boolean toggled = !isAutoSneaking(player);
		
		setAutoSneaking(player, toggled);
		return toggled;
	}

	@SuppressWarnings("unchecked")
	public static AutoSneakers deserialize(Map<String, Object> input) {
		Object value = input.get(SNEAKERS_KEY);
		
		if (value == null)
			throw new IllegalArgumentException("Cannot construct auto sneaker - missing list.");
		else if (!(value instanceof Collection))
			throw new IllegalArgumentException("Value should be a collection, but was " + value.getClass());
		
		// Construc a new list, just in case
		return new AutoSneakers(new HashSet<String>((Collection<String>) value));
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> container = new HashMap<String, Object>();
		container.put(SNEAKERS_KEY, new ArrayList<String>(autoSneakers));
		return container;
	}
}
