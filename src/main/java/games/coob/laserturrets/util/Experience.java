package games.coob.laserturrets.util;

import org.bukkit.entity.Player;

/**
 * A utility for managing Player experience properly.
 *
 * @author Jikoo
 */
public class Experience { // TODO check if needed

	/**
	 * Calculates a player's total exp based on level and progress to next.
	 *
	 * @param player the Player
	 * @return the amount of exp the Player has
	 * @see <a href="http://minecraft.gamepedia.com/Experience#Leveling_up">...</a>
	 */
	public static int getExp(final Player player) {
		return getExpFromLevel(player.getLevel())
				+ Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * Calculates total experience based on level.
	 *
	 * @param level the level
	 * @return the total experience calculated
	 * @see <a href="http://minecraft.gamepedia.com/Experience#Leveling_up">...</a>
	 * <p>
	 * "One can determine how much experience has been collected to reach a level using the equations:
	 * <p>
	 * Total Experience = [Level]2 + 6[Level] (at levels 0-15)
	 * 2.5[Level]2 - 40.5[Level] + 360 (at levels 16-30)
	 * 4.5[Level]2 - 162.5[Level] + 2220 (at level 31+)"
	 */
	public static int getExpFromLevel(final int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	/**
	 * Calculates level based on total experience.
	 *
	 * @param exp the total experience
	 * @return the level calculated
	 */
	public static double getLevelFromExp(final long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}

	/**
	 * @see <a href="http://minecraft.gamepedia.com/Experience#Leveling_up">...</a>
	 * <p>
	 * "The formulas for figuring out how many experience orbs you need to get to the next level are as follows:
	 * Experience Required = 2[Current Level] + 7 (at levels 0-15)
	 * 5[Current Level] - 38 (at levels 16-30)
	 * 9[Current Level] - 158 (at level 31+)"
	 */
	private static int getExpToNext(final int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}

	/**
	 * Change a Player's exp.
	 * <p>
	 * This method should be used in place of {@link Player#giveExp(int)}, which does not properly
	 * account for different levels requiring different amounts of experience.
	 *
	 * @param player the Player affected
	 * @param exp    the amount of experience to add or remove
	 */
	public static void changeExp(final Player player, int exp) {
		exp += getExp(player);

		if (exp < 0)
			exp = 0;

		final double levelAndExp = getLevelFromExp(exp);

		final int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}

	public static int getExpToNext(final Player player) {
		return getExpFromLevel(player.getLevel() + 1) - getExp(player);
	}

}