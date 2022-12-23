package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.ShopMenu;
import games.coob.laserturrets.model.Permissions;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.settings.Lang;

public final class ShopCommand extends SimpleSubCommand {

	public ShopCommand() {
		super("shop");

		setPermission(Permissions.Command.SHOP);
		setDescription(Lang.of("Turret_Commands.Shop_Description"));
	}

	// See SpawnEntityCommand for help and comments.
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		new ShopMenu(player).displayTo(player);
	}
}
