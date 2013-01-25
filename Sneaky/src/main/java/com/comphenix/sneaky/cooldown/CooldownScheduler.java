package com.comphenix.sneaky.cooldown;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class CooldownScheduler {
	/**
	 * Represents an element in the priority queue.
	 * 
	 * @author Kristian
	 */
	private static class Element implements Comparable<Element> {
		public Player player;
		public long cooldown;
		
		public Element(Player player, long cooldown) {
			this.player = player;
			this.cooldown = cooldown;
		}

		@Override
		public int compareTo(Element o) {
			int result = Longs.compare(cooldown, o.cooldown);
			
			// Compare cooldown first and foremost
			if (result != 0)
				return result;
			else if (o.player != null)
				// Then order by player entity ID
				return Ints.compare(player.getEntityId(), o.player.getEntityId());
			else
				return 1;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Element) {
				return ((Element) obj).compareTo(this) == 0;
			} else {
				return false;
			}
		}
	}
	
	// Quickly lookup the next expired cooldown
	private Queue<Element> cooldowns = new PriorityQueue<Element>();
	
	// Cooldown listeners
	private List<ExpiredListener> listeners = new ArrayList<ExpiredListener>();

	// How often to perform a cooldown check
	private static final long COOLDOWN_DELAY_CHECK = 1;
	
	// Tasks
	private int task;
	private final Plugin plugin;	
	private BukkitScheduler scheduler;
	
	public CooldownScheduler(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void registerBukkit(BukkitScheduler scheduler) {
		this.scheduler = scheduler;
		this.task = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				long current = System.currentTimeMillis();
				
				// See if multiple cooldowns have expired
				while (current > cooldowns.peek().cooldown) {
					ExpiredEvent event = new ExpiredEvent(this, cooldowns.poll().player);
					invokeExpireEvent(event);
				}
			}
		}, COOLDOWN_DELAY_CHECK, COOLDOWN_DELAY_CHECK);
	}
	
	private void invokeExpireEvent(ExpiredEvent event) {
		for (ExpiredListener listener : listeners) {
			listener.cooldownExpired(event);
		}
	}
	
	/**
	 * Update a player's cooldown.
	 * @param player - the player to update.
	 * @param previousCooldown - the previous cooldown value.
	 * @param currentCooldown - the new cooldown value - use NULL to remove.
	 */
	public void setCooldown(Player player, long cooldown) {
		cooldowns.add(new Element(player, cooldown));
	}
	
	/**
	 * Remove a player's cooldown.
	 * @param player - the player to update.
	 * @param cooldown - the cooldown value before it was removed.
	 */
	public void removeCooldown(Player player, long cooldown) {
		cooldowns.remove(new Element(player, cooldown));
	}

	public void addExpiredListener(ExpiredListener listener) {
		listeners.add(listener);
	}
	
	public void removeExpiredListener(ExpiredListener listener) {
		listeners.remove(listener);
	}
	
	public void close() {
		if (task >= 0) {
			scheduler.cancelTask(task);
			task = -1;
		}
	}
}
