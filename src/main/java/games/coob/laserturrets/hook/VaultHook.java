package games.coob.laserturrets.hook;

import net.milkbowl.vault.economy.Economy;

public class VaultHook {

	private static Economy econ = null;

	public static Economy getEconomy() {
		return econ;
	}
}

