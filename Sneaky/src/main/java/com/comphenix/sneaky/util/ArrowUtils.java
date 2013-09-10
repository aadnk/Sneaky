package com.comphenix.sneaky.util;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.StructureModifier;

public class ArrowUtils {
	private static BukkitUnwrapper UNWRAPPER = new BukkitUnwrapper();
	private static StructureModifier<Integer> SHARED_INTEGER_MODIFIER;
	private static StructureModifier<Boolean> SHARED_BOOLEAN_MODIFIER;

	// Internal checking
	private static final int MAXIMUM_DISTANCE = 50;
	
	/**
	 * Retrieve the block hit by this arrow.
	 * <p>
	 * Note that this must be invoked a tick after ProjectileHitEvent.
	 * @param arrow - the arrow to test.
	 * @return The location.
	 */
	public static Location getHitBlock(Arrow arrow) {
		Object nmsEntity = getNMSEntity(arrow);

		StructureModifier<Integer> integerModifier = SHARED_INTEGER_MODIFIER.withTarget(nmsEntity);
		Location result = new Location(arrow.getWorld(), 
			integerModifier.read(0), integerModifier.read(1), integerModifier.read(2));
		
		// Sanity check
		if (result.distance(arrow.getLocation()) < MAXIMUM_DISTANCE) 
			return result;
		else
			throw new IllegalStateException("Unable to determine block location of " + arrow);
	}
	
	/**
	 * Determine if a given arrow has hit the ground.
	 * @param arrow - the arrow to test.
	 * @return TRUE it if has, FALSE otherwise.
	 */
	public static boolean hasHitGround(Arrow arrow) {
		Object nmsEntity = getNMSEntity(arrow);
		return SHARED_BOOLEAN_MODIFIER.withTarget(nmsEntity).read(0);
	}
	
	/**
	 * Retrieve the NMS entity.
	 * @param arrow - the arrow to retrieve.
	 * @return The corresponding NMS entity,
	 */
	private static Object getNMSEntity(Arrow arrow) {
		Object nmsEntity = UNWRAPPER.unwrapItem(arrow);
		
		if (SHARED_INTEGER_MODIFIER == null) {
			StructureModifier<Object> modifier = new StructureModifier<Object>(
				nmsEntity.getClass(), nmsEntity.getClass().getSuperclass(), false, false);
			SHARED_INTEGER_MODIFIER = modifier.withType(int.class);
			SHARED_BOOLEAN_MODIFIER = modifier.withType(boolean.class);
		}
		return nmsEntity;
	}
}
