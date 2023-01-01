package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.remain.CompProperty;
import org.mineacademy.fo.remain.Remain;

import java.util.*;

/**
 *
 */
public class Hologram implements ConfigSerializable {

	/**
	 * The distance between each line of lore for this item
	 */
	@Getter
	@Setter
	private static double loreLineHeight = 0.26D;

	/**
	 * A registry of created animated items
	 */
	@Getter
	private static Set<Hologram> registeredItems = new HashSet<>();

	/**
	 * The ticking task responsible for calling {@link #onTick()}
	 */
	private static volatile BukkitTask tickingTask = null;

	/**
	 * The armor stand names, each line spawns another invisible stand
	 */
	@Getter
	private final List<ArmorStand> loreEntities = new ArrayList<>();

	/**
	 * The spawning location
	 */
	private final Location lastTeleportLocation;

	/**
	 * The lore over the item
	 */
	@Getter
	private final List<String> loreLines = new ArrayList<>();


	/**
	 * The displayed entity
	 */
	@Getter
	private Entity entity;

	/*
	 * A private flag to help with teleporting of this entity
	 */
	private Location pendingTeleport = null;

	/*
	 * Constructs a new item and registers it
	 */
	public Hologram(final Location spawnLocation) {
		this.lastTeleportLocation = spawnLocation;

		registeredItems.add(this);

		onReload();
	}

	/**
	 * Restart ticking task on reload
	 *
	 * @deprecated internal use only, do not call
	 */
	@Deprecated
	public static void onReload() {
		if (tickingTask != null)
			tickingTask.cancel();

		tickingTask = scheduleTickingTask();
	}

	/*
	 * Helper method to start main anim ticking task
	 */
	private static BukkitTask scheduleTickingTask() {
		return Common.runTimer(1, () -> {

			for (final Iterator<Hologram> it = registeredItems.iterator(); it.hasNext(); ) {
				final Hologram model = it.next();

				if (model.isSpawned())
					if (!model.getEntity().isValid() || model.getEntity().isDead()) {
						model.removeLore();
						model.getEntity().remove();

						it.remove();
					} else
						model.tick();
			}
		});
	}

	/**
	 * Spawns this hologram entity
	 *
	 * @return
	 */
	public Hologram spawn() {
		Valid.checkBoolean(!this.isSpawned(), this + " is already spawned!");

		this.entity = this.createEntity();
		Valid.checkNotNull(this.entity, "Failed to spawn entity from " + this);

		this.drawLore(this.lastTeleportLocation.clone());

		return this;
	}

