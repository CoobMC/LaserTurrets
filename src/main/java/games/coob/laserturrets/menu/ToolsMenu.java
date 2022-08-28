package games.coob.laserturrets.menu;

import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.BeamTurretTool;
import games.coob.laserturrets.tools.FireballTurretTool;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.menu.MenuTools;

public class ToolsMenu extends MenuTools {
	public ToolsMenu() {
		this.setTitle("Turret Tools");
	}

	@Override
	protected Object[] compileTools() {
		return new Object[]{
				ArrowTurretTool.class, FireballTurretTool.class, MinecraftVersion.atLeast(MinecraftVersion.V.v1_9) ? BeamTurretTool.class : NO_ITEM
		};
	}

	@Override
	protected String[] getInfo() {
		return new String[]{
				"Click a tool to get it!",
				"You can then register turrets",
				"by clicking blocks with these",
				"tools."
		};
	}
}
