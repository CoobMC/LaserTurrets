package games.coob.laserturrets.tools;

import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.settings.Lang;

@AutoRegister
public final class FireballTurretTool extends TurretTool {

	@Getter
	private static final Tool instance = new FireballTurretTool();

	private FireballTurretTool() {
		super("fireball", Lang.of("Placeholders.Fireball"), false);
	}
}
