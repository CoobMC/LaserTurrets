package games.coob.laserturrets.tools;

import games.coob.laserturrets.util.Lang;
import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;

@AutoRegister
public final class ArrowTurret extends TurretTool {

    @Getter
    private static final Tool instance = new ArrowTurret();

    private ArrowTurret() {
        super("arrow", Lang.of("Placeholders.Arrow"), true);
    }
}
