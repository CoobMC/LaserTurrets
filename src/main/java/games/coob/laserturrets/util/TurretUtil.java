package games.coob.laserturrets.util;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.TurretSettings;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.remain.CompMaterial;

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
            default:
                return null;
        }

    }

    public static void updateTexture(final TurretData turretData) {
        final String type = turretData.getType();
        final TurretSettings settings = TurretSettings.findByName(type);
        final Block skullBlock = turretData.getLocation().getBlock().getRelative(BlockFace.UP);

        if (CompMaterial.isSkull(skullBlock.getType())) {
            final Skull state = (Skull) skullBlock.getState();
            SkullCreator.mutateBlockState(state, settings.getHeadTexture());
            state.update(false, false);
        }
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
}
