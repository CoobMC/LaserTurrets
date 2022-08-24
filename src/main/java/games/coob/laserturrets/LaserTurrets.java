package games.coob.laserturrets;

import games.coob.laserturrets.database.LaserTurretsDatabase;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.task.ArrowTask;
import games.coob.laserturrets.task.FireballTask;
import games.coob.laserturrets.task.LaserPointerTask;
import games.coob.laserturrets.task.LaserTask;
import net.milkbowl.vault.economy.Economy;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.debug.LagCatcher;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.plugin.SimplePlugin;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin {

	private static Economy econ = null;

	public static Economy getEconomy() {
		return econ;
	}

	/**
	 * Automatically perform login ONCE when the plugin starts.
	 */
	@Override
	protected void onPluginStart() {
		Common.runLater(TurretRegistry::getInstance);

		if (!HookManager.isVaultLoaded() && Settings.CurrencySection.USE_VAULT) {
			Common.log("[LaserTurrets] - Disabled due to no Vault dependency found!", getDescription().getName());
			getServer().getPluginManager().disablePlugin(this);
		}

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			LaserTurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);
	}

	@Override
	protected void onPluginReload() {
		TurretRegistry.getInstance().save();
		Sequence.reload();
	}

	@Override
	protected void onPluginStop() {
		TurretRegistry.getInstance().save();
		Sequence.reload();
	}

	public String[] getTypes() {
		return new String[]{
				"arrow", "laser", "flame"
		};
	}

	/**
	 * Automatically perform login when the plugin starts and each time it is reloaded.
	 */
	@Override
	protected void onReloadablesStart() {
		LagCatcher.start("onStart");
		for (final String type : getTypes()) {
			// if (!TurretSettings.isTurretSettingLoaded(type))
			TurretSettings.createSettings(type);
		}
		LagCatcher.end("onStart", true);

		//
		// Add your own plugin parts to load automatically here
		// Please see @AutoRegister for parts you do not have to register manually
		//
		Common.runTimer(20, new ArrowTask());
		Common.runTimer(30, new LaserTask());
		Common.runTimer(10, new FireballTask());
		Common.runTimer(2, new LaserPointerTask());
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
