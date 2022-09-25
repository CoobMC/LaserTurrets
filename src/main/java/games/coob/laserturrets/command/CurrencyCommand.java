package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

public class CurrencyCommand extends SimpleSubCommand {
	protected CurrencyCommand() {
		super("currency");

		setMinArguments(1);
		setPermission(Permissions.Command.CURRENCY);
		setDescription("Get, set,  give or take " + Settings.CurrencySection.CURRENCY_NAME + " from players.");
		setUsage("<get, set, give, take> <player> <amount>");
	}

	@Override
	protected void onCommand() {
		checkConsole();

		if (args.length < 3)
			returnTell("Wrong usage of command (/lt currency get/set/give/take <player> <amount>).");

		final String param = args[0];
		final String name = args[1];

		final Player player = Bukkit.getPlayer(name);

		if (player == null)
			returnTell("The player '" + name + "' does not exist.");

		final PlayerCache cache = PlayerCache.from(player);

		if ("get".equals(param))
			cache.getCurrency(true);

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
				returnTell("'" + number + "' is not a valid number.");
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
