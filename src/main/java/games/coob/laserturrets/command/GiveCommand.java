package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
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
		if ("arrow_turret".equals(type))
			ArrowTurret.getInstance().give(player);
		else if ("beam_turret".equals(type))
			if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
				BeamTurret.getInstance().give(player);
			else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
		else if ("fireball_turret".equals(type))
			FireballTurret.getInstance().give(player);
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord("arrow_turret", "beam_turret", "fireball_turret");

		if (this.args.length == 2)
			return completeLastWordPlayerNames();

		return NO_COMPLETE;
	}
}
