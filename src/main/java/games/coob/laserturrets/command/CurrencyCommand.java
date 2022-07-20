package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

public class CurrencyCommand extends SimpleSubCommand {
	protected CurrencyCommand() {
		super("currency");

		setMinArguments(1);
	}

	@Override
	protected void onCommand() {
		checkConsole();

		final String param = args[0];
		final String name = args[1];

		final Player player = Bukkit.getPlayer(name);

		if (player == null)
			return;

		final PlayerCache cache = PlayerCache.from(player);

		if ("get".equals(param))
			cache.getCurrency(true);

		else if (args.length == 3) {
			final int amount = Integer.parseInt(args[2]);

			if ("set".equals(param))
				cache.setCurrency(amount, true);
			else if ("give".equals(param))
				cache.giveCurrency(amount, true);
			else if ("take".equals(param))
				cache.takeCurrency(amount, true);
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
