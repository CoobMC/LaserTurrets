package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;

public class ArrowTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final TurretData turretData : turretRegistry.getArrowTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretData.getCurrentLevel();
			final int range = turretRegistry.getTurretRange(block, level);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, range, LivingEntity.class, block);

			if (nearestEntity == null)
				continue;

			shootArrow(nearestEntity, block);
		}
	}

	private void shootArrow(final LivingEntity target, final Block block) {
		if (target != null) {
			final Location blockLocation = block.getLocation().add(0.5, 1.5, 0.5);
			final Location targetLocation = target.getLocation().clone().add(-0.5, 1, -0.5);
			final double distance = blockLocation.distance(targetLocation);
			final Vector vector = targetLocation.subtract(block.getLocation()).toVector().normalize().multiply(1.5);
			final Arrow arrow = block.getWorld().spawnArrow(blockLocation, vector, 1, 0);
			final Location arrowLocation = arrow.getLocation().clone();

			if (distance < 4) {
				arrow.teleport(blockLocation.clone().add(0, 0.5, 0));
				vector.add(new Vector(0, -1.2, 0));
			} else {
				for (int i = 0; i <= 10; i++)
					if (arrow.getLocation().getBlock().getType() != Material.AIR)
						arrow.teleport(arrowLocation.add(vector.clone().add(new Vector(0, -0.1, 0)).normalize().multiply(0.1)));

				if (distance >= 4 && distance <= 10)
					vector.add(new Vector(0, -0.16, 0));
				else if (distance > 10 && distance <= 18)
					vector.add(new Vector(0, -0.12, 0)).multiply(1.6);
				else if (distance > 18 && distance <= 25)
					vector.add(new Vector(0, 0, 0)).multiply(2);
				else if (distance > 25)
					vector.add(new Vector(0, 0.02, 0)).multiply(2);
			}

			arrow.setVelocity(vector);
			Common.runLater(80, arrow::remove);
		}
	}
}


