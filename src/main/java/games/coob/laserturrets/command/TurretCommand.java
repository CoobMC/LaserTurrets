package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.ToolsMenu;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.tools.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class TurretCommand extends SimpleSubCommand {

	TurretCommand() {
		super("turret|turrets");

		setDescription("Give a tool that can create an infinite amount of turrets or give a turret that can be placed directly.");
		setUsage("<give_tool|give_turret> <turret_type> <player>");
		setPermission(Permissions.Command.TOOL);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		if (args.length == 0)
			new ToolsMenu().displayTo(getPlayer());
		else {
			final String param = args[0];

			if (args.length == 1)
				returnTell("Wrong usage of command (/lt turret <tool/give> <player>).");

			final String type = args[1];

			if (args.length == 2) {
				giveTurret(param, type, getPlayer());
			} else if (args.length == 3) {
				final String playerName = args[2];
				final Player player = Bukkit.getPlayer(playerName);

				if (player == null)
					returnTell("The player '" + playerName + "' does not exist.");

				giveTurret(param, type, player);
			}
		}
	}

	private void giveTurret(final String param, final String type, final Player player) {
		if (param.equals("tool")) {
			if ("arrow_turret".equals(type))
				ArrowTurretTool.getInstance().give(player);
			else if ("beam_turret".equals(type))
				if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
					BeamTurretTool.getInstance().give(player);
				else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
			else if ("fireball_turret".equals(type))
				FireballTurretTool.getInstance().give(player);
		} else if (param.equals("give")) {
			if ("arrow_turret".equals(type))
				ArrowTurret.getInstance().give(player);
			else if ("beam_turret".equals(type))
				if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
					BeamTurret.getInstance().give(player);
				else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
			else if ("fireball_turret".equals(type))
				FireballTurret.getInstance().give(player);
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord("tool", "give");

		if (this.args.length == 2)
			return this.completeLastWord("arrow_turret", "beam_turret", "fireball_turret");

		if (this.args.length == 3)
			return completeLastWordPlayerNames();

		return NO_COMPLETE;
	}
}
