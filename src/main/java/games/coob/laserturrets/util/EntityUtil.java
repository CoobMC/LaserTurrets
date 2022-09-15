package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityUtil {
	public static <T> LivingEntity findNearestEntityNonBlacklisted(final Location center, final double range3D, final Class<T> entityClass, final Block turret) {
		final List<Entity> found = new ArrayList<>();
		final TurretRegistry registry = TurretRegistry.getInstance();

		if (center.getWorld() == null)
			return null;

		for (final Entity nearby : center.getWorld().getNearbyEntities(center, range3D, range3D, range3D)) {
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass()) && !(nearby instanceof ArmorStand)) {
				if (!registry.isPlayerBlacklisted(turret, nearby.getUniqueId()) && !registry.isMobBlacklisted(turret, nearby.getType())) {
					final Vector vector = nearby.getLocation().subtract(center.clone().add(0, 0.8, 0)).toVector();
					center.setDirection(vector);

					if (vectorHasBlock(center, vector)) {
						System.out.println("no clean shot");
						break;
					}

					found.add(nearby);
				}
			}
		}

		found.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));
		return found.isEmpty() ? null : (LivingEntity) found.get(0);
	}

	private static boolean vectorHasBlock(final Location start, final Vector direction) {
		final int length = (int) Math.floor(direction.length());

		if (length >= 1) {
			try {
				final BlockIterator blockIterator = new BlockIterator(start.getWorld(), start.clone().add(0, 1.2, 0).toVector(), direction.add(new Vector(0, 1.2, 0)), 0, length);
				Block block;

				while (blockIterator.hasNext()) {
					block = blockIterator.next();
					// Account for a Spigot bug: BARRIER and MOB_SPAWNER are not occluding blocks
					if (block.getType().isSolid()) {
						block.setType(Material.DIAMOND_BLOCK);
						return true;
					}
				}
			} catch (final IllegalStateException exception) {
				// Just in case the start block could not be found for some reason or a chunk is loaded async.
				return false;
			}
		}

		return false;
	}
}
