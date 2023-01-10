package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.menu.ToolsMenu;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.*;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.*;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.remain.Remain;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class TurretCommand extends SimpleSubCommand {

	TurretCommand() {
		super("turret|turrets");

		setDescription(Lang.of("Turret_Commands.Turret_Description"));
		setUsage("<give_tool|give_turret> <turret_type> <player>");
		setPermission(Permissions.Command.TOOL);
	}

	/**
	 * Perform the main command logic.
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		if (args.length == 0)
			new ToolsMenu().displayTo(getPlayer());
		else {
			final String param = args[0];

			if (args.length == 1) {
				removeOrTakeTurret(param, getPlayer(), null);
			} else if (args.length <= 2) {
				final String value = args[1];

				giveTurret(param, getPlayer(), value);
				removeOrTakeTurret(param, getPlayer(), value);

				if (args.length == 3) {
					final String playerName = args[2];
					final Player player = Bukkit.getPlayer(playerName);

					if (player == null)
						returnTell(Lang.of("Turret_Commands.Player_Non_Existent"));

					giveTurret(param, player, value);
				}
			}
		}
	}

	private void giveTurret(final String param, final Player player, final String type) {
		switch (param) {
			case "tool":
				if ("arrow_turret".equals(type))
					ArrowTurretTool.getInstance().give(player);
				else if ("beam_turret".equals(type))
					if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
						BeamTurretTool.getInstance().give(player);
					else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
				else if ("fireball_turret".equals(type))
					FireballTurretTool.getInstance().give(player);
				break;
			case "give":
				if ("arrow_turret".equals(type))
					ArrowTurret.getInstance().give(player);
				else if ("beam_turret".equals(type))
					if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
						BeamTurret.getInstance().give(player);
					else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
				else if ("fireball_turret".equals(type))
					FireballTurret.getInstance().give(player);
				break;
			case "buy":
				final PlayerCache cache = PlayerCache.from(player);
				final String typeName = type.replace("_turret", "");
				final TurretSettings settings = TurretSettings.findTurretSettings(typeName);

				if (cache.getCurrency(false) - settings.getLevels().get(0).getPrice() < 0) {
					Messenger.error(player, Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));
					break;
				}


				System.out.println(cache.getCurrency(false));
				cache.takeCurrency(settings.getLevels().get(0).getPrice(), false);

				switch (type) {
					case "arrow_turret":
						ArrowTurret.getInstance().give(player);
						break;
					case "beam_turret":
						if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
							BeamTurret.getInstance().give(player);
						else Messenger.error(player, "Beam turrets are only supported in versions 1.9 and above.");
						break;
					case "fireball_turret":
						FireballTurret.getInstance().give(player);
						break;
				}

				Messenger.success(player, Lang.of("Turret_Commands.Buy_Turret_Message", "{turretType}", typeName));
				break;
		}
	}

	private void removeOrTakeTurret(final String param, final Player player, @Nullable final String turretId) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final Block block = player.getTargetBlock(null, 5);

		if (turretId == null) {
			final TurretData turretData = registry.getTurretByBlock(block);

			if (param.equals("remove")) {
				if (registry.isRegistered(block)) {
					registry.unregister(block);
					Messenger.success(player, Lang.of("Turret_Commands.Remove_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
				} else
					Messenger.error(player, Lang.of("Turret_Commands.Error_Not_Looking_At_Turret"));
			} else if (param.equals("take")) {
				if (registry.isRegistered(block)) {
					final TurretSettings settings = TurretSettings.findTurretSettings(turretData.getType());
					final ItemStack skull = SkullCreator.itemFromBase64(settings.getBase64Texture());
					final ItemStack turret = ItemCreator.of(skull).name(Lang.of("Tool.Unplaced_Turret_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
							.lore(Lang.ofArray("Tool.Unplaced_Turret_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName()))
							.tag("id", turretData.getId()).make();

					player.getInventory().addItem(turret);
					registry.registerToUnplaced(turretData, turret);
					registry.unregister(block);
					Messenger.success(player, Lang.of("Turret_Commands.Take_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
				} else
					Messenger.error(player, Lang.of("Turret_Commands.Error_Not_Looking_At_Turret"));
			}
		} else {
			final TurretData turretData = registry.getTurretByID(turretId);

			if (param.equals("remove")) {
				if (registry.isRegistered(turretId)) {
					registry.unregisterByID(turretId);
					Messenger.success(player, Lang.of("Turret_Commands.Remove_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
				} else
					Messenger.error(player, Lang.of("Turret_Commands.Turret_ID_Does_Not_Exist", "{invalidID}", turretId));
			} else if (param.equals("take")) {
				if (registry.isRegistered(turretId)) {
					final TurretSettings settings = TurretSettings.findTurretSettings(turretData.getType());
					final ItemStack skull = SkullCreator.itemFromBase64(settings.getBase64Texture());
					final ItemStack turret = ItemCreator.of(skull).name(Lang.of("Tool.Unplaced_Turret_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
							.lore(Lang.ofArray("Tool.Unplaced_Turret_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName()))
							.tag("id", turretData.getId()).make();

					player.getInventory().addItem(turret);
					registry.registerToUnplaced(turretData, turret);
					registry.unregisterByID(turretId);
					Messenger.success(player, Lang.of("Turret_Commands.Take_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
				} else
					Messenger.error(player, Lang.of("Turret_Commands.Turret_ID_Does_Not_Exist", "{invalidID}", turretId));
			}
		}
	}

	@Override
	protected List<String> tabComplete() {
		final boolean isAddingTurret = args[0].equals("give") || args[0].equals("tool") || args[0].equals("buy");

		if (this.args.length == 1)
			return this.completeLastWord("tool", "give", "remove", "take", "buy");

		if (this.args.length == 2) {
			if (isAddingTurret)
				return this.completeLastWord("arrow_turret", "beam_turret", "fireball_turret");
			else return completeTurretIDs();
		}

		if (this.args.length == 3 && isAddingTurret)
			return completeLastWordPlayerNames();

		return NO_COMPLETE;
	}

	private List<String> completeTurretIDs() {
		final TurretRegistry registry = TurretRegistry.getInstance();

		return TabUtil.complete(this.getLastArg(), registry.getTurretIDs());
	}
}
