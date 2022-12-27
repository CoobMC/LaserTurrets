package games.coob.laserturrets.sequence;

import games.coob.laserturrets.util.SimpleHologramStand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a hologram stand that rotates up and down around a certain center rotation
 * as well as rotating around its axis and then back once it hits the bottom/up limit
 * of the vertical movement.
 */
@Getter
@Setter
public class AnimatedHologram extends SimpleHologramStand {

	/**
	 * Is the animation enabled?
	 */
	private boolean animated = true;

	/**
	 * Is this stand rotating up and down?
	 */
	private boolean rotatingUpAndDown = true;

	/**
	 * Is this stand rotating to sides?
	 */
	private boolean rotatingToSides = true;

	/**
	 * How far down or up (in blocks) can this stand
	 * move from its initial spawn location when animated?
	 */
	private double verticalMovementThreshold = 2;

	/*
	 * A private flag to determine if we're going up or down.
	 */
	private boolean motionDown = true;

	/**
	 * Create a new simple hologram using armor stand showing the given material
	 */
	public AnimatedHologram(final Location spawnLocation, final ItemStack itemStack) {
		super(spawnLocation, itemStack);
	}

	/**
	 * Called automatically each tick for this stand.
	 */
	@Override
	protected void onTick() {
		final Location location = this.getLocation();

		if (this.isAnimated()) {

			if (this.isRotatingUpAndDown()) {
				final double y = location.getY();
				final double lastY = this.getLastTeleportLocation().getY();

				// We hit the bottom
				if (y < lastY - this.verticalMovementThreshold)
					this.motionDown = false;

				// We hit the top
				if (y > lastY + this.verticalMovementThreshold)
					this.motionDown = true;

				location.subtract(0, 0.01 * (this.motionDown ? 4 : -4), 0);
			}

			if (this.isRotatingToSides())
				location.setYaw(location.getYaw() + 7 * (this.motionDown ? -4 : 4));

			this.getEntity().teleport(location);
		}
	}
}
