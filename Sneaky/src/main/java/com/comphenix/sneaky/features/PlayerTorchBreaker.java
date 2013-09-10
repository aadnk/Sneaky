package com.comphenix.sneaky.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

import com.comphenix.sneaky.util.ArrowUtils;

/**
 * Represents a listener that allows players with a specific permission to break torches using arrows.
 * @author Kristian
 */
public class PlayerTorchBreaker implements Listener {
	/**
	 * Whether or not a player is able to fire projectiles that can break torches.
	 */
	private static final String BREAK_TORCH_PERMISSION = "sneaky.breaktorch";
	
	// Possible torches
	private BlockFace[] TORCH_LOCATIONS = new BlockFace[] { 
			BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP };
		
	private Plugin plugin;
	
	public PlayerTorchBreaker(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent e) {
		Projectile entity = e.getEntity();

		// This only applies to arrows
		if (entity instanceof Arrow && canBreakTorches(entity.getShooter())) {	
			final Arrow arrow = (Arrow) entity;
						
			// The information we need can be found after the event has been invoked
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					if (!ArrowUtils.hasHitGround(arrow)) {
						System.out.println("Hit animal");
						return;
					}
					
					// Detach any arrows attached to this block
					Location loc = ArrowUtils.getHitBlock(arrow);
					Block block = loc.getBlock();
					
					// Think of it has if the arrow is "shaking" the block
					for (BlockFace face : TORCH_LOCATIONS) {
						Block relative = block.getRelative(face);
						
						if (relative.getType() == Material.TORCH) {
							relative.breakNaturally();
						}
					}
				}
			});
		}
	}
	
	/**
	 * Determine if a given entity can break torches.
	 * @param entity - the entity to test.
	 * @return TRUE it it can, FALSE otherwise.
	 */
	private boolean canBreakTorches(Entity entity) {
		return entity instanceof Permissible && 
			   ((Permissible) entity).hasPermission(BREAK_TORCH_PERMISSION);
	}
}
