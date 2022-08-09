package games.coob.laserturrets.sequence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a simple sequence. A sequence is an animated series of scenes.
 */
public abstract class Sequence {

	/**
	 * Holds all registered sequences by name. This is initialized the first
	 * time you call this class in a static way.
	 */
	private static final Map<String, Sequence> byName = new HashMap<>();

	/**
	 * Pre-loaded crate drop sequence.
	 */
	public static final Sequence TURRET_CREATION = new TurretCreationSequence();

	/**
	 * How long to wait between each scene? Defaults to 20 ticks (1 second)
	 */
	@Setter(AccessLevel.PROTECTED)
	private int sceneDelayTicks = 20;

	/**
	 * A dynamic variable storing the last location (of a last animated stand for example)
	 * for convenient use later, this is initialized in the {@link #start(Location)} method.
	 */
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	private Location lastLocation;

	/**
	 * A dynamic variable storing the last armor stand we spawned for convenience.
	 */
	@Getter(AccessLevel.PROTECTED)
	private AnimatedHologram lastStand;

	/*
	 * Creates and registers a new sequence.
	 */
	protected Sequence(final String name) {
		byName.put(name, this);
	}

	/* ------------------------------------------------------------------------------- */
	/* Sequence operations */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Starts playing this sequence at the given location
	 *
	 * @param location
	 */
	public final void start(final Location location) {
		this.lastLocation = location;

		this.onStart();
	}

	/**
	 * Called automatically on sequence start, you should start playing your
	 * first scene here.
	 */
	protected abstract void onStart();

	/**
	 * Starts playing the next sequence
	 *
	 * @param runnable
	 */
	protected final void nextSequence(final Runnable runnable) {
		Common.runLater(this.sceneDelayTicks, runnable);
	}

	/**
	 * Perfom cleaning up logic when the sequence should stop prematurelly.
	 */
	public void disable() {
	}

	/* ------------------------------------------------------------------------------- */
	/* Helper methods */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Spawns a glowing hologram stand at the last location with the given material.
	 *
	 * @param material
	 */
	protected final void glowingStand(final CompMaterial material) {
		this.stand(material, true, false);
	}

	/**
	 * Spawns an animated hologram stand at the last location with the given material.
	 *
	 * @param material
	 */
	protected final void animatedStand(final CompMaterial material) {
		this.stand(material, false, true);
	}

	/**
	 * Spawns a glowing animated hologram stand at the last location with the given material.
	 *
	 * @param material
	 */
	protected final void animatedGlowingStand(final CompMaterial material) {
		this.stand(material, true, true);
	}

	/*
	 * A helper method to spawn an animated hologram stand.
	 */
	private void stand(final CompMaterial material, final boolean glow, final boolean animated) {
		final AnimatedHologram stand = new AnimatedHologram(this.lastLocation, material);

		stand.setGlowing(glow);
		stand.setAnimated(animated);
		stand.setLore(ItemUtil.bountifyCapitalized(material));

		stand.spawn();

		// Set invisible metadata tag to entity.
		// This tag is removed upon restart, but so is the entity thanks to our disable() method.
		stand.getEntity().setMetadata("AnimatedStand", new FixedMetadataValue(SimplePlugin.getInstance(), ""));

		this.lastStand = stand;
	}

	/**
	 * Strikes lightning effect with no damage at the last location.
	 */
	protected final void lightning() {
		this.lastLocation.getWorld().strikeLightningEffect(this.lastLocation);
	}

	/**
	 * Removes the last stand if it exists.
	 */
	protected final void removeLast() {
		if (this.lastStand != null)
			this.lastStand.remove();
	}

	/* ------------------------------------------------------------------------------- */
	/* Static */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Automatically called upon reload.
	 */
	public static void reload() {
		for (final Sequence sequence : byName.values())
			sequence.disable();
	}

	/**
	 * Finds a sequence by name, returns null if not found.
	 *
	 * @param name
	 * @return
	 */
	public static Sequence getByName(final String name) {
		return byName.get(name);
	}

	/**
	 * Return all sequence names.
	 *
	 * @return
	 */
	public static Set<String> getSequenceNames() {
		return byName.keySet();
	}
}
