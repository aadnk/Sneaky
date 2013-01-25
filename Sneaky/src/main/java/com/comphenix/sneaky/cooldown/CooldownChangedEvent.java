package com.comphenix.sneaky.cooldown;

import java.util.EventObject;

import org.bukkit.entity.Player;

/**
 * Represents a cooldown change event.
 * 
 * @author Kristian
 */
public class CooldownChangedEvent extends EventObject {
	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = -1375858549864271307L;
	
	private final Player player;
	private final Long fromValue;
	private final Long toValue;

	public CooldownChangedEvent(Object source, Player player, Long fromValue, Long toValue) {
		super(source);
		
		this.player = player;
		this.fromValue = fromValue;
		this.toValue = toValue;
	}

	public Player getPlayer() {
		return player;
	}

	/**
	 * Retrieve the original cooldown value in milliseconds since 1970.
	 * @return Cooldown value, or NULL if not set.
	 */
	public Long getFromValue() {
		return fromValue;
	}

	/**
	 * Retrieve the new cooldown value in milliseconds since 1970.
	 * @return New cooldown value, or NULL if removed.
	 */
	public Long getToValue() {
		return toValue;
	}
}
