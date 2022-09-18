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

		for (final TurretData turretData : turretRegistry.getTurretsOfType("arrow")) {
			final Location location = turretData.getLocation();
			final Location locationTemp = location.clone().add(0.5, 1.4, 0.5);
			final int level = turretData.getCurrentLevel();
			final int range = turretData.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(locationTemp/*location.clone().add(0.5, 1.4, 0.5)*/, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			shootArrowFromBlock(nearestEntity, location.getBlock());
		}
	}

	private void shootArrowFromBlock(final LivingEntity target, final Block block) {
		final Location blockLocation = block.getLocation().clone().add(0.5, 1.4, 0.5);
		final Location targetLocation = target.getEyeLocation().clone();/*target.getLocation().clone().add(-0.5, 1, -0.5);*/
		final double distance = blockLocation.distance(targetLocation);
		final Vector vector = targetLocation.subtract(blockLocation).toVector().normalize();
		final Arrow arrow = target.getWorld().spawnArrow(blockLocation, vector, 1, 0);
		final Location arrowLocation = arrow.getLocation().clone();

		if (distance < 4) {
			arrow.teleport(blockLocation.clone().add(0, 0.5, 0));
			vector.add(new Vector(0, -0.8, 0));
		} else {
			for (int i = 0; i <= 10; i++)
				if (arrow.getLocation().getBlock().getType() != Material.AIR)
					arrow.teleport(arrowLocation.add(vector.clone().add(new Vector(0, -0.1, 0)).normalize().multiply(0.1)));

			// TODO
			final double mag = Math.sqrt(distance / 2);
			final double power = Math.pow(2, 0.05 * distance);
			vector.multiply(mag);

			System.out.println("Mag: " + mag);
			System.out.println("Distance: " + distance);
			System.out.println("Power: " + power);
				/*if (distance >= 4 && distance <= 10)
					vector.add(new Vector(0, -0.16, 0));
				else if (distance > 10 && distance <= 18)
					vector.add(new Vector(0, -0.12, 0)).multiply(1.6);
				else if (distance > 18 && distance <= 25)
					vector.add(new Vector(0, 0, 0)).multiply(2);
				else if (distance > 25)
					vector.add(new Vector(0, 0.02, 0)).multiply(2);*/
		}

		arrow.setVelocity(vector);
		Common.runLater(80, arrow::remove);
	}
}


