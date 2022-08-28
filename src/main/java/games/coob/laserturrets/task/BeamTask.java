package games.coob.laserturrets.task;

import games.coob.laserturrets.model.Beam;
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

public class BeamTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getBeamTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretData.getCurrentLevel();
			final int range = turretRegistry.getTurretRange(block, level);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntityNonBlacklisted(location, range, LivingEntity.class, block);

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



