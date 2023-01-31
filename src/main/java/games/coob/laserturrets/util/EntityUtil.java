package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EntityUtil {
	public static <T> LivingEntity findNearestEntityNonBlacklisted(final Location center, final double range3D, final Class<T> entityClass, final Block turret) {
		if (center.getWorld() == null)
			return null;

		final List<Entity> foundEntities = new ArrayList<>();
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(turret);

		for (final Entity nearby : center.getWorld().getNearbyEntities(center, range3D, range3D, range3D)) {
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass()) && !(nearby instanceof ArmorStand))
				if (((!turretData.isPlayerListedAsAlly(nearby.getUniqueId()) && !turretData.isPlayerWhitelistEnabled()) && (!turretData.isMobListedAsAlly(nearby.getType()) && !turretData.isMobWhitelistEnabled()))
						|| ((turretData.isPlayerListedAsAlly(nearby.getUniqueId()) && turretData.isPlayerWhitelistEnabled()) || (turretData.isMobListedAsAlly(nearby.getType()) && turretData.isMobWhitelistEnabled())))
					foundEntities.add(nearby);
		}

		foundEntities.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));

		for (final Iterator<Entity> entityIterator = foundEntities.iterator(); entityIterator.hasNext(); ) {
			final Entity entity = entityIterator.next();
			final Location entityLocation = ((LivingEntity) entity).getEyeLocation().clone();
			final Vector vector = entityLocation.subtract(center).toVector();

			/*
			final Location location = center.clone();
			for (double waypoint = 1; waypoint < 30 + 0.5; waypoint += 0.5) {
				location.add(vector.clone().normalize().multiply(0.5));
				CompParticle.VILLAGER_HAPPY.spawn(location);
			}*/

			center.setDirection(vector);
			
			if (vectorHasBlock(center, vector))
				entityIterator.remove();
		}

		return foundEntities.isEmpty() ? null : (LivingEntity) foundEntities.get(0);
	}

	private static boolean vectorHasBlock(final Location start, final Vector direction) {
		final int length = (int) Math.floor(direction.length());

		if (length >= 1) {
			try {
				final BlockIterator blockIterator = new BlockIterator(start.getWorld(), start.toVector(), direction, 0, length);
				Block block;

				while (blockIterator.hasNext()) {
					block = blockIterator.next();

					if (block.getType().isSolid())
						return true;
				}
			} catch (final IllegalStateException exception) {
				return false;
			}
		}

		return false;
	}
}
