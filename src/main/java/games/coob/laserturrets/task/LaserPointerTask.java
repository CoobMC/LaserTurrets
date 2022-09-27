package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.remain.CompParticle;

public class LaserPointerTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getRegisteredTurrets()) {
			if (turretData.isBroken())
				continue;

			final int level = turretData.getCurrentLevel();

			if (!turretData.getLevel(level).isLaserEnabled())
				continue;

			final Location location = turretData.getLocation();
			final Location shootLocation = location.clone().add(0.5, 1.4, 0.5);
			final int range = turretData.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(shootLocation, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			final double damage = turretData.getLevel(level).getLaserDamage();
			final Location laserLocation = location.clone().add(0.5, 1.2, 0.5);
			final Location targetLocation = nearestEntity.getEyeLocation();
			final double distance = location.clone().add(0.5, 1.2, 0.5).distance(targetLocation);
			final Vector vector = targetLocation.subtract(laserLocation).toVector().normalize().multiply(0.5);

			/*laserLocation.setY(location.getY() + 1.2);
			laserLocation.setX(location.getX() + 0.5);
			laserLocation.setZ(location.getZ() + 0.5);

			final double dX = laserLocation.getX() - nearestEntity.getLocation().getX();
			final double dY = laserLocation.getY() - nearestEntity.getLocation().getY();
			final double dZ = laserLocation.getZ() - nearestEntity.getLocation().getZ();

			final double yaw = Math.atan2(dZ, dX);
			final double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

			final double X = Math.sin(pitch) * Math.cos(yaw);
			final double Y = Math.sin(pitch) * Math.sin(yaw);
			final double Z = Math.cos(pitch);

			final Vector vector = new Vector(X, Z, Y);*/

			for (double waypoint = 1; waypoint < distance + 0.5; waypoint += 0.5) {
				laserLocation.add(vector);
				CompParticle.REDSTONE.spawn(laserLocation);
			}

			if (turretData.getLevel(level).isLaserEnabled() && damage > 0)
				nearestEntity.damage(damage);
		}
	}
}



