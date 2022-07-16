package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.remain.CompParticle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LaserTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final Location location : turretRegistry.getLocations()) {
			final Block block = location.getBlock();

			if (turretRegistry.getType(block).equals("flame"))
				continue;

			final int level = turretRegistry.getCurrentTurretLevel(block);

			if (turretRegistry.getCurrentTurretLevel(block) == 1) {
				final Entity closestPlayer = findNearestEntityNonBlacklisted(location, turretRegistry.getTurretRange(block, level), LivingEntity.class, block);

				if (closestPlayer != null && closestPlayer.getType().equals(EntityType.PLAYER)) {
					final Player player = (Player) closestPlayer;
					player.damage(turretRegistry.getLaserDamage(block, level));

					final int length = 50; // show twenty blocks ahead
					final Location laserLocation = location.clone();

					laserLocation.setY(location.getY() + 1.2);
					laserLocation.setX(location.getX() + 0.5);
					laserLocation.setZ(location.getZ() + 0.5);

					final double dX = laserLocation.getX() - closestPlayer.getLocation().getX();
					final double dY = laserLocation.getY() - closestPlayer.getLocation().getY();
					final double dZ = laserLocation.getZ() - closestPlayer.getLocation().getZ();

					final double yaw = Math.atan2(dZ, dX);
					final double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

					final double X = Math.sin(pitch) * Math.cos(yaw);
					final double Y = Math.sin(pitch) * Math.sin(yaw);
					final double Z = Math.cos(pitch);

					final Vector vector = new Vector(X, Z, Y);

					for (double waypoint = 1; waypoint < length; waypoint += 0.5) {
						laserLocation.add(vector);

						if (!turretRegistry.getType(block).equals("laser"))
							CompParticle.VILLAGER_HAPPY.spawn(laserLocation);
						else CompParticle.REDSTONE.spawn(laserLocation);
					}
				}
			}
		}
	}

	public <T> LivingEntity findNearestEntityNonBlacklisted(final Location center, final double range3D, final Class<T> entityClass, final Block turret) {
		final List<Entity> found = new ArrayList<>();
		final TurretRegistry registry = TurretRegistry.getInstance();

		if (center.getWorld() == null)
			return null;

		for (final Entity nearby : center.getWorld().getNearbyEntities(center, range3D, range3D, range3D)) {
			if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass())) {
				if (!registry.isPlayerBlacklisted(turret, nearby.getName()))
					found.add(nearby);
			}
		}

		found.sort(Comparator.comparingDouble(entity -> entity.getLocation().distance(center)));
		return found.isEmpty() ? null : (LivingEntity) found.get(0);
	}
}



