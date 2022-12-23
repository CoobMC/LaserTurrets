package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.TurretsMenu;
import games.coob.laserturrets.model.Permissions;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.settings.Lang;

import java.util.List;

public final class MenuCommand extends SimpleSubCommand {

	public MenuCommand() {
		super("menu");

		setPermission(Permissions.Command.MENU);
		setDescription(Lang.of("Turret_Commands.Menu_Description"));
	}

	// See SpawnEntityCommand for help and comments.
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		if (args.length == 0) {
			TurretsMenu.openAllTurretsSelectionMenu(player);
		} else {
			final String param = args[0].toLowerCase();

			switch (param) {
				case "arrow":
					TurretsMenu.openArrowTurretsSelectionMenu(player);
					break;
				case "fireball":
					TurretsMenu.openFireballTurretsSelectionMenu(player);
					break;
				case "beam":
					TurretsMenu.openBeamTurretsSelectionMenu(player);
					break;
			}
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeLastWord("arrow", "fireball", "beam");

		return NO_COMPLETE;
	}
}
