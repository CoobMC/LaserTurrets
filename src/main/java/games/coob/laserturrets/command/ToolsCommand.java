package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.ToolsMenu;
import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.FlameTurretTool;
import games.coob.laserturrets.tools.LaserTurretTool;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class ToolsCommand extends SimpleSubCommand {

	ToolsCommand() {
		super("tool");

		setDescription("Chose a tool to set up your turrets!");
		setPermission("laserturrets.command.tool");
		setMinArguments(1);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		if (args.length == 0) {
			new ToolsMenu().displayTo(player);
		} else {
			final String param = args[0];

			if ("arrow_turret".equals(param))
				ArrowTurretTool.getInstance().give(player);
			else if ("laser_turret".equals(param))
				LaserTurretTool.getInstance().give(player);
			else if ("flame_turret".equals(param))
				FlameTurretTool.getInstance().give(player);
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1)
			return this.completeLastWord("arrow_turret", "laser_turret", "flame_turret");

		return NO_COMPLETE;
	}
}
