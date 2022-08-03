package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.TurretsMenu;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

public final class MenuCommand extends SimpleSubCommand {

	public MenuCommand() {
		super("menu");
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
				case "flame":
					TurretsMenu.openFlameTurretsSelectionMenu(player);
					break;
				case "laser":
					TurretsMenu.openLaserTurretsSelectionMenu(player);
					break;
			}
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeLastWord("arrow", "flame", "laser");

		return NO_COMPLETE;
	}
}
