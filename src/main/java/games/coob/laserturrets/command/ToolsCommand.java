package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.ToolsMenu;
import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.BeamTurretTool;
import games.coob.laserturrets.tools.FireballTurretTool;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class ToolsCommand extends SimpleSubCommand {

	ToolsCommand() {
		super("tool|tools");

		setDescription("Chose a tool to set up your turrets!");
		setPermission("laserturrets.tool");
		setPermission("laserturrets.admin");
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		if (args.length == 0)
			new ToolsMenu().displayTo(player);
		else {
			final String param = args[0];

			if ("arrow_turret".equals(param))
				ArrowTurretTool.getInstance().give(player);
			else if ("beam_turret".equals(param))
				if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
					BeamTurretTool.getInstance().give(player);
				else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
			else if ("fireball_turret".equals(param))
				FireballTurretTool.getInstance().give(player);
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1)
			return this.completeLastWord("arrow_turret", "beam_turret", "fireball_turret");

		return NO_COMPLETE;
	}
}
