package games.coob.laserturrets.tools;

import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.settings.Lang;

@AutoRegister
public final class FireballTurret extends TurretTool {

	@Getter
	private static final Tool instance = new FireballTurret();

	private FireballTurret() {
		super("fireball", Lang.of("Placeholders.Fireball"), true);
	}
}
