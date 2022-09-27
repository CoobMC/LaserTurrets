package games.coob.laserturrets.database;

import games.coob.laserturrets.PlayerCache;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.database.SimpleFlatDatabase;

/**
 * Handles MySQL database for player data, votes and pending votes.
 */
public final class TurretsDatabase extends SimpleFlatDatabase<PlayerCache> {

	@Getter
	private final static TurretsDatabase instance = new TurretsDatabase();

	private TurretsDatabase() {
		this.addVariable("table", "LaserTurrets");
	}

	@Override
	protected void onLoad(final SerializedMap map, final PlayerCache cache) {
		final Double currency = map.getDouble("Currency");

		if (currency != null)
			cache.setCurrency(currency, false);
	}

	@Override
	protected SerializedMap onSave(final PlayerCache cache) {
		final SerializedMap map = new SerializedMap();

		if (cache.getCurrency() != 0)
			map.put("Currency", cache.getCurrency());

		return map;
	}

	/**
	 * Saves data about the player to the database
	 */
	public static void save(final Player player) {
		final PlayerCache cache = PlayerCache.from(player);

		instance.save(player.getName(), player.getUniqueId(), cache);
	}

	/**
	 * Loads data about the player to the database
	 */
	public static void load(final Player player) {
		final PlayerCache cache = PlayerCache.from(player);

		instance.load(player.getUniqueId(), cache);
	}

	@Override
	protected int getExpirationDays() {
		return 90; // 90 is the default value
	}
}