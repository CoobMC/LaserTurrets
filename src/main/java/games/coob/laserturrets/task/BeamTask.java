package games.coob.laserturrets.task;

import games.coob.laserturrets.model.Beam;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.EntityUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

public class BeamTask extends BukkitRunnable {

	@Override
	public void run() { // TODO
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getTurretsOfType("beam")) {
			if (turretData.isBroken())
				continue;

			final Location location = turretData.getLocation();
			final int level = turretData.getCurrentLevel();
			final int range = turretData.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, range, LivingEntity.class);

			if (nearestEntity == null)
				continue;

			final Location turretLocation = location.clone().add(0.5, 1.2, 0.5);

			try {
				final Beam beam = new Beam.GuardianBeam(turretLocation, nearestEntity, 1, 40);
				beam.start(SimplePlugin.getInstance());

				turretLocation.getWorld().playSound(turretLocation, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.4F, 0.2F);
				nearestEntity.damage(1);
				Common.runLater(10, () -> nearestEntity.damage(1));
			} catch (final ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}
}



