package com.comphenix.sneaky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.comphenix.sneaky.cooldown.CooldownChangedEvent;
import com.comphenix.sneaky.cooldown.CooldownListener;
import com.comphenix.sneaky.cooldown.CooldownListenerSource;

/**
 * Represents a list of sneakers and every player's cooldown.
 * 
 * @author Kristian
 */
public class AutoSneakers implements ConfigurationSerializable, CooldownListenerSource {
	// Where the list of sneaking players is stored
	private static final String SNEAKERS_KEY = "sneakers";
	private static final String COOLDOWN_KEY = "cooldowns";
	
	// Store whether or not a player is auto sneaking or not
	private Set<String> autoSneakers;
	
	// Cooldown
	private Map<String, Long> cooldowns;
	
	// Change listeners
	private List<CooldownListener> listeners = new ArrayList<CooldownListener>();
	
	public AutoSneakers() {
		// Initialize an empty set and map
		this(
			new HashSet<String>(),
			new HashMap<String, Long>()
		);
	}
	
	public AutoSneakers(Set<String> sneakers, Map<String, Long> cooldowns) {
		this.autoSneakers = sneakers;
		this.cooldowns = cooldowns;
	}
	
	/**
	 * Set whether or not a player is currently automatically sneaking.
	 * @param player - the player to change.
	 * @param value - TRUE if the player is automatically sneaking, FALSE otherwise.
	 */
	public void setAutoSneaking(Player player, boolean value) {
		if (value) {
			autoSneakers.add(player.getName());
		} else {
			autoSneakers.remove(player.getName());
		}
	}
		
	/**
	 * Determine if a player is automatically sneaking.
	 * @param player - the player to test.
	 * @return TRUE if the player is automatically sneaking, FALSE otherwise.
	 */
	public boolean isAutoSneaking(Player player) {
		return autoSneakers.contains(player.getName());
	}
	
	/**
	 * Retrieve the time after which a state change is possible or required:
     * <ul>
     *   <li>If the player is currently not sneaking, this is the time after which a 
     *       player may toggle its sneaking status.</li>
     *   <li>If not, this is instead the time after which a toggle will be forced.</li>
     * </ul>
	 * <p>
	 * This time is measured in milliseconds since 1970.1.1 00:00 GMT. 
	 * @param player - the player to retrieve.
	 * @return A time after which the state can be changed, or NULL if not set.
	 */
	public Long getCooldown(Player player) {
		return cooldowns.get(player.getName());
	}
	
	/**
	 * Set the time after which a state change is possible or required.
	 * @param player - the player to update.
	 * @param time - the new cooldown, or NULL to remove the cooldown entirely.
	 * @see {@link AutoSneakers#getCooldown(Player)} for more details.
	 */
	public void setCooldown(Player player, Long time) {
		Long old = null;
		
		if (time != null)
			old = cooldowns.put(player.getName(), time);
		else
			old = cooldowns.remove(player.getName());
		
		// Inform about the change
		onCooldownChange(player, old, time);
	}
	
	private void onCooldownChange(Player player, Long from, Long to) {
		CooldownChangedEvent event = new CooldownChangedEvent(this, player, from, to);
		
		for (CooldownListener listener : listeners) {
			listener.cooldownChanged(event);
		}
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

	@Override
	public void addCooldownListener(CooldownListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeCooldownListener(CooldownListener listener) {
		listeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	public static AutoSneakers deserialize(Map<String, Object> input) {
		Object list = input.get(SNEAKERS_KEY);
		Object map = input.get(COOLDOWN_KEY);

		checkInput(list, Collection.class);
		checkInput(map, Map.class);
		
		// Construct new objects, just in case
		return new AutoSneakers(
				new HashSet<String>((Collection<String>) list),
				new HashMap<String, Long>((Map<String, Long>) map)
		);
	}
	
	private static void checkInput(Object value, Class<?> type) {
		if (value == null)
			throw new IllegalArgumentException("Cannot construct auto sneaker - missing " + type.getClass());
		else if (!type.isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException("Value should be a collection, but was " + value.getClass());
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> container = new HashMap<String, Object>();
		container.put(SNEAKERS_KEY, new ArrayList<String>(autoSneakers));
		container.put(COOLDOWN_KEY, cooldowns);
		return container;
	}
}
