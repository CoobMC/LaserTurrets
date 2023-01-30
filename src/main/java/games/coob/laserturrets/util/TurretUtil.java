package games.coob.laserturrets.util;

import org.mineacademy.fo.menu.model.SkullCreator;

public class TurretUtil {
	public static String getDisplayName(final String turretType) {
		switch (turretType) {
			case "all":
				return Lang.of("Placeholders.All");
			case "arrow":
				return Lang.of("Placeholders.Arrow");
			case "beam":
				return Lang.of("Placeholders.Beam");
			case "fireball":
				return Lang.of("Placeholders.Fireball");
		}

		return null;
	}

	public static String capitalizeWord(final String word) {
		final String capitalizedWord;

		if (word.charAt(0) == '&')
			capitalizedWord = word.substring(0, 2) + word.substring(2, 3).toUpperCase() + word.substring(3);
		else if (word.charAt(2) == '&')
			capitalizedWord = word.substring(0, 4) + word.substring(4, 5).toUpperCase() + word.substring(5);
		else capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);

		return capitalizedWord;
	}

	public static boolean isBase64ValueValid(final String value) {
		try {
			SkullCreator.itemFromBase64(value);
		} catch (final StringIndexOutOfBoundsException e) {
			return false;
		}

		return true;
	}

	public static double getYForLines(final int numberOfLines) { // TODO
		return (numberOfLines / 6.0) + 2;
	}
}
