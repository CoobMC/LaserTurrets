package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

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
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass())) {
				if (!registry.isPlayerBlacklisted(turret, nearby.getUniqueId()) && !registry.isMobBlacklisted(turret, nearby.getType()))
					found.add(nearby);
			}
		}

		found.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));
		return found.isEmpty() ? null : (LivingEntity) found.get(0);
	}
}
