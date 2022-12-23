package games.coob.laserturrets.menu;

import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.BeamTurretTool;
import games.coob.laserturrets.tools.FireballTurretTool;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.menu.MenuTools;
import org.mineacademy.fo.settings.Lang;

public class ToolsMenu extends MenuTools {
	public ToolsMenu() {
		this.setTitle(Lang.of("Tools_Menu.Menu_Title"));
	}

	@Override
	protected Object[] compileTools() {
		return new Object[]{
				ArrowTurretTool.class, FireballTurretTool.class, MinecraftVersion.atLeast(MinecraftVersion.V.v1_9) ? BeamTurretTool.class : NO_ITEM
		};
	}

	@Override
	protected String[] getInfo() {
		return Lang.ofArray("Tools_Menu.Info_Button");
	}
}
