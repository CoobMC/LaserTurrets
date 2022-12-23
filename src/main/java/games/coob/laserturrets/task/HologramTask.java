package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.util.HologramUtil;
import games.coob.nmsinterface.NMSHologramI;
import games.coob.smp.settings.Settings;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.model.SimpleHologram;

/**
 * Represents a self-repeating task managing hologram.
 */
@RequiredArgsConstructor
public final class HologramTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry registry = TurretRegistry.getInstance();

		for (final TurretData turretData : registry.getRegisteredTurrets()) {
			final HologramUtil hologramUtil = new HologramUtil(turretData.getLocation(),
					"&6" + ChatUtil.capitalize(turretData.getType()) + " Turret",
					"Health: " + turretData.getCurrentHealth(),
					"Level: " + turretData.getCurrentLevel());

			hologramUtil.spawn();


			final SimpleHologram hologram = new SimpleHologram(turretData.getLocation()) {
				@Override
				protected Entity createEntity() {
					final Consumer<ArmorStand> consumer = armorStand -> {
						armorStand.setGravity(false);
						armorStand.setVisible(false);
					};

					return this.getLastTeleportLocation().getWorld().spawn(this.getLastTeleportLocation(), ArmorStand.class, consumer);
				}
			};

			hologram.setLore(
					"&6" + ChatUtil.capitalize(turretData.getType()) + " Turret",
					"Health: " + turretData.getCurrentHealth(),
					"Level: " + turretData.getCurrentLevel());

			// TODO spawn if the turret doesn't have a hologram
		}

		for (final NMSHologramI hologram : registry.getLoadedHolograms()) {
			if (!player.hasMetadata(hologram.getUniqueId().toString()) && registry.isRegistered(hologram))
				showPlayersInRange(hologram, player);

			if (!player.getWorld().equals(hologram.getLocation().getWorld()) || player.getLocation().distance(hologram.getLocation()) > Settings.DeathStorageSection.HOLOGRAM_VISIBLE_RANGE)
				hologram.hide(player);
		}
	}

	/*
	 * Shows the hologram to players within the set range
	 */
	private void showPlayersInRange(final NMSHologramI hologram, final Player player) {
		final Location hologramLocation = hologram.getLocation();
		final Location playerLocation = player.getLocation();

		if (player.getWorld().equals(hologramLocation.getWorld()) && playerLocation.distance(hologramLocation) <= Settings.DeathStorageSection.HOLOGRAM_VISIBLE_RANGE) {
			hologram.show(hologramLocation, player, hologram.getLines());
		}
	}
}