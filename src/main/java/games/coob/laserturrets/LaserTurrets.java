package games.coob.laserturrets;

import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.VaultHook;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.task.*;
import games.coob.laserturrets.util.Hologram;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.plugin.SimplePlugin;

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
		Common.runLater(TurretRegistry::getInstance);

		for (final String type : getTypes()) {
			TurretSettings.createSettings(type);
		}

		if (!VaultHook.setupEconomy(getServer()) && Settings.CurrencySection.USE_VAULT) {
			Common.log("[LaserTurrets] - Disabled due to no Vault dependency found!", getDescription().getName());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			TurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);
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

	public String[] getTypes() {
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

		if (Settings.TurretSection.DISPLAY_HOLOGRAM)
			Common.runTimer(20, new HologramTask());

		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_9))
			Common.runTimer(30, new BeamTask());
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
