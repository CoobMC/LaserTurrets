package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.TurretSelectionMenu;
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
		// final String param = args[0].toLowerCase();

		new TurretSelectionMenu(player).displayTo(player);
	}

	@Override
	protected List<String> tabComplete() {

		/*switch (this.args.length) {
			case 1:
				return completeLastWord("merch", "book", "rules", "sign", "workbench", "inventory", "fo", "file");

			case 2:
				return "file".equalsIgnoreCase(args[0]) ? completeLastWord(MenuData.getMenuNames()) : NO_COMPLETE;
		}*/

		return NO_COMPLETE;
	}
}
