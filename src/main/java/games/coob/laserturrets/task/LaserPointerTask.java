package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompParticle;

public class LaserPointerTask extends BukkitRunnable {

	@Override
	public void run() {
		for (final TurretData turretData : TurretData.getTurrets()) {
			if (turretData.isBroken())
				continue;

			final TurretSettings settings = TurretSettings.findByName(turretData.getType());
			final int level = turretData.getCurrentLevel();

			if (settings == null || settings.getLevel(level) == null)
				continue;

			if (!settings.getLevel(level).isLaserEnabled())
				continue;

			final Location location = turretData.getLocation();
			final Location shootLocation = location.clone().add(0.5, 1.4, 0.5);
			final int range = settings.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(shootLocation, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			final double damage = settings.getLevel(level).getLaserDamage();
			final Location laserLocation = location.clone().add(0.5, 1.2, 0.5);
			final Location targetLocation = nearestEntity.getEyeLocation();
			final double distance = location.clone().add(0.5, 1.2, 0.5).distance(targetLocation);
			final Vector vector = targetLocation.subtract(laserLocation).toVector().normalize().multiply(0.5);

			for (double waypoint = 1; waypoint < distance + 0.5; waypoint += 0.5) {
				laserLocation.add(vector);
				CompParticle.REDSTONE.spawn(laserLocation);
			}

			if (settings.getLevel(level).isLaserEnabled() && damage > 0) {
				nearestEntity.setMetadata("TurretDamage", new FixedMetadataValue(SimplePlugin.getInstance(), turretData.getId()));
				nearestEntity.damage(damage);
			}
		}
	}
}



