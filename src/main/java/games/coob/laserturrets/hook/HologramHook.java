package games.coob.laserturrets.hook;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

public class HologramHook {

    public static void createHologram(final TurretData turretData) {
        final Hologram hologram = DHAPI.createHologram(turretData.getId(), turretData.getLocation().clone().add(0.5, 2.65, 0.5), true);
        final List<String> lore = Lang.ofList("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
        DHAPI.setHologramLines(hologram, lore);
        hologram.showAll();
    }

    public static void removeHologram(final String id) {
        final Hologram hologram = DHAPI.getHologram(id);

        if (hologram != null)
            hologram.delete();
    }
}


