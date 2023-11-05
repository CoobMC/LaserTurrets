package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.PlayerAlliesMenu;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.util.Lang;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

public final class AlliesCommand extends SimpleSubCommand {

	public AlliesCommand() {
		super("allies");

		setPermission(Permissions.Command.ALLIES);
		setDescription(Lang.of("Turret_Commands.Allies_Description"));
	}

	// See SpawnEntityCommand for help and comments.
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		new PlayerAlliesMenu(player).displayTo(player);
	}
}
