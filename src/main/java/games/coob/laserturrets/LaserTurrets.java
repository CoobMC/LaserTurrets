package games.coob.laserturrets;

import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.VaultHook;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.settings.TurretType;
import games.coob.laserturrets.task.*;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.SkullCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.Arrays;
import java.util.function.Function;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin { // TODO use HookManager.deposit(); instead of vault hook

	/**
	 * Automatically perform login ONCE when the plugin starts.
	 */
	@Override
	protected void onPluginStart() {
		Common.runLater(TurretRegistry::getInstance);

		for (final String type : getTypes()) {
			final TurretType turretType = findEnum(TurretType.class, type, null, "No such such turret type. Available: " + Arrays.toString(getTypes()) + ".");
			TurretSettings.createTurretType(type, turretType);

			final TurretSettings settings = TurretSettings.findByName(type);

			if (settings.getToolItem() == null) {
				final ItemStack itemStack = SkullCreator.itemFromBase64(settings.getHeadTexture());
				settings.setToolItem(itemStack);
			}
		}

		if (!VaultHook.setupEconomy(getServer()) && Settings.CurrencySection.USE_VAULT) {
			Common.log("[LaserTurrets] - Disabled due to no Vault dependency found (an economy plugin is also required)!", getDescription().getName());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			TurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);
	}

	/*private void updateFolder() {
		//this.getDataFolder().getAbsolutePath();
		final Path moveSourcePath = Paths.get(this.getDataFolder().getAbsolutePath() + "/turrets");
		final Path moveTargetPath = Paths.get(this.getDataFolder().getAbsolutePath() + "/old-turrets");

		System.out.println("Path1: " + moveSourcePath);
		System.out.println("Path2: " + moveTargetPath);
		try {
			if (!Files.exists(moveTargetPath)) {
				Files.createDirectory(moveTargetPath);
			}
			Files.move(moveSourcePath, moveTargetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (final Exception e) {
			// Handle any errors that occur during the move operation
		}
	}*/

	@Override
	protected void onPluginReload() {
		TurretRegistry.getInstance().save();
		Sequence.reload();
		Hologram.deleteAll();
	}

	@Override
	protected void onPluginStop() {
		TurretRegistry.getInstance().save();
		Sequence.reload();
		Hologram.deleteAll();
	}

	public String[] getTypes() { // TODO get from database
		return new String[]{
				"arrow", "beam", "fireball"
		};
	}

	/**
	 * Automatically perform login when the plugin starts and each time it is reloaded.
	 */
	@Override
	protected void onReloadablesStart() {
		//
		// Add your own plugin parts to load automatically here
		// Please see @AutoRegister for parts you do not have to register manually
		//

		Common.runTimer(20, new ArrowTask());
		Common.runTimer(25, new FireballTask());
		Common.runTimer(2, new LaserPointerTask());

		Common.runLater(() -> {
			for (final TurretData turretData : TurretRegistry.getInstance().getRegisteredTurrets()) {
				final String type = turretData.getType();
				final TurretSettings settings = TurretSettings.findByName(type);
				final Block skullBlock = turretData.getLocation().getBlock().getRelative(BlockFace.UP);

				if (CompMaterial.isSkull(skullBlock.getType())) {
					final Skull state = (Skull) skullBlock.getState();
					SkullCreator.mutateBlockState(state, settings.getHeadTexture());
					state.update(false, false);
				}

				if (Settings.TurretSection.DISPLAY_HOLOGRAM)
					TurretRegistry.getInstance().updateHologram(turretData);
			}

			TurretRegistry.getInstance().save();
		});

		if (Settings.TurretSection.DISPLAY_HOLOGRAM)
			Common.runTimer(20, new HologramTask());

		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
			Common.runTimer(30, new BeamTask());
	}

	private <T extends Enum<T>> T findEnum(final Class<T> enumType, final String name, final Function<T, Boolean> condition, final String falseMessage) throws CommandException {
		T found = null;

		try {
			found = ReflectionUtil.lookupEnum(enumType, name);

			if (!condition.apply(found))
				found = null;

		} catch (final Throwable t) {
			// Not found, pass through below to error out
		}

		Valid.checkNotNull(found, falseMessage.replace("{enum}", name).replace("{available}", Common.join(enumType.getEnumConstants())));
		return found;
	}

	/* ------------------------------------------------------------------------------- */
	/* Static */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Return the instance of this plugin, which simply refers to a static
	 * field already created for you in SimplePlugin but casts it to your
	 * specific plugin instance for your convenience.
	 *
	 * @return
	 */
	public static LaserTurrets getInstance() {
		return (LaserTurrets) SimplePlugin.getInstance();
	}
}
