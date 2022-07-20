package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.TurretShopMenu;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

public final class ShopCommand extends SimpleSubCommand {

	public ShopCommand() {
		super("shop");
	}

	// See SpawnEntityCommand for help and comments.
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		new TurretShopMenu(player).displayTo(player);
	}
}
