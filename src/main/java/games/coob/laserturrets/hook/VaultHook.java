package games.coob.laserturrets.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

	private static Economy econ = null;

	public static Economy getEconomy() {
		return econ;
	}

	public static boolean setupEconomy(final Server server) {
		if (server.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		final RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();

		return true;
	}
}

