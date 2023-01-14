package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class BuyCommand extends SimpleSubCommand {

	BuyCommand() {
		super("buy");

		setDescription(Lang.of("Turret_Commands.Buy_Description"));
		setUsage("<buy> <turret_type>");
		setPermission(Permissions.Command.BUY);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final String type = args[0];
		final PlayerCache cache = PlayerCache.from(getPlayer());
		final String typeName = type.replace("_turret", "");
		final TurretSettings settings = TurretSettings.findTurretSettings(typeName);
		final double price = settings.getLevels().get(0).getPrice();

		if (cache.getCurrency(false) - price < 0) {
			Messenger.error(getPlayer(), Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));
			return;
		}

		cache.takeCurrency(price, false);
		giveTurret(type, getPlayer());

		Messenger.success(getPlayer(), Lang.of("Turret_Commands.Buy_Turret_Message", "{turretType}", typeName, "{price}", price, "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));

	}

	private void giveTurret(final String type, final Player player) {
		if ("arrow_turret".equals(type))
			ArrowTurret.getInstance().give(player);
		else if ("beam_turret".equals(type))
			if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
				BeamTurret.getInstance().give(player);
			else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
		else if ("fireball_turret".equals(type))
			FireballTurret.getInstance().give(player);
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord("arrow_turret", "beam_turret", "fireball_turret");

		return NO_COMPLETE;
	}
}
