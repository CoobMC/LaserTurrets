package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;

public class FireballTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final TurretData turretData : turretRegistry.getFlameTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretData.getCurrentLevel();
			final int range = turretRegistry.getTurretRange(block, level);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, range, LivingEntity.class, block);

			if (nearestEntity == null)
				continue;

			shootFireball(nearestEntity, block);
		}
	}

	private void shootFireball(final LivingEntity target, final Block block) {
		if (target != null) {
			final Location blockLocation = block.getLocation().add(0.5, 1.5, 0.5);
			final Location targetLocation = target.getLocation().clone().add(0, 1, 0);
			final Vector vector = targetLocation.subtract(blockLocation).toVector().normalize().multiply(1);
			final Fireball fireball = block.getWorld().spawn(blockLocation, Fireball.class);

			fireball.setDirection(vector); // TODO prevent fireball from exploding on spawn
			fireball.setYield(0);
			Common.runLater(80, fireball::remove);
		}
	}
}


