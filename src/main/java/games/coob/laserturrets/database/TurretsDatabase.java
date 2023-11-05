package games.coob.laserturrets.database;

import games.coob.laserturrets.PlayerCache;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.database.SimpleFlatDatabase;

import java.util.Set;
import java.util.UUID;

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
		final Double currency = map.getDouble("Balance");
		final Set<UUID> playerAllies = map.getSet("Player_Allies", UUID.class);
		final Set<EntityType> mobAllies = map.getSet("Mob_Allies", EntityType.class);
		final boolean playerWhitelistEnabled = map.getBoolean("Use_Player_Whitelist", false);
		final boolean mobWhitelistEnabled = map.getBoolean("Use_Mob_Whitelist", false); // Add default value to load it if the key doesn't exist

		if (currency != null)
			cache.setCurrency(currency, false);

		if (!playerAllies.isEmpty())
			cache.setPlayerAllies(playerAllies);

		if (!mobAllies.isEmpty())
			cache.setMobAllies(mobAllies);

		cache.setPlayerWhitelistEnabled(playerWhitelistEnabled);
		cache.setMobWhitelistEnabled(mobWhitelistEnabled);
	}

	@Override
	protected SerializedMap onSave(final PlayerCache cache) {
		final SerializedMap map = new SerializedMap();

		if (cache.getCurrency() != 0)
			map.put("Balance", cache.getCurrency());

		if (!cache.getPlayerAllies().isEmpty())
			map.put("Player_Allies", cache.getPlayerAllies());

		if (!cache.getMobAllies().isEmpty())
			map.put("Mob_Allies", cache.getMobAllies());

		map.put("Use_Player_Whitelist", cache.isPlayerWhitelistEnabled());
		map.put("Use_Mob_Whitelist", cache.isMobWhitelistEnabled());

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