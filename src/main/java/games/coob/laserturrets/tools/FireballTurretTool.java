package games.coob.laserturrets.tools;

import games.coob.laserturrets.util.Lang;
import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;

@AutoRegister
public final class FireballTurretTool extends TurretTool {

	@Getter
	private static final Tool instance = new FireballTurretTool();

	private FireballTurretTool() {
		super("fireball", Lang.of("Placeholders.Fireball"), false);
	}
}
