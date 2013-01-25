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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.sneaky.cooldown.CooldownChangedEvent;
import com.comphenix.sneaky.cooldown.CooldownExpiredEvent;
import com.comphenix.sneaky.cooldown.CooldownListener;
import com.comphenix.sneaky.cooldown.CooldownListenerSource;
import com.comphenix.sneaky.metrics.MetricsLite;

public class SneakyPlugin extends JavaPlugin implements Listener {
	/**
	 * Represents the main name of this command.
	 */
	public static final String NAME = "sneak";
	
	// Permissions
	public static final String PERMISSION_SELF = "sneaky.sneak.self";
	public static final String PERMISSION_OTHER = "sneaky.sneak.other";
	public static final String PERMISSION_EXEMPT = "sneaky.exempt";
	
	// Override default cooldowns
	public static final String PLAYER_INFO_COOLDOWN = "sneaky_cooldown";
	public static final String PLAYER_INFO_DURATION = "sneaky_duration";
	
	// List of people sneaking
	private AutoSneakers sneakers;
	private CooldownManager cooldownManager;
	private CooldownListener cooldownListener;
	
	// Minecraft packet handling
	private SneakPacketListener listener;

	// Configuration
	private TypedConfiguration config;
	
	// Reference to PL
	private ProtocolManager manager;
	
	// Vault (if enabled)
	private Chat chat = null;
	
	// Metrics
	private MetricsLite metrics;
	
	public void onEnable() {
		// Load configuration
		ConfigurationSerialization.registerClass(AutoSneakers.class);
		config = new TypedConfiguration(getConfig());
		
		if (!config.isCreated()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
			
			// Load it again
			config = new TypedConfiguration(getConfig());
			getLogger().info("Creating default configuration.");
		}
		
		// Add vault
		if (setupChat()) {
			getLogger().info("Detected Vault.");
		}
		
		sneakers = config.getSneakers();
		cooldownManager = new CooldownManager(this, sneakers);
		registerCooldownListener();
		
		// Packet handling
		manager = ProtocolLibrary.getProtocolManager();
		listener = new SneakPacketListener(this, sneakers);

		// Register listeners
		manager.addPacketListener(listener);
		cooldownManager.registerBukkit(getServer());
		getServer().getPluginManager().registerEvents(this, this);
		
		// Load metrics
		try {
			metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Damn it
			e.printStackTrace();
		}
	}
	
	private void registerCooldownListener() {
		cooldownListener = new CooldownListener() {
			@Override
			public void cooldownExpired(CooldownExpiredEvent event) {
				Player player = event.getPlayer();
				
				// Skip player's that have no cooldown permission
				if (!player.hasPermission(PERMISSION_EXEMPT)) {
					if (sneakers.isAutoSneaking(player)) {
						try {
							toggleSneaking(player);
							
						} catch (InvocationTargetException e) {
							// That would be bad
							e.printStackTrace();
						}
					} else {
						// Inform about this opportunity
						player.sendMessage(config.getCooldownExpiredMessage());
						
						// Remove the cooldown completely
						sneakers.setCooldown(player, null);
					}
				}
			}
			
			@Override
			public void cooldownChanged(CooldownChangedEvent event) {
				Player player = event.getPlayer();
				
				if (!player.hasPermission(PERMISSION_EXEMPT)) {
					if (!sneakers.isAutoSneaking(player) && event.getToValue() != null) {
						String message = getCooldownMessage(player);
						
						// Inform about the cooldown
						if (message != null && message.length() > 0) {
							player.sendMessage(ChatColor.RED + message);
						}
					}
				}
			}
		};
		cooldownManager.addCooldownListener(cooldownListener);
	}

	/**
	 * Initialize refernece to Vault.
	 * @return TRUE if Vault was detected and loaded, FALSE otherwise.
	 */
    private boolean setupChat() {
    	try {
	        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
	        
	        if (chatProvider != null) {
	            chat = chatProvider.getProvider();
	        }
	        return (chat != null);
    	} catch (NoClassDefFoundError e) {
    		// Nope
    		return false;
    	}
    }
	
