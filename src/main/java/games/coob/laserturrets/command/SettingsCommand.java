package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.SettingsMenu;
import games.coob.laserturrets.model.Permissions;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.settings.Lang;

public class SettingsCommand extends SimpleSubCommand {
	protected SettingsCommand() {
		super("settings");

		setPermission(Permissions.Command.SETTINGS);
		setDescription(Lang.of("Turret_Commands.Settings_Description"));
	}

	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		new SettingsMenu(null, player).displayTo(player);
	}
}
