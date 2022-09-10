package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.SettingsMenu;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

public class SettingsCommand extends SimpleSubCommand {
	protected SettingsCommand() {
		super("settings");

		setPermission("laserturrets.settings");
		setPermission("laserturrets.admin");
		setDescription("Edit your default turret settings in a settings menu.");
	}

	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		new SettingsMenu(null, player).displayTo(player);
	}
}
