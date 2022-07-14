package games.coob.laserturrets.command;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class BlacklistCommand extends SimpleSubCommand {

	BlacklistCommand() {
		super("blacklist");

		setDescription("Add a player to the blacklist so he doesn't get targeted by the turret!");
		setPermission("laserturrets.blacklist");
		setMinArguments(1);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();
		final String param = args[0];
		final String playerName = args[1];
		final TurretRegistry registry = TurretRegistry.getInstance();
		final Block block = Remain.getTargetBlock(player, 3);

		if (registry.isRegistered(block)) {
			if ("add".equals(param)) {
				registry.addPlayerToBlacklist(block, playerName);
				tell("&aAdded &3" + playerName + " &ato the turrets player blacklist.");
			} else if ("remove".equals(param)) {
				registry.removePlayerFromBlacklist(block, playerName);
				tell("&cRemoved &e" + playerName + " &cfrom the turrets player blacklist.");
			}
		} else tell("&cYou must look at a turret to perform this command.");
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1)
			return this.completeLastWord("add", "remove");

		else if (args.length == 2)
			return this.completeLastWordPlayerNames();

		return NO_COMPLETE;
	}
}
