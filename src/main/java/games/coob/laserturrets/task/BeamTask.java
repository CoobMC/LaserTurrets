package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Beam_v1_8;
import games.coob.laserturrets.util.EntityUtil;
import games.coob.laserturrets.util.Laser;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompSound;

public class BeamTask extends BukkitRunnable {

	@Override
	public void run() {
		for (final TurretData turretData : TurretData.getTurretsOfType("beam")) {
			if (turretData.isBroken())
				continue;

			final Location location = turretData.getLocation();
			final Location shootLocation = location.clone().add(0.5, 1.4, 0.5);
			final int level = turretData.getCurrentLevel();
			final int range = turretData.getLevel(level).getRange();
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(shootLocation, range, LivingEntity.class, location.getBlock());

			if (nearestEntity == null)
				continue;

			final Location turretLocation = location.clone().add(0.5, 1.2, 0.5);

			try {
				if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9)) {
					final Laser beam = new Laser.GuardianLaser(turretLocation, nearestEntity, 1, 40);
					beam.start(SimplePlugin.getInstance());
					CompSound.ENTITY_ELDER_GUARDIAN_CURSE.play(turretLocation, 0.4F, 0.2F);
				} else {
					final Beam_v1_8 beam_v1_8 = new Beam_v1_8(turretLocation, nearestEntity, 1, 40);
					beam_v1_8.start();
					CompSound.WITHER_IDLE.play(turretLocation, 0.4F, 2F);
				}

				nearestEntity.damage(1);
				Common.runLater(10, () -> nearestEntity.damage(1));
			} catch (final ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}
}



