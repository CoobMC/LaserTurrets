package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.EntityUtil;

public class ArrowTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final TurretData turretData : turretRegistry.getArrowTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretRegistry.getCurrentTurretLevel(block);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntity(location, turretRegistry.getTurretRange(block, level), LivingEntity.class);

			if (nearestEntity == null)
				continue;

			shootArrow(nearestEntity, block);
		}
	}

	private void shootArrow(final LivingEntity target, final Block block) {
		if (target != null) {
			final Location location = block.getLocation();
			final Location targetLocation = target.getLocation().clone().add(-0.5, 1, -0.5);

			location.setY(location.getY() + 1.2);
			location.setX(location.getX() + 0.5);
			location.setZ(location.getZ() + 0.5);

			final Vector vector = targetLocation.subtract(block.getLocation()).toVector().normalize();
			final Arrow arrow = block.getWorld().spawnArrow(location, vector, 1, 0);

			arrow.setVelocity(vector.multiply(2).add(new Vector(0, 0.1, 0)));
		}
	}
}


