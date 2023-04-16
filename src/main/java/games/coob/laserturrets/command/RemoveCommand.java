package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Lang;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.TabUtil;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;
import java.util.Set;

/**
 * A sample command belonging to a command group.
 */
final class RemoveCommand extends SimpleSubCommand {

	RemoveCommand() {
		super("remove");

		setDescription(Lang.of("Turret_Commands.Remove_Description"));
		setUsage("<id>");
		setPermission(Permissions.Command.REMOVE);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();
		String turretId = null;
		TurretData turretData = null;
		Block block = null;

		if (this.args.length == 1) {
			turretId = args[0];
			turretData = TurretData.findById(turretId);
		} else if (this.args.length == 0) {
			block = player.getTargetBlock(null, 5);
			turretData = TurretData.findByBlock(block);
		}

		if (turretId == null) {
			assert turretData != null;

			if (TurretData.isRegistered(block)) {
				turretData.unregister();
				Messenger.success(player, Lang.of("Turret_Commands.Remove_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
			} else
				Messenger.error(player, Lang.of("Turret_Commands.Error_Not_Looking_At_Turret"));
		} else {
			if (TurretData.isTurretLoaded(turretId)) {
				turretData.unregister();
				Messenger.success(player, Lang.of("Turret_Commands.Remove_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
			} else
				Messenger.error(player, Lang.of("Turret_Commands.Turret_ID_Does_Not_Exist", "{invalidID}", turretId));
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeTurretIDs();

		return NO_COMPLETE;
	}

	private List<String> completeTurretIDs() {
		final Set<String> turretIDs = TurretData.getTurretIDs();

		return TabUtil.complete(this.getLastArg(), turretIDs);
	}
}
