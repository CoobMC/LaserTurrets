package games.coob.laserturrets;

import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.HookSystem;
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
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin {

	/**
	 * Automatically perform login ONCE when the plugin starts.
	 */
	@Override
	protected void onPluginStart() {
		if (folderContainsOldTurretFiles("turrets")) {
			moveTurretsFolder();
			createReadmeFile();
		}

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
			Common.log("Disabled due to no Vault dependency found (an economy plugin is also required)!", getDescription().getName());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			TurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);

		new UpdateChecker(this, 105494).getVersion(version -> {
			if (!this.getDescription().getVersion().equals(version))
				Common.log("There is a new update available (v" + version + ").");
		});
	}

	private void moveTurretsFolder() {
		final File turretsFolder = new File(this.getDataFolder(), "turrets");
		final File oldTurretsFolder = new File(this.getDataFolder(), "old-turrets");

		if (oldTurretsFolder.exists())
			return;

		if (!oldTurretsFolder.mkdir()) {
			Common.log("Error: failed to create old-turrets folder");
			return;
		}

		for (final File file : turretsFolder.listFiles()) {
			try {
				Files.move(file.toPath(), new File(oldTurretsFolder, file.getName()).toPath());
			} catch (final IOException e) {
				Common.log("Error moving file " + file.getName() + ": " + e.getMessage());
			}
		}

		Common.log("All files from turrets folder moved to old-turrets folder");
	}

	public boolean folderContainsOldTurretFiles(final String folderName) {
		final File pluginDataFolder = new File(this.getDataFolder(), folderName);

		if (!pluginDataFolder.isDirectory()) {
			return false;
		}

		for (final File file : pluginDataFolder.listFiles()) {
			if (file.getName().contains("turret")) {
				return true;
			}
		}

		return false;
	}


	public void createReadmeFile() {
		final File oldTurretsFolder = new File(this.getDataFolder(), "old-turrets");

		if (!oldTurretsFolder.exists() || !oldTurretsFolder.isDirectory()) {
			Common.log("Error: old-turrets folder not found or is not a directory");
			return;
		}

		final File readmeFile = new File(oldTurretsFolder, "README.txt");

		try (FileWriter writer = new FileWriter(readmeFile)) {
			writer.write("Update to v2.0.0 information\n\n");
			writer.write("This folder contains all the files that were previously in the 'turrets' folder. ");
			writer.write("These files have been moved here because I've made some new changes and recoded some parts of the plugin.\n\n");
			writer.write("If you would like to maintain your previous settings, you can copy paste the specific sections from the 'old-turrets' to the files in the 'turrets' folder. ");
			writer.write("Feel free to delete this folder if you no longer need these files.\n\n");
			writer.write("You may use a YAML validator to check if your files are correctly formatted and prevent the plugin from breaking. ");
			writer.write("If you encounter any issues, you can contact me on our discord server (https://discord.gg/2rgvQbHsSW).\n\n");
			writer.write("NOTE: This update does not impact already created turrets, only the settings have been modified.");
		} catch (final IOException e) {
			Common.log("Error creating README file: " + e.getMessage());
			return;
		}

		Common.log("README file created in the old-turrets folder");
	}

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
		// Load our dependency system
		try {
			HookSystem.loadDependencies();
		} catch (final Throwable throwable) {
			Common.throwError(throwable, "Error while loading " + this.getDataFolder().getName() + " dependencies!");
		}

		Common.runTimer(20, new ArrowTask());
		Common.runTimer(25, new FireballTask());
		Common.runTimer(30, new BeamTask());
		Common.runTimer(2, new LaserPointerTask());

		Common.runLater(() -> {
			for (final TurretData turretData : TurretRegistry.getInstance().getRegisteredTurrets()) {
				TurretUtil.updateHologramAndTexture(turretData);
			}
		});

		if (Settings.TurretSection.DISPLAY_HOLOGRAM)
			Common.runTimer(20, new HologramTask());
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
