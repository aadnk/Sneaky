package com.comphenix.sneaky;

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

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.sneaky.cooldown.*;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Add cooldown expire notification to a cooldown store.
 * 
 * @author Kristian
 */
class CooldownManager implements CooldownListenerSource {
	// How often to perform a cooldown check
	private static final long COOLDOWN_DELAY_CHECK = 1;
	
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
	
	// Tasks
	private int task;
	private Plugin plugin;	
	private Server server;
	
	// Quickly lookup the next expired cooldown
	private Queue<Element> cooldowns = new PriorityQueue<Element>();
	
	// Cooldown listeners
	private List<CooldownListener> listeners = new ArrayList<CooldownListener>();
	
	// The list of sneakers we will extend
	private AutoSneakers sneakerList;
	
	// The listener that informs us of changes to the cooldown
	private CooldownListener cooldownListener;
	
	public CooldownManager(Plugin plugin, AutoSneakers sneakerlist) {
		this.plugin = plugin;
		this.sneakerList = sneakerlist;
		
		// Pass on changes to the cooldown
		registerProxy();
	}
	
	private void registerProxy() {
		sneakerList.addCooldownListener(cooldownListener = new CooldownListener() {
			@Override
			public void cooldownExpired(CooldownExpiredEvent event) {
				// Will never occur
			}
			
			@Override
			public void cooldownChanged(CooldownChangedEvent event) {
				// Update our expire data structure
				removeCooldown(event.getPlayer(), event.getFromValue());
				setCooldown(event.getPlayer(), event.getToValue());
				
				// Pass on event
				invokeChangedEvent(event);
			}
		});
	}
	
	public void registerBukkit(Server server) {
		this.server = server;
		this.task = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				long current = System.currentTimeMillis();
				
				// See if multiple cooldowns have expired
				while (cooldowns.size() > 0 && current > cooldowns.peek().cooldown) {
					Player player = cooldowns.poll().player;
					CooldownExpiredEvent event = new CooldownExpiredEvent(this, player);
					
					invokeExpireEvent(event);
				}
			}
		}, COOLDOWN_DELAY_CHECK, COOLDOWN_DELAY_CHECK);
		
		// Handle players that have logged in and out
		server.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				// Update the expire list
				Player player = event.getPlayer();
				setCooldown(player, sneakerList.getCooldown(player));
			}
			
			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent event) {
				// Remove expire
				Player player = event.getPlayer();
				removeCooldown(player, sneakerList.getCooldown(player));
			}
			
		},plugin);
	}
	
	private void invokeExpireEvent(CooldownExpiredEvent event) {
		for (CooldownListener listener : listeners) {
			listener.cooldownExpired(event);
		}
	}
	
	private void invokeChangedEvent(CooldownChangedEvent event) {
		for (CooldownListener listener : listeners) {
			listener.cooldownChanged(event);
		}
	}
	
	private void setCooldown(Player player, Long currentCooldown) {
		if (currentCooldown != null) {
			cooldowns.add(new Element(player, currentCooldown));
		}
	}
	
	private void removeCooldown(Player player, Long previousCooldown) {
		if (previousCooldown != null) {
			cooldowns.remove(new Element(player, previousCooldown));
		}
	}
	
	@Override
	public void addCooldownListener(CooldownListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeCooldownListener(CooldownListener listener) {
		listeners.remove(listener);
	}
	
	public void close() {
		if (task >= 0) {
			// Clear the cooldown listener too
			sneakerList.removeCooldownListener(cooldownListener);
			cooldownListener = null;
			
			server.getScheduler().cancelTask(task);
			task = -1;
		}
	}
}
