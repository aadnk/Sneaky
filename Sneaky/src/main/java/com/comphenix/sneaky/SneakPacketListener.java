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
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.sneaky.packets.Packet28EntityMetadata;

class SneakPacketListener extends PacketAdapter {
	private static final int ENTITY_CROUCHED = 0x02;
	
	// Whether or not a player can see autosneaking
	private static final String PERMISSION_HIDE_AUTO = "sneaky.hide.autosneak";
	
	// Determine if a player is autosneaking or not
	private AutoSneakers autoSneakers;
	
	// Last seen flag byte
	private Map<Player, Byte> flagByte = new WeakHashMap<Player, Byte>();
	
	public SneakPacketListener(Plugin plugin, AutoSneakers autoSneakers) {
		super(plugin, ConnectionSide.SERVER_SIDE, Packet28EntityMetadata.ID);
		this.autoSneakers = autoSneakers;
	}
	
	/**
	 * Update the given player.
	 * @param manager - reference to ProtocolLib
	 * @param player - player to refresh.
	 * @throws InvocationTargetException If we are unable to send a packet.
	 */
	public void updatePlayer(ProtocolManager manager, Player player) throws InvocationTargetException {
		Byte flag = flagByte.get(player);
		
		// It doesn't matter much
		if (flag == null) {
			flag = 0;
		}
		
		// Create the packet we will transmit
		Packet28EntityMetadata packet = new Packet28EntityMetadata();
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, flag);
	
		packet.setEntityId(player.getEntityId());
		packet.setEntityMetadata(watcher.getWatchableObjects());
		
		// Broadcast the packet
		for (Player observer : manager.getEntityTrackers(player)) {
			manager.sendServerPacket(observer, packet.getHandle());
		}
	}
	
	@Override
	public void onPacketSending(PacketEvent event) {
		// This modification shall only apply to certain users
		if (event.getPlayer().hasPermission(PERMISSION_HIDE_AUTO)) {
			return;
		}
		
		Packet28EntityMetadata packet = new Packet28EntityMetadata(event.getPacket());
		Entity entity = packet.getEntity(event);
		
		if (entity instanceof Player) {
			Player target = (Player) entity;
			
			if (autoSneakers.isAutoSneaking(target)) {
				WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
				Byte flag = watcher.getByte(0);
				
				if (flag != null) {
					// Store the last seen flag byte
					flagByte.put(target, flag);
					
					// Clone and update it
					packet = new Packet28EntityMetadata(packet.getHandle().deepClone());
					watcher = new WrappedDataWatcher(packet.getEntityMetadata());
					watcher.setObject(0, (byte) (flag | ENTITY_CROUCHED));
					
					event.setPacket(packet.getHandle());
				}
			}
		}
	}
}
