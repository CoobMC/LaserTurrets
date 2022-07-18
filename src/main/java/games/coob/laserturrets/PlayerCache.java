package games.coob.laserturrets;

import games.coob.laserturrets.model.TurretData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.remain.Remain;
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

	//
	// Store any custom saveable data here
	//
	@Getter
	@Setter
	private Block turretBlock;

	@Getter
	@Setter
	private String turretType;


	@Getter
	@Setter
	private TurretData selectedTurret;

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
		//
		// Load any custom fields here, example:
		// this.chatColor = get("Chat_Color", CompChatColor.class);
		//
	}

	/**
	 * Called automatically when the file is about to be saved, set your field values here
	 */
	@Override
	public void onSave() {
		//
		// Save any custom fields here, example:
		// this.set("Chat_Color", this.chatColor);
		//
	}

	/* ------------------------------------------------------------------------------- */
	/* Data-related methods */
	/* ------------------------------------------------------------------------------- */

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

	/**
	 * Clear the entire cache map
	 */
	public static void clearCaches() {
		synchronized (cacheMap) {
			cacheMap.clear();
		}
	}
}
