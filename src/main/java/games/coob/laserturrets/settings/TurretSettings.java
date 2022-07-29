package games.coob.laserturrets.settings;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.StringUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.settings.YamlConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TurretSettings extends YamlConfig {

	@Getter
	private List<LevelData> levels;

	private List<String> playerBlacklist;

	@Getter
	private List<EntityType> mobBlacklist;

	public static TurretSettings getInstance(final String turretType) {
		return new TurretSettings(turretType);
	}

	public TurretSettings(final String turretType) {
		setPathPrefix(StringUtil.capitalize(turretType) + "_Turret_Default_Settings");
		this.loadConfiguration("turrets/" + turretType + "-turrets.yml");
	}

	@Override
	protected void onLoad() {
		this.playerBlacklist = this.getList("Player_Blacklist", String.class);
		this.mobBlacklist = this.getList("Mob_Blacklist", EntityType.class);
		this.levels = this.loadLevels();
	}

	private List<LevelData> loadLevels() {
		final List<LevelData> levels = new ArrayList<>();

		for (final Map.Entry<Integer, Object> entry : getMap("Levels", Integer.class, Object.class).entrySet()) {
			final int level = entry.getKey();
			final SerializedMap levelSettings = SerializedMap.of(entry.getValue());

			levels.add(LevelData.deserialize(levelSettings, level));
		}

		return levels;
	}

	public List<UUID> getPlayerBlacklist() {
		final List<UUID> playerList = new ArrayList<>();

		for (final String playerName : this.playerBlacklist) {
			final Player player = Bukkit.getPlayer(playerName);

			if (player != null)
				playerList.add(player.getUniqueId());
		}

		return playerList;
	}

	@Getter
	@ToString
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class LevelData implements ConfigSerializable {

		private final int level;

		private double price;

		private boolean enableLasers;

		private double laserDamage;

		private int range;

		private int health;

		private List<Tuple<ItemStack, Double>> lootChances;

		public void setLevelData(final TurretData.TurretLevel turretLevel) {
			turretLevel.setRange(this.range);
			turretLevel.setPrice(this.price);
			turretLevel.setLaserEnabled(this.enableLasers);
			turretLevel.setLaserDamage(this.laserDamage);
			turretLevel.setHealth(this.health);
			turretLevel.setLootChances(this.lootChances);
		}

		@Override
		public SerializedMap serialize() {
			return null;
		}

		public static LevelData deserialize(final SerializedMap map, final int level) {
			final LevelData levelData = new LevelData(level);

			map.setRemoveOnGet(true);

			levelData.range = map.getInteger("Range");
			Valid.checkNotNull(levelData.range, "Missing 'Range' key from level: " + map);

			levelData.health = map.getInteger("Health");
			Valid.checkNotNull(levelData.health, "Missing 'Health' key from level: " + map);

			levelData.price = map.getDouble("Price");
			Valid.checkNotNull(levelData.price, "Missing 'Price' key from level: " + map);

			levelData.enableLasers = map.getBoolean("Enable_Laser_Pointers");
			Valid.checkNotNull(levelData.enableLasers, "Missing 'Enable_Laser_Pointers' key from level: " + map);

			levelData.laserDamage = map.getDouble("Laser_Pointer_Damage");
			Valid.checkNotNull(levelData.price, "Missing 'Laser_Pointer_Damage' key from level: " + map);

			levelData.lootChances = map.getTupleList("Loot_Drops", ItemStack.class, Double.class);
			Valid.checkNotNull(levelData.lootChances, "Missing 'Loot_Drops' key from level: " + map);

			Valid.checkBoolean(map.isEmpty(), "Found unrecognized level settings: " + map);

			return levelData;
		}
	}
}
