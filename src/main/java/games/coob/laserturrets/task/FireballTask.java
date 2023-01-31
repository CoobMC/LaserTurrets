package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SmallFireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;

public class FireballTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final TurretData turretData : turretRegistry.getTurretsOfType("fireball")) {
			if (turretData.isBroken())
				continue;

			final Location location = turretData.getLocation();
			final Location shootLocation = location.clone().add(0.5, 1.4, 0.5);
			final Block block = location.getBlock();
			final int level = turretData.getCurrentLevel();
			final int range = turretData.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(shootLocation, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			shootFireball(nearestEntity, block);
		}
	}

	private void shootFireball(final LivingEntity target, final Block block) {
		if (target != null) {
			final Location blockLocation = block.getLocation().clone().add(0.5, 1.4, 0.5);
			final Location targetLocation = target.getEyeLocation().clone();
			final Vector vector = targetLocation.subtract(blockLocation).toVector().normalize();
			final SmallFireball fireball = block.getWorld().spawn(blockLocation, SmallFireball.class);

			fireball.setDirection(vector);
			fireball.setIsIncendiary(false);

			for (int i = 0; i <= 10; i++) {
				if (fireball.getLocation().getBlock().getType() != Material.AIR) {
					fireball.teleport(fireball.getLocation().add(vector.clone().normalize().multiply(0.08)));

					if (block.getLocation().distance(target.getLocation()) < 1.6)
						target.setVelocity(vector.clone().multiply(0.25));
				}
			}

			Common.runLater(100, fireball::remove);
		}
	}
}


