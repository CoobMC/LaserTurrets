package games.coob.laserturrets;

import games.coob.laserturrets.menu.ShopMenu;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.task.ArrowTask;
import games.coob.laserturrets.task.LaserPointerTask;
import games.coob.laserturrets.task.LaserTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.plugin.SimplePlugin;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin { // TODO create an animation when registering a turret (spiny head animation)

	// TODO allow players to buy already placed turrets

	private static Economy econ = null;

	public static Economy getEconomy() {
		return econ;
	}

	public String[] getTypes() {
		return new String[]{
				"arrow", "laser", "flame"
		};
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
	}

	@Override
	protected void onPluginReload() {
		TurretRegistry.getInstance().save();
	}

	@Override
	protected void onPluginStop() {
		TurretRegistry.getInstance().save();
	}

	/**
	 * Automatically perform login when the plugin starts and each time it is reloaded.
	 */
	@Override
	protected void onReloadablesStart() {

		// You can check for necessary plugins and disable loading if they are missing
		// Valid.checkBoolean(HookManager.isVaultLoaded(), "You need to install Vault so that we can work with packets, offline player data, prefixes and groups.");

		// Uncomment to load variables
		// Variable.loadVariables();

		//
		// Add your own plugin parts to load automatically here
		// Please see @AutoRegister for parts you do not have to register manually
		//
		Common.runTimer(20, new ArrowTask());
		Common.runTimer(2, new LaserPointerTask());
		Common.runTimer(30, new LaserTask());
	}

	/* ------------------------------------------------------------------------------- */
	/* Events */
	/* ------------------------------------------------------------------------------- */

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {
		final Block block = event.getBlock();
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		final Block block = event.getBlock();
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteractAtBlock(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();
		final TurretRegistry registry = TurretRegistry.getInstance();

		if (registry.isRegistered(block)) {
			final TurretData turretData = registry.getTurretByBlock(block);
			final Player player = event.getPlayer();

			if (turretData.getPlayerBlacklist() != null && turretData.getPlayerBlacklist().contains(player))
				new ShopMenu.UpgradeMenu(turretData, turretData.getCurrentLevel(), player).displayTo(player);
		}
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
