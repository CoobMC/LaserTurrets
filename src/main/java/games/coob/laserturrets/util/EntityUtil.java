package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.mineacademy.fo.remain.CompParticle;

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

		for (final Entity nearby : center.getWorld().getNearbyEntities(center, range3D, range3D, range3D))
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass()) && !(nearby instanceof ArmorStand))
				if (!turretData.isPlayerBlacklisted(nearby.getUniqueId()) && !turretData.isMobBlacklisted(nearby.getType()))
					foundEntities.add(nearby);

		foundEntities.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));

		for (final Iterator<Entity> entityIterator = foundEntities.iterator(); entityIterator.hasNext(); ) {
			final Entity entity = entityIterator.next();
			final Location entityLocation = ((LivingEntity) entity).getEyeLocation().clone();
			final Vector vector = entityLocation.subtract(center).toVector();

			center.setDirection(vector);

			if (vectorHasBlock(center, vector))
				entityIterator.remove();
		}

		return foundEntities.isEmpty() ? null : (LivingEntity) foundEntities.get(0);
	}


	public static <T> LivingEntity findNearestEntityNonBlacklisted(final Location center, final double range3D, final Class<T> entityClass) {
		if (center.getWorld() == null)
			return null;

		final List<Entity> found = new ArrayList<>();
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(center.getBlock());

		for (final Entity nearby : center.getWorld().getNearbyEntities(center, range3D, range3D, range3D))
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass()) && !(nearby instanceof ArmorStand))
				if (!turretData.isPlayerBlacklisted(nearby.getUniqueId()) && !turretData.isMobBlacklisted(nearby.getType()))
					found.add(nearby);

		found.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));

		for (final Iterator<Entity> entityIterator = found.iterator(); entityIterator.hasNext(); ) {
			final Entity entity = entityIterator.next();
			final Location entityLocation = ((LivingEntity) entity).getEyeLocation().clone();
			final Vector vector = entityLocation.subtract(center).toVector();

			center.setDirection(vector);

			if (vectorHasBlock(center, vector))
				entityIterator.remove();
		}

		return found.isEmpty() ? null : (LivingEntity) found.get(0);
	}

	private static boolean vectorHasBlock(final Location start, final Vector direction) {
		final int length = (int) Math.floor(direction.length());

		if (length >= 1) {
			try {
				final BlockIterator blockIterator = new BlockIterator(start.getWorld(), start.toVector(), direction, 0, length);
				Block block;

				final Location location = start.clone();
				for (double waypoint = 1; waypoint < 50; waypoint += 0.5) {
					location.add(direction.normalize());
					CompParticle.REDSTONE.spawn(location);
				}

				while (blockIterator.hasNext()) {
					block = blockIterator.next();

					if (block.getType().isSolid()) {
						block.setType(Material.DIAMOND_BLOCK);
						return true;
					}
				}
			} catch (final IllegalStateException exception) {
				return false;
			}
		}

		return false;
	}
}
