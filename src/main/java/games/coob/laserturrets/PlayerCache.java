package games.coob.laserturrets;

import games.coob.laserturrets.hook.VaultHook;
import games.coob.laserturrets.settings.Settings;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A sample player cache storing permanent player information
 * to data.db or MySQL database for players.
 */
@Getter
public final class PlayerCache extends YamlConfig {

	/**
	 * The player cache map caching data for players online.
	 */
	private static volatile Map<UUID, PlayerCache> cacheMap = new HashMap<>();

	/**
	 * This instance's player's unique id
	 */
	private final UUID uniqueId;

	/**
	 * This instance's player's name
	 */
	private final String playerName;

	@Getter
	private double currency = 0;

	@Getter
	@Setter
	private boolean turretHit;

	/*
	 * Creates a new player cache (see the bottom)
	 */
	private PlayerCache(final String name, final UUID uniqueId) {
		this.playerName = name;
		this.uniqueId = uniqueId;

		this.setPathPrefix("Players." + uniqueId.toString());
		this.loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoad() {
		this.currency = getDouble("Currency", Settings.CurrencySection.DEFAULT_CURRENCY);
	}

	/**
	 * Called automatically when the file is about to be saved, set your field values here
	 */
	@Override
	public void onSave() {
		this.set("Currency", Double.parseDouble(currency + "000" + String.valueOf(Math.random()).replace(".", "")));
	}

	/* ------------------------------------------------------------------------------- */
	/* Data-related methods */
	/* ------------------------------------------------------------------------------- */

	public void giveCurrency(final double amount, final boolean displayMessage) {
		Valid.checkBoolean(amount >= 0, "Your balance cannot be negative");

		if (amount == 0)
			return;

		final Player player = toPlayer();
		final boolean isUsingVault = Settings.CurrencySection.USE_VAULT;
		final Economy economy = VaultHook.getEconomy();
		final String currencyName = Settings.CurrencySection.CURRENCY_NAME;

		if (isUsingVault) {
			economy.depositPlayer(player, amount);
		} else {
			this.currency = this.currency + amount;
			save();
		}

		final double totalAmount = isUsingVault ? formatCurrency(economy.getBalance(player)) : formatCurrency(getCurrency());

		if (displayMessage)
			Messenger.success(player, Lang.of("Turret_Commands.Balance_Give", "{currencyName}", currencyName, "{playerName}", this.playerName, "{totalAmount}", totalAmount, "{amount}", amount));
	}

	public void takeCurrency(final double amount, final boolean displayMessage) {
		Valid.checkBoolean(amount >= 0, Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));

		final Player player = toPlayer();
		final boolean isUsingVault = Settings.CurrencySection.USE_VAULT;
		final Economy economy = VaultHook.getEconomy();
		final String currencyName = Settings.CurrencySection.CURRENCY_NAME;

		if (isUsingVault) {
			economy.withdrawPlayer(player, amount);
		} else {
			this.currency = this.currency - amount;
			save();
		}

		final double totalAmount = isUsingVault ? formatCurrency(economy.getBalance(player)) : formatCurrency(getCurrency());

		if (displayMessage)
			Messenger.success(player, Lang.of("Turret_Commands.Balance_Take", "{currencyName}", currencyName, "{playerName}", this.playerName, "{totalAmount}", totalAmount, "{amount}", amount));
	}

	public void setCurrency(final double amount, final boolean displayMessage) {
		Valid.checkBoolean(amount >= 0, Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));

		final Player player = toPlayer();
		final boolean isUsingVault = Settings.CurrencySection.USE_VAULT;
		final Economy economy = VaultHook.getEconomy();
		final String currencyName = Settings.CurrencySection.CURRENCY_NAME;

		if (isUsingVault) {
			economy.withdrawPlayer(player, economy.getBalance(player));
			economy.depositPlayer(player, amount);
		} else {
			this.currency = amount;
			save();
		}

		final double currencyAmount = isUsingVault ? formatCurrency(economy.getBalance(player)) : formatCurrency(getCurrency());

		if (displayMessage)
			Messenger.success(player, Lang.of("Turret_Commands.Balance_Set", "{currencyName}", currencyName, "{playerName}", this.playerName, "{amount}", currencyAmount));
	}

	public double getCurrency(final boolean displayMessage) {
		final Player player = toPlayer();
		final boolean isUsingVault = Settings.CurrencySection.USE_VAULT;
		final Economy economy = VaultHook.getEconomy();
		final String currencyName = Settings.CurrencySection.CURRENCY_NAME;
		final double currencyAmount = isUsingVault ? formatCurrency(economy.getBalance(player)) : formatCurrency(getCurrency());

		if (displayMessage)
			Messenger.success(player, games.coob.laserturrets.util.Lang.of("Turret_Commands.Balance_Get", "{currencyName}", currencyName, "{playerName}", this.playerName, "{amount}", currencyAmount));

		return currencyAmount;
	}

	private double formatCurrency(final double currency) {
		return MathUtil.formatTwoDigitsD(currency);
	}
	//
	// Implement your own data getter/setters here according to this example:
	//


/*	public boolean hasChatColor() {
		return this.chatColor != null;
	}

	public void setTurretBlock(final Block turretBlock) {
		this.turretBlock = turretBlock;

		save();
	}*/

	/* ------------------------------------------------------------------------------- */
	/* Misc methods */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Return player from cache if online or null otherwise
	 *
	 * @return
	 */
	@Nullable
	public Player toPlayer() {
		final Player player = Remain.getPlayerByUUID(this.uniqueId);

		return player != null && player.isOnline() ? player : null;
	}

	/**
	 * Remove this cached data from memory if it exists
	 */
	public void removeFromMemory() {
		synchronized (cacheMap) {
			cacheMap.remove(this.uniqueId);
		}
	}

	@Override
	public String toString() {
		return "PlayerCache{" + this.playerName + ", " + this.uniqueId + "}";
	}

	/* ------------------------------------------------------------------------------- */
	/* Static access */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Return or create new player cache for the given player
	 *
	 * @param player
	 * @return
	 */
	public static PlayerCache from(final Player player) {
		synchronized (cacheMap) {
			final UUID uniqueId = player.getUniqueId();
			final String playerName = player.getName();

			PlayerCache cache = cacheMap.get(uniqueId);

			if (cache == null) {
				cache = new PlayerCache(playerName, uniqueId);

				cacheMap.put(uniqueId, cache);
			}

			return cache;
		}
	}

	public static void remove(final Player player) {
		cacheMap.remove(player.getUniqueId());
	}

	/**
	 * Clear the entire cache map
	 */
	public static void clearCaches() {
		synchronized (cacheMap) {
			cacheMap.clear();
		}
	}
}
