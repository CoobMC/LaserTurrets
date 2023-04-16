package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.remain.Remain;

/**
 * Represents a self-repeating task managing hologram.
 */
@RequiredArgsConstructor
public final class HologramTask extends BukkitRunnable {

	@Override
	public void run() {
		for (final TurretData turretData : TurretData.getRegisteredTurrets()) {
			final Hologram hologram = turretData.getHologram();

			if (hologram == null)
				continue;

			final Player player = EntityUtil.findNearestEntity(turretData.getLocation(), 40, Player.class);

			if (player != null && !hologram.isSpawned()) {
				if (hologram.getLoreLines().isEmpty())
					hologram.setLore(Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth()));

				hologram.spawn();
			}
		}
	}
}