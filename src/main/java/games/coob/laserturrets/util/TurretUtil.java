package games.coob.laserturrets.util;

import org.mineacademy.fo.settings.Lang;

public class TurretUtil {
	public static String getDisplayName(final String turretType) {
		switch (turretType) {
			case "arrow":
				return Lang.of("Placeholders.Arrow");
			case "beam":
				return Lang.of("Placeholders.Beam");
			case "fireball":
				return Lang.of("Placeholders.Fireball");
		}

		return null;
	}
}
