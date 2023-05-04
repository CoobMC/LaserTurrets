package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.remain.CompMaterial;

public class TurretUtil {
	public static String getDisplayName(final String turretType) {
		return switch (turretType) {
			case "all" -> Lang.of("Placeholders.All");
			case "arrow" -> Lang.of("Placeholders.Arrow");
			case "beam" -> Lang.of("Placeholders.Beam");
			case "fireball" -> Lang.of("Placeholders.Fireball");
			default -> null;
		};

	}

	public static void updateHologramAndTexture(final TurretData turretData) {
		final String type = turretData.getType();
		final TurretSettings settings = TurretSettings.findByName(type);
		final Block skullBlock = turretData.getLocation().getBlock().getRelative(BlockFace.UP);

		if (CompMaterial.isSkull(skullBlock.getType())) {
			final Skull state = (Skull) skullBlock.getState();
			games.coob.laserturrets.util.SkullCreator.mutateBlockState(state, settings.getHeadTexture());
			state.update(false, false);
		}

		if (Settings.TurretSection.DISPLAY_HOLOGRAM)
			TurretData.findById(turretData.getId()).updateHologram();
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
