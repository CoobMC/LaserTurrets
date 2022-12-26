package games.coob.laserturrets.tools;

import games.coob.laserturrets.util.Lang;
import lombok.Getter;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;

@AutoRegister
public final class BeamTurret extends TurretTool {

	@Getter
	private static final Tool instance = new BeamTurret();

	private BeamTurret() {
		super("beam", Lang.of("Placeholders.Beam"), true);
	}
}
