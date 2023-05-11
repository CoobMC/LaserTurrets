package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.UnplacedData;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.SkullCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.TabUtil;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.Remain;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class TakeCommand extends SimpleSubCommand {

	TakeCommand() {
		super("take");

		setDescription(Lang.of("Turret_Commands.Take_Description"));
		setUsage("<id>");
		setPermission(Permissions.Command.TAKE);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();
		TurretData turretData = null;
		String turretId = null;
		Block block = null;

		if (this.args.length == 1) {
			turretId = args[0];
			turretData = TurretData.findById(turretId);
		} else if (this.args.length == 0) {
			block = player.getTargetBlock(null, 5);
			turretData = TurretData.findByBlock(block);
		}

		if (turretId == null) {
			if (TurretData.isRegistered(block)) {
				final TurretSettings settings = TurretSettings.findByName(turretData.getType());
				final ItemStack skull = SkullCreator.itemFromBase64(settings.getHeadTexture());
				final ItemStack turret = ItemCreator.of(skull).name(Lang.of("Tool.Unplaced_Turret_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
						.lore(Lang.ofArray("Tool.Unplaced_Turret_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName()))
						.tag("id", turretData.getId()).make();

				player.getInventory().addItem(turret);

				final UnplacedData unplacedData = UnplacedData.createTurret(turretData.getId(), turretData.getType());
				unplacedData.registerFromTurret(turretData, turret);
				turretData.unregister();

				Messenger.success(player, Lang.of("Turret_Commands.Take_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
			} else
				Messenger.error(player, Lang.of("Turret_Commands.Error_Not_Looking_At_Turret"));
		} else {
			if (TurretData.isRegistered(turretId)) {
				final TurretSettings settings = TurretSettings.findByName(turretData.getType());
				final ItemStack skull = SkullCreator.itemFromBase64(settings.getHeadTexture());
				final ItemStack turret = ItemCreator.of(skull).name(Lang.of("Tool.Unplaced_Turret_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
						.lore(Lang.ofArray("Tool.Unplaced_Turret_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName()))
						.tag("id", turretData.getId()).make();

				player.getInventory().addItem(turret);

				final UnplacedData unplacedData = UnplacedData.createTurret(turretData.getId(), turretData.getType());
				unplacedData.registerFromTurret(turretData, turret);
				turretData.unregister();

				Messenger.success(player, Lang.of("Turret_Commands.Take_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
			} else
				Messenger.error(player, Lang.of("Turret_Commands.Turret_ID_Does_Not_Exist", "{invalidID}", turretId));
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeTurretIDs();

		return NO_COMPLETE;
	}

	private List<String> completeTurretIDs() {
		return TabUtil.complete(this.getLastArg(), TurretData.getTurretIDs());
	}
}
