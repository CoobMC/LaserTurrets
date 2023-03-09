package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class GiveCommand extends SimpleSubCommand {

	GiveCommand() {
		super("give");

		setDescription(Lang.of("Turret_Commands.Give_Description"));
		setUsage("<give> <turret_type> <player>");
		setPermission(Permissions.Command.GIVE);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		if (args.length == 0)
			returnInvalidArgs();

		final String type = args[0];

		if (args.length == 1) {
			giveTurret(type, getPlayer());
		} else if (args.length == 2) {
			final String playerName = args[1];
			final Player player = Bukkit.getPlayer(playerName);

			if (player == null)
				returnTell(Lang.of("Turret_Commands.Player_Non_Existent"));

			giveTurret(type, player);
		} else returnInvalidArgs();
	}

	private void giveTurret(final String type, final Player player) {
		if ("arrow".equals(type))
			ArrowTurret.getInstance().give(player);
		else if ("beam".equals(type))
			BeamTurret.getInstance().give(player);
		else if ("fireball".equals(type))
			FireballTurret.getInstance().give(player);
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord("arrow", "beam", "fireball");

		if (this.args.length == 2)
			return completeLastWordPlayerNames();

		return NO_COMPLETE;
	}
}
