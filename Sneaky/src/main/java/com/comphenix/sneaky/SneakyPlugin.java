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

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class SneakyPlugin extends JavaPlugin implements Listener {
	/**
	 * Represents the main name of this command.
	 */
	public static final String NAME = "sneak";
	
	// Permissions
	public static final String PERMISSION_SELF = "sneaky.sneak.self";
	public static final String PERMISSION_OTHER = "sneaky.sneak.other";
	public static final String PERMISSION_EXEMPT = "sneaky.exempt";
	
	// List of people sneaking
	private AutoSneakers sneakers;
	private SneakPacketListener listener;

	// Configuration
	private TypedConfiguration config;
	
	// Reference to PL
	private ProtocolManager manager;
	
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
		
		sneakers = config.getSneakers();

		manager = ProtocolLibrary.getProtocolManager();
		listener = new SneakPacketListener(this, sneakers);
		
		// Register listeners
		getServer().getPluginManager().registerEvents(this, this);
		manager.addPacketListener(listener);
	}

	public void onDisable() {
		// Save the list of sneakers
		config.setSneakers(sneakers);
		saveConfig();
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
		
		// Handle cooldown to enabling automatic sneaking
		if (sneaking && !sender.hasPermission(PERMISSION_EXEMPT)) {
			Long cooldown = sneakers.getCooldown(target);
			long current = System.currentTimeMillis();
			
			if (cooldown != null && cooldown > System.currentTimeMillis()) {
				return config.getFormattedMessage((cooldown - current) / 1000.0);
			}
		}
		
		// Verify permissions
		if (!sender.hasPermission(sender == target ? PERMISSION_SELF : PERMISSION_OTHER)) {
			return "Insufficient permission to toggle sneaking.";
		}
		
		try {
			toggleSneaking(sender, target);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return "Unable to update nearby players.";
		}
		
		// No error
		return null;
	}
	
	public void toggleSneaking(CommandSender sender, Player target) throws InvocationTargetException {
		// Toggle sneaking
		boolean status = sneakers.toggleAutoSneaking(target);

		// Get the message to transmit
		String message = ChatColor.GOLD + config.getFormattedMessage(status, target.getName());

		// We may need to refresh the player
		listener.updatePlayer(manager, target);
		
		// Update cooldown
		double delta = (status ? config.getDuration() : config.getCooldown()) * 1000.0;
		
		// Save cooldown
		if (delta > 0) {
			sneakers.setCooldown(target, System.currentTimeMillis() + (long)delta);
		} else {
			// Remove it
			sneakers.setCooldown(target, null);
		}
		
		if (target == sender) {
			sender.sendMessage(message);
		} else {
			sender.sendMessage(message);
			target.sendMessage(message);
		}
	}
}
