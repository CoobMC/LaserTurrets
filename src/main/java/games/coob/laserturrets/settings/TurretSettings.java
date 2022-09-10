package games.coob.laserturrets.settings;

import games.coob.laserturrets.model.TurretData;
import lombok.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
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

	private List<LevelData> levels = new ArrayList<>();

	private Set<UUID> playerBlacklist = new HashSet<>();

	private Set<EntityType> mobBlacklist = new HashSet<>();

	private TurretSettings(final String turretType) {
		this.setPathPrefix("Turret_Settings");
		this.loadConfiguration("turret-data.yml", "turrets/" + turretType + "-turrets.yml");
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	@Override
	protected void onLoad() {
		if (!this.levels.isEmpty()) {
			this.save();

			return;
		}

		this.playerBlacklist = this.getSet("Player_Blacklist", UUID.class);
		this.mobBlacklist = this.getSet("Mob_Blacklist", EntityType.class);
		this.levels = this.loadLevels();
	}

	@Override
	protected void onSave() {
		this.set("Player_Blacklist", this.playerBlacklist);
		this.set("Mob_Blacklist", this.mobBlacklist);
		this.set("Levels", this.levels);
	}

	private List<LevelData> loadLevels() {
		final List<LevelData> levels = new ArrayList<>();

		for (final Map.Entry<Integer, Object> entry : this.getMap("Levels", Integer.class, Object.class).entrySet()) {
			final int level = entry.getKey();
			final SerializedMap levelSettings = SerializedMap.of(entry.getValue());

			levels.add(LevelData.deserialize(levelSettings, level));
		}

		return levels;
	}

	public void setSettingsRange(final LevelData levelData, final int range) {
		levelData.setRange(range);

		this.save();
	}

	public int getLevelsSize() {
		return this.levels.size();
	}

	public LevelData addLevel() {
		final LevelData level = new LevelData(this.levels.size() + 1);

		this.levels.add(level);
		this.save();

		return this.levels.get(this.levels.size() - 1);
	}

	public void createSettingsLevel() {
		final int size = this.levels.size();
		final LevelData level = getLevel(size - 1);
		final List<TurretSettings.LevelData> levels = this.levels;

		levels.get(size - 1).setLevelSettings(level);
		this.save();
	}

	public void setLevelPrice(final LevelData levelData, final double price) {
		levelData.setPrice(price);

		this.save();
	}

	public LevelData getLevel(final int level) {
		final boolean outOfBounds = level <= 0 || level >= this.levels.size();

		if (!outOfBounds)
			return this.levels.get(level - 1);

		return null;
	}

	public void setLaserEnabled(final LevelData levelData, final boolean laserEnabled) {
		levelData.setLaserEnabled(laserEnabled);

		this.save();
	}

	public void setLaserDamage(final LevelData levelData, final double damage) {
		levelData.setLaserDamage(damage);

		this.save();
	}

	public void setLootChances(final LevelData levelData, final List<Tuple<ItemStack, Double>> lootChances) {
		levelData.setLootChances(lootChances);

		this.save();
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
			turretLevel.setMaxHealth(this.health);
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
			final SerializedMap map = new SerializedMap();

			map.put("Range", this.range);
			map.put("Price", this.price);
			map.put("Health", this.health);
			map.put("Enable_Laser_Pointer", this.laserEnabled);
			map.put("Laser_Pointer_Damage", this.laserDamage);
			map.putIf("Loot_Drops", this.lootChances);

			return map;
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

			levelData.laserEnabled = map.getBoolean("Enable_Laser_Pointer");
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

	public static void createSettings(@NonNull final String turretType) {
		loadedTurretSettings.loadOrCreateItem(turretType, () -> new TurretSettings(turretType));
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

	public static boolean isTurretSettingLoaded(final String type) {
		return loadedTurretSettings.isItemLoaded(type);
	}

	public static TurretSettings findTurretSettings(final String type) {
		return loadedTurretSettings.findItem(type);
	}
}
