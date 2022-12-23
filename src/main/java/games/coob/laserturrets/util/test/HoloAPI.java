package games.coob.laserturrets.util.test;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * @author Janhektor
 * @version 1.4 (December 30, 2015)
 */
public interface HoloAPI { // TODO 1.8

	/**
	 * Shows this hologram to the given player
	 *
	 * @param p The player who will see this hologram at the location specified by calling the constructor
	 * @return true if the action was successful, else false
	 */
	boolean display(Player player);

	/**
	 * Removes this hologram from the players view
	 *
	 * @param p The target player
	 * @return true if the action was successful, else false (including the try to remove a non-existing hologram)
	 */
	boolean destroy(Player player);

	/**
	 * Create a new hologram
	 * Note: The internal cache will be automatically initialized, it may take some millis
	 *
	 * @param loc   The location where this hologram is shown
	 * @param lines The text-lines, from top to bottom, farbcodes are possible
	 */
	public static HoloAPI newInstance(final Location location, final String... lines) {
		return newInstance(location, Arrays.asList(lines));
	}

	/**
	 * Create a new hologram
	 * Note: The internal cache will be automatically initialized, it may take some millis
	 *
	 * @param loc   The location where this hologram is shown
	 * @param lines The text-lines, from top to bottom, farbcodes are possible
	 */
	public static HoloAPI newInstance(final Location location, final List<String> lines) {
		return new DefaultHoloAPI(location, lines);
	}
}