package com.comphenix.sneaky;

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

class SneakListener extends PacketAdapter {
	private static final int ENTITY_CROUCHED = 0x02;
	
	private AutoSneakers autoSneakers;
	
	// Last seen flag byte
	private Map<Player, Byte> flagByte = new WeakHashMap<Player, Byte>();
	
	public SneakListener(Plugin plugin, AutoSneakers autoSneakers) {
		super(plugin, ConnectionSide.SERVER_SIDE, Packet28EntityMetadata.ID);
		this.autoSneakers = autoSneakers;
	}
	
	/**
	 * Update the given player.
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
					
					watcher = watcher.deepClone();
					watcher.setObject(0, (byte) (flag | ENTITY_CROUCHED));
					
					// Update the watchable objects
					packet.setEntityMetadata(watcher.getWatchableObjects());
				}
			}
		}
	}
}
