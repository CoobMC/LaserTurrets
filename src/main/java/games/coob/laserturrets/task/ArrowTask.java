package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

public class ArrowTask extends BukkitRunnable {

	@Override
	public void run() {
		for (final TurretData turretData : TurretData.getTurretsOfType("arrow")) {
			if (turretData.isBroken())
				continue;

			final TurretSettings settings = TurretSettings.findByName(turretData.getType());
			final Location location = turretData.getLocation();
			final Location shootLocation = location.clone().add(0.5, 1.4, 0.5);
			final int level = turretData.getCurrentLevel();
			final int range = settings.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(shootLocation, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			if (settings.getAmmo().getFirstValue()) {
				if (turretData.hasAmmo())
					turretData.deductAmmo();
				else continue;
			}

			shootArrowFromBlock(nearestEntity, location.getBlock(), turretData.getId());
		}
	}

	private void shootArrowFromBlock(final LivingEntity target, final Block block, final String turretId) {
		final Location blockLocation = block.getLocation().clone().add(0.5, 1.4, 0.5);
		final Location targetLocation = target.getEyeLocation().clone();
		final double distance = blockLocation.distance(targetLocation);
		final Vector vector = targetLocation.subtract(blockLocation).toVector().normalize();
		final Arrow arrow = target.getWorld().spawnArrow(blockLocation, vector, 1, 0);
		final Location arrowLocation = arrow.getLocation().clone();

		if (distance < 1.5) {
			target.damage(arrow.getDamage());
			target.setVelocity(targetLocation.subtract(blockLocation).toVector().normalize().multiply(-0.5));
			arrow.remove();
			return;
		}

		for (int i = 0; i <= 10; i++)
			if (arrow.getLocation().getBlock().getType() != Material.AIR)
				arrow.teleport(arrowLocation.add(vector.clone().add(new Vector(0, -0.1, 0)).normalize().multiply(0.05)));

		final double power = Math.pow(2, 0.04 * distance);
		vector.multiply(power);
		vector.add(new Vector(0, power * 0.15, 0));

		arrow.setMetadata("LaserTurrets", new FixedMetadataValue(SimplePlugin.getInstance(), turretId));

		arrow.setVelocity(vector);
		Common.runLater(80, arrow::remove);
	}
}


