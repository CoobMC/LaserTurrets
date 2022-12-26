package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.SimpleHologram;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.EntityUtil;

/**
 * Represents a self-repeating task managing hologram.
 */
@RequiredArgsConstructor
public final class HologramTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry registry = TurretRegistry.getInstance();

		for (final TurretData turretData : registry.getRegisteredTurrets()) {
			final SimpleHologram hologram = turretData.getHologram();
			final Player player = EntityUtil.findNearestEntity(turretData.getLocation(), 40, Player.class);

			if (player != null && !hologram.isSpawned())
				hologram.spawn();
		}
	}
}