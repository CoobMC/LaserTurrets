package games.coob.laserturrets.tools;

import games.coob.laserturrets.util.Lang;
import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;

@AutoRegister
public final class FireballTurret extends TurretTool {

    @Getter
    private static final Tool instance = new FireballTurret();

    private FireballTurret() {
        super("fireball", Lang.of("Placeholders.Fireball"), true);
    }
}