	public void onDisable() {
		// Save the list of sneakers
		config.setSneakers(sneakers);
		saveConfig();
		
		// Clean up
		cooldownManager.close();
		cooldownManager.removeCooldownListener(cooldownListener);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Inform a player about the automatic sneaking
		if (sneakers.isAutoSneaking(event.getPlayer())) {
			event.getPlayer().sendMessage(ChatColor.GOLD + config.getFormattedMessage(true, event.getPlayer().getName()));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Process the main sneak command
		if (command.getName().equals(NAME)) {
			String error = processSneak(sender, args);
			
			if (error != null) {
				sender.sendMessage(ChatColor.RED + error);
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Process the sneak command, returning an error message if not successful.
	 * @param sender - the sender of the command.
	 * @param args - arguments.
	 * @return An error message, or NULL if successful.
	 */
	private String processSneak(CommandSender sender, String[] args) {
		Player target = null;
		
		// Parse parameter
		if (args.length == 0) {
			if (sender instanceof Player)
				target = (Player) sender;
			else
				return "The player parameter is only optional when executed by a player.";
			
		} else if (args.length == 1) {
			target = getServer().getPlayer(args[0]);
			
			if (target == null) {
				return "Cannot find player " + args[0];
			}
			
		} else {
			return "This command can only take one parameter.";
		}
		
		// Whether or not this player is currently automatically sneaking
		boolean sneaking = sneakers.isAutoSneaking(target);
		
		// Handle cooldown to enable automatic sneaking
		if (!sneaking && !sender.hasPermission(PERMISSION_EXEMPT)) {
			String message = getCooldownMessage(target);
			
			// See if we in fact are under a cooldown
			if (message != null) {
				return message;
			}
		}
		
		// Verify permissions
		if (!sender.hasPermission(sender == target ? PERMISSION_SELF : PERMISSION_OTHER)) {
			return "Insufficient permission to toggle sneaking.";
		}
		
		try {
			boolean status = toggleSneaking(target);
			String message = ChatColor.GOLD + config.getFormattedMessage(status, target.getName());
			
			// Notify player and sender
			if (target == sender) {
				sender.sendMessage(message);
			} else {
				sender.sendMessage(message);
				target.sendMessage(message);
			}
			
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return "Unable to update nearby players.";
		}
		
		// No error
		return null;
	}
	
	/**
	 * Retrieve the cooldown message, or NULL if there is no cooldown.
	 * @param player - the player to retrieve the message for.
	 * @return The cooldown message.
	 */
	private String getCooldownMessage(Player player) {
		Double cooldown = getCooldown(player);
		
		if (cooldown != null && cooldown > 0) {
			return config.getFormattedMessage(cooldown);
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieve the object that reports when a cooldown changes or expires.
	 * @return The cooldown manager.
	 */
	public CooldownListenerSource getCooldownManager() {
		return cooldownManager;
	}
	
	/**
	 * Retrieve the number of seconds left before a state change is possible or automatic.
	 * <ul>
     *   <li>If the player is currently not sneaking, this is the amount of time left until a 
     *       player may toggle its sneaking status.</li>
     *   <li>If not, this is the time until a toggle will be forced.</li>
     * </ul>
     * Note that this may be negative if we've exceeded the cooldown.
	 * @param player - the player whose information we're looking for.
	 * @return Number of seconds left, or NULL if no cooldown is defined.
	 */
	public Double getCooldown(Player player) {
		Long current = System.currentTimeMillis();
		Long cooldown = sneakers.getCooldown(player);
		
		if (cooldown != null) {
			return (cooldown - current) / 1000.0;
		} else {
			return null;
		}
	}
	
	/**
	 * Toggle the sneaking of a given player.
	 * <p>
	 * This disregards any permission or cooldowns that may be active.
	 * 
	 * @param target - the player whose sneaking will be toggled.
	 * @return
	 * @throws InvocationTargetException If we are unable to notify sourrounding player's of this change.
	 */
	public boolean toggleSneaking(@Nonnull Player target) throws InvocationTargetException {
		if (target == null)
			throw new IllegalArgumentException("target cannot be NULL.");
		
		// Toggle sneaking
		boolean status = sneakers.toggleAutoSneaking(target);
		double delta = -1;
		
		// We may need to refresh the player
		listener.updatePlayer(manager, target);
		
		// Look this up in Vault
		if (chat != null) {
			String key = (status ? PLAYER_INFO_DURATION : PLAYER_INFO_COOLDOWN);
			delta = chat.getPlayerInfoDouble(target, key, -1);
			
			// Try the integer as well
			if (delta < -0.5) {
				delta = chat.getPlayerInfoInteger(target, key, -1);
			}
		}
		// Use default if Vault failed
		if (delta < -0.5) {
			delta = (status ? config.getDuration() : config.getCooldown()) * 1000.0;
		}
		
		// Save cooldown
		if (delta > 0) {
			sneakers.setCooldown(target, System.currentTimeMillis() + (long)delta);
		} else {
			// Remove it
			sneakers.setCooldown(target, null);
		}
		return status;
	}
}
