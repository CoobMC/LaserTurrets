package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.remain.CompParticle;

public class LaserPointerTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getRegisteredTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretRegistry.getCurrentTurretLevel(block);

			if (!turretData.getLevel(level).isLaserEnabled())
				continue;
			
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, turretRegistry.getTurretRange(block, level), LivingEntity.class, block);

			if (nearestEntity == null)
				continue;

			nearestEntity.damage(turretRegistry.getLaserDamage(block, level));

			final int length = 50; // show twenty blocks ahead
			final Location laserLocation = location.clone();

			laserLocation.setY(location.getY() + 1.2);
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

			final Vector vector = new Vector(X, Z, Y);

			for (double waypoint = 1; waypoint < length; waypoint += 0.5) {
				laserLocation.add(vector);
				CompParticle.REDSTONE.spawn(laserLocation);
			}

			if (turretRegistry.isLaserEnabled(block, turretRegistry.getCurrentTurretLevel(block)) && turretRegistry.getLaserDamage(block, level) > 0) {
				final double damage = turretRegistry.getLaserDamage(block, level);
				nearestEntity.damage(damage); // TODO
			}
		}
	}
}



