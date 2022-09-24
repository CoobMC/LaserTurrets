package games.coob.laserturrets.tools;

import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;

@AutoRegister
public final class FireballUseTurretTool extends TurretTool {

	@Getter
	private static final Tool instance = new FireballUseTurretTool();

	private FireballUseTurretTool() {
		super("fireball", true);
	}
}
