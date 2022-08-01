package games.coob.laserturrets.settings;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.StringUtil;
import lombok.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import java.util.*;

@Getter
public class TurretSettings extends YamlConfig {

	private static final ConfigItems<TurretSettings> loadedTurretSettings = ConfigItems.fromFolder("turrets", TurretSettings.class);

	/*public static TurretSettings getInstance(final String turretType) { // TODO may not be needed
		TurretSettings settings = loadedTurretSettings.findItem(turretType);

		if (settings == null) {
			settings = new TurretSettings(turretType);

			loadedTurretSettings.loadOrCreateItem(turretType);
		}

		return settings;
	}*/

	private List<LevelData> levels;

	private List<UUID> playerBlacklist;

	private List<EntityType> mobBlacklist;

	private TurretSettings(final String turretType) {
		final String capitalizedType = StringUtil.capitalize(turretType);
		final String type = StringUtil.getStringBeforeSymbol(turretType, "-");

		setHeader(
				Common.configLine(),
				"You can edit settings for each turret type in game via a menu by using the '/lt settings' command (recommended).",
				"",
				"Edit your default " + capitalizedType + " Turret settings, everytime you create a turret these are the settings",
				"the " + type + " turrets will have by default. Feel free to add extra levels in the 'Levels' section and make",
				"sure to format the file properly. You can modify specific turrets in game by using the '/lt menu' command.",
				Common.configLine() + "\n");
		setPathPrefix(StringUtil.capitalize(type) + "_Turret_Default_Settings");

		this.loadConfiguration("turrets/" + turretType + ".yml"); // TODO
	}

	@Override
	protected void onLoad() {
		if (this.levels != null && this.playerBlacklist != null && this.mobBlacklist != null) {
			this.save();

			return;
		}

		this.playerBlacklist = this.getList("Player_Blacklist", UUID.class);
		this.mobBlacklist = this.getList("Mob_Blacklist", EntityType.class);
		this.levels = this.loadLevels();
	}

	@Override
	protected void onSave() {
		this.set("Player_Blacklist", UUID.class);
		this.set("Mob_Blacklist", EntityType.class);
		this.set("Levels", LevelData.class);
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

	public void setSettingsRange(final LevelData levelData, final int range) {
		levelData.setRange(range);

		save();
	}

	public LevelData addLevel() {
		final LevelData level = new LevelData(this.levels.size());

		this.levels.add(level);
		save();

		return this.levels.get(this.levels.size() - 1);
	}

	public void createSettingsLevel() {
		final LevelData level = addLevel();
		final List<TurretSettings.LevelData> levels = this.levels;

		levels.get(levels.size() - 1).setLevelSettings(level);
		save();
	}

	public void setLevelPrice(final LevelData levelData, final double price) {
		levelData.setPrice(price);

		save();
	}

	public void setLaserEnabled(final LevelData levelData, final boolean laserEnabled) {
		levelData.setLaserEnabled(laserEnabled);

		save();
	}

	public void setLaserDamage(final LevelData levelData, final double damage) {
		levelData.setLaserDamage(damage);

		save();
	}

	public void setLootChances(final LevelData levelData, final List<Tuple<ItemStack, Double>> lootChances) {
		levelData.setLootChances(lootChances);

		save();
	}

	public void addPlayerToBlacklist(final UUID uuid) {
		this.playerBlacklist.add(uuid);

		this.save();
	}

	public void removePlayerFromBlacklist(final UUID uuid) {
		if (this.playerBlacklist != null)
			this.playerBlacklist.remove(uuid);

		this.save();
	}

	public void addMobToBlacklist(final EntityType entityType) {
		this.mobBlacklist.add(entityType);

		this.save();
	}

	public void removeMobFromBlacklist(final EntityType entityType) {
		if (this.mobBlacklist != null)
			this.mobBlacklist.remove(entityType);

		this.save();
	}

	@Getter
	@Setter
	@ToString
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class LevelData implements ConfigSerializable {

		private final int level;

		private double price;

		private boolean laserEnabled;

		private double laserDamage;

		private int range;

		private int health;

		private List<Tuple<ItemStack, Double>> lootChances;

		public void setLevelData(final TurretData.TurretLevel turretLevel) {
			turretLevel.setRange(this.range);
			turretLevel.setPrice(this.price);
			turretLevel.setLaserEnabled(this.laserEnabled);
			turretLevel.setLaserDamage(this.laserDamage);
			turretLevel.setHealth(this.health);
			turretLevel.setLootChances(this.lootChances);
		}

		public void setLevelSettings(final LevelData levelData) {
			levelData.setRange(this.range);
			levelData.setPrice(this.price);
			levelData.setLaserEnabled(this.laserEnabled);
			levelData.setLaserDamage(this.laserDamage);
			levelData.setHealth(this.health);
			levelData.setLootChances(this.lootChances);
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

			levelData.laserEnabled = map.getBoolean("Enable_Laser_Pointers");
			Valid.checkNotNull(levelData.laserEnabled, "Missing 'Enable_Laser_Pointers' key from level: " + map);

			levelData.laserDamage = map.getDouble("Laser_Pointer_Damage");
			Valid.checkNotNull(levelData.price, "Missing 'Laser_Pointer_Damage' key from level: " + map);

			levelData.lootChances = map.getTupleList("Loot_Drops", ItemStack.class, Double.class);
			Valid.checkNotNull(levelData.lootChances, "Missing 'Loot_Drops' key from level: " + map);

			Valid.checkBoolean(map.isEmpty(), "Found unrecognized level settings: " + map);

			return levelData;
		}
	}

	// -----------------------------------------------------------------
	// Static
	// -----------------------------------------------------------------

	public static TurretSettings createSettings(@NonNull final String turretType) {
		return loadedTurretSettings.loadOrCreateItem(turretType);
	}

	public static void loadTurretSettings() {
		loadedTurretSettings.loadItems();
	}

	public static Collection<TurretSettings> getTurretSettings() {
		return loadedTurretSettings.getItems();
	}

	public static Set<String> getTurretSettingTypes() {
		return loadedTurretSettings.getItemNames();
	}

	public static TurretSettings findTurretSettings(final String type) {
		return loadedTurretSettings.findItem(type);
	}
}