	/**
	 * Core implementation method to spawn your entity
	 *
	 * @return
	 */
	private Entity createEntity() {
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_11)) {
			final Consumer<ArmorStand> consumer = armorStand -> {
				armorStand.setMarker(true);
				CompProperty.GRAVITY.apply(armorStand, false);
				armorStand.setSmall(true);
				armorStand.setVisible(false);
			};

			return this.getLastTeleportLocation().getWorld().spawn(this.getLastTeleportLocation(), ArmorStand.class, consumer);
		} else {
			final ArmorStand armorStand = this.getLastTeleportLocation().getWorld().spawn(this.getLastTeleportLocation(), ArmorStand.class);

			CompProperty.GRAVITY.apply(armorStand, false);
			armorStand.setVisible(false);
			armorStand.setMarker(true);
			armorStand.setSmall(true);

			return armorStand;
		}
	}

	/*
	 * Set a lore for this armor stand
	 */
	public void drawLore(Location location) {
		if (this.loreLines.isEmpty())
			return;

		if (this.entity instanceof ArmorStand && ((ArmorStand) this.entity).isSmall())
			location = location.clone().add(0, -0.5, 0);

		for (final String loreLine : this.loreLines) {
			final ArmorStand armorStand;

			if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_11)) {
				final Consumer<ArmorStand> consumer = stand -> {
					stand.setMarker(true);
					CompProperty.GRAVITY.apply(stand, false);
					stand.setSmall(true);
					stand.setVisible(false);
				};

				armorStand = location.getWorld().spawn(location, ArmorStand.class, consumer);
			} else {
				armorStand = location.getWorld().spawn(location, ArmorStand.class);

				armorStand.setMarker(true);
				CompProperty.GRAVITY.apply(armorStand, false);
				armorStand.setSmall(true);
				armorStand.setVisible(false);
			}

			/*armorStand.setGravity(false);
			armorStand.setVisible(false);*/

			Remain.setCustomName(armorStand, loreLine);

			location = location.subtract(0, loreLineHeight, 0);

			this.loreEntities.add(armorStand);
		}
	}

	/*
	 * Iterate the ticking mechanism of this entity
	 */
	private void tick() {

		if (this.pendingTeleport != null) {
			this.entity.teleport(this.pendingTeleport);

			for (final ArmorStand loreEntity : this.loreEntities)
				loreEntity.teleport(this.pendingTeleport);

			this.pendingTeleport = null;
			return;
		}

		this.onTick();
	}

	/**
	 * Called automatically where you can animate this armor stand
	 */
	protected void onTick() {
	}

	/**
	 * Return true if this armor stand is spawned
	 *
	 * @return
	 */
	public final boolean isSpawned() {
		return this.entity != null && this.entity.isValid();
	}

	/**
	 * Deletes all text that the armor stand has
	 */
	public final void removeLore() {
		this.loreEntities.forEach(ArmorStand::remove);
	}

	/**
	 * @param lore
	 */
	public final void setLore(final String... lore) {
		this.loreLines.clear();
		this.loreLines.addAll(Arrays.asList(lore));

	}

	/**
	 * Return the current armor stand location
	 *
	 * @return
	 */
	public final Location getLocation() {
		this.checkSpawned("getLocation");

		return this.entity.getLocation();
	}

	/**
	 * Return the last known teleport location
	 *
	 * @return
	 */
	public final Location getLastTeleportLocation() {
		return this.lastTeleportLocation.clone();
	}

	/**
	 * Teleport this hologram with its lores to the given location
	 *
	 * @param location
	 */
	public final void teleport(final Location location) {
		Valid.checkBoolean(this.pendingTeleport == null, this + " is already pending teleport to " + this.pendingTeleport);
		this.checkSpawned("teleport");

		this.lastTeleportLocation.setX(location.getY());
		this.lastTeleportLocation.setY(location.getY());
		this.lastTeleportLocation.setZ(location.getZ());

		this.pendingTeleport = location;
	}

	/**
	 * Deletes this armor stand
	 */
	public final void remove() {
		this.removeLore();

		if (this.entity != null)
			this.entity.remove();

		registeredItems.remove(this);
	}

	public final void update(final TurretData turretData) {
		this.remove();
		this.setLore(Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth()));
		this.spawn();
	}

	/*
	 * A helper method to check if this entity is spawned
	 */
	private void checkSpawned(final String method) {
		Valid.checkBoolean(this.isSpawned(), this + " is not spawned, cannot call " + method + "!");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ArmorStandItem{spawnLocation=" + Common.shortLocation(this.lastTeleportLocation) + ", spawned=" + this.isSpawned() + "}";
	}

	/**
	 * Deletes all floating items on the server
	 */
	public static void deleteAll() {
		final Set<Hologram> holograms = new HashSet<>();

		for (final TurretData turretData : TurretRegistry.getInstance().getRegisteredTurrets())
			holograms.add(turretData.getHologram());

		for (final Iterator<Hologram> it = holograms.iterator(); it.hasNext(); ) {
			final Hologram item = it.next();

			item.remove();
			it.remove();
		}
	}

	public static Hologram deserialize(final SerializedMap map) {
		final Location location = map.getLocation("Location");
		final String[] lines = map.getStringList("Lines").toArray(new String[0]);

		final Hologram hologram = new Hologram(location.clone().add(0.5, 2.5, 0.5));

		hologram.setLore(lines);

		return hologram;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("Location", this.lastTeleportLocation);
		map.put("Lore", this.loreLines);

		return map;
	}
}
