package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;
import java.util.Set;

/**
 * A sample command belonging to a command group.
 */
final class SyncCommand extends SimpleSubCommand {

	SyncCommand() {
		super("sync");

		setDescription(Lang.of("Turret_Commands.Sync_Description"));
		setUsage("<sync> <turret_type>");
		setPermission(Permissions.Command.BUY);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final TurretRegistry registry = TurretRegistry.getInstance();

		if (args.length == 0)
			returnInvalidArgs();

		final String type = args[0];
		final TurretSettings settings = TurretSettings.findByName(type);
		final Set<TurretData> turretDataSet = registry.getTurretsOfType(type);

		for (final TurretData turretData : turretDataSet)
			registry.syncTurretDataWithSettings(settings, turretData);

		Messenger.success(getPlayer(), Lang.of("Turret_Commands.Sync_Message", "{turretType}", type));
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord("arrow", "beam", "fireball");

		return NO_COMPLETE;
	}
}
