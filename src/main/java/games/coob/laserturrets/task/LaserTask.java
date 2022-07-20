package games.coob.laserturrets.task;

import games.coob.laserturrets.model.Laser;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

public class LaserTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getLaserTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretData.getCurrentLevel();
			final int range = turretRegistry.getTurretRange(block, level);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, range, LivingEntity.class, block);

			if (nearestEntity == null)
				continue;

			final Location turretLocation = location.clone().add(0.5, 1.2, 0.5);

			try {
				final Laser laser = new Laser.GuardianLaser(turretLocation, nearestEntity, 1, 40);
				turretLocation.getWorld().playSound(turretLocation, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.4F, 0.2F);
				laser.start(SimplePlugin.getInstance());
				nearestEntity.damage(1);
				Common.runLater(10, () -> nearestEntity.damage(1));
			} catch (final ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}
}



