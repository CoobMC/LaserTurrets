package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.settings.Lang;

import java.util.List;

public class BalanceCommand extends SimpleSubCommand {
	protected BalanceCommand() {
		super("currency");

		setMinArguments(1);
		setPermission(Permissions.Command.CURRENCY);
		setDescription(Lang.of("Turret_Commands.Balance_Description"));
		setUsage("<get, set, give, take> <player> <amount>");
	}

	@Override
	protected void onCommand() {
		final String param = args[0];
		final String name = args[1];

		final Player player = Bukkit.getPlayer(name);

		if (player == null)
			returnTell(Lang.of("Turret_Commands.Player_Non_Existent", "{invalidPlayer}", name));

		final PlayerCache cache = PlayerCache.from(player);

		if ("get".equals(param))
			cache.getCurrency(true);

		if (args.length < 3 && !param.equals("get"))
			returnUsage();

		else if (args.length == 3) {
			final String number = args[2];

			try {
				final double amount = Double.parseDouble(number);

				if ("set".equals(param))
					cache.setCurrency(amount, true);
				else if ("give".equals(param))
					cache.giveCurrency(amount, true);
				else if ("take".equals(param))
					cache.takeCurrency(amount, true);
			} catch (final NumberFormatException e) {
				returnTell(Lang.of("Turret_Commands.Invalid_Number"));
			}
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeLastWord("get", "set", "give", "take");

		if (this.args.length == 2)
			return completeLastWordPlayerNames();

		if (this.args.length == 3)
			return completeLastWord("<amount>");

		return NO_COMPLETE;
	}
}
