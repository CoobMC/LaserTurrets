package games.coob.laserturrets.menu;

import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.FlameTurretTool;
import games.coob.laserturrets.tools.LaserTurretTool;
import org.mineacademy.fo.menu.MenuTools;

public class ToolsMenu extends MenuTools {
	public ToolsMenu() {
		this.setTitle("Turret Tools");
	}

	@Override
	protected Object[] compileTools() {
		return new Object[]{
				ArrowTurretTool.class, FlameTurretTool.class, LaserTurretTool.class
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
