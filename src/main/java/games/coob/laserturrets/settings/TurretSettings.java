package games.coob.laserturrets.settings;

import games.coob.laserturrets.model.TurretData;
import lombok.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import java.util.*;

@Getter
public class TurretSettings extends YamlConfig {

	private static final ConfigItems<TurretSettings> loadedTurretSettings = ConfigItems.fromFolder("turrets", TurretSettings.class);

	private List<LevelData> levels;

	private Set<UUID> playerBlacklist = new HashSet<>();

	private Set<EntityType> mobBlacklist = new HashSet<>();

	private int turretLimit;

	private TurretSettings(final String turretType) {
		this.setPathPrefix("Turret_Settings");
		this.loadConfiguration("turret-data.yml", "turrets/" + turretType + "-turrets.yml");
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	@Override
	protected void onLoad() { // TODO ask question about this condition
		if (this.levels != null) {
			this.save();

			return;
		}

		this.turretLimit = this.getInteger("Turret_Limit");
		this.playerBlacklist = this.getSet("Player_Blacklist", UUID.class);
		this.mobBlacklist = this.getSet("Mob_Blacklist", EntityType.class);
		this.levels = this.getList("Levels", LevelData.class);
	}

	@Override
	protected void onSave() {
		this.set("Turret_Limit", this.turretLimit);
		this.set("Player_Blacklist", this.playerBlacklist);
		this.set("Mob_Blacklist", this.mobBlacklist);
		this.set("Levels", this.levels);
	}

	public void setSettingsRange(final LevelData levelData, final int range) {
		levelData.setRange(range);

		this.save();
	}

	public int getLevelsSize() {
		return this.levels.size();
	}

	public LevelData addLevel() {
		final List<LevelData> levels = this.levels;
		final LevelData level = new LevelData(levels.size() + 1);

		level.setLevelSettings(levels.get(levels.size() - 1), level);
		levels.add(level);
		this.save();

		return levels.get(levels.size() - 1);
	}

	public void setLevelPrice(final LevelData levelData, final double price) {
		levelData.setPrice(price);

		this.save();
	}

	public LevelData getLevel(final int level) {
		final boolean outOfBounds = level <= 0 || level >= this.levels.size() + 1;

		if (!outOfBounds)
			return this.levels.get(level - 1);

		return null;
	}
	
	public void removeLevel(final int settingsLevel) {
		this.levels.remove(settingsLevel - 1);
		this.save();
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

		public void setLevelSettings(final LevelData levelData, final LevelData newLevel) {
			newLevel.setRange(levelData.getRange());
			newLevel.setPrice(levelData.getPrice());
			newLevel.setLaserEnabled(levelData.isLaserEnabled());
			newLevel.setLaserDamage(levelData.getLaserDamage());
			newLevel.setHealth(levelData.getHealth());
			newLevel.setLootChances(levelData.getLootChances());
		}

		@Override
		public SerializedMap serialize() {
			final SerializedMap map = new SerializedMap();

			map.put("Level", this.level);
			map.put("Range", this.range);
			map.put("Price", this.price);
			map.put("Health", this.health);
			map.put("Enable_Laser_Pointer", this.laserEnabled);
			map.put("Laser_Pointer_Damage", this.laserDamage);
			map.putIf("Loot_Drops", this.lootChances);

			return map;
		}

		/*public static List<LevelData> deserialize(final SerializedMap map) {
			//final Map<Integer, LevelData> levels = new HashMap<>();
			final List<LevelData> levelDataList = new ArrayList<>();

			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				final int level = Integer.parseInt(entry.getKey());
				final LevelData levelData = new LevelData(level);
				final SerializedMap levelSettings = SerializedMap.of(entry.getValue());

				System.out.println("Level: " + level);
				System.out.println("Map: " + map);
				System.out.println("Entry: " + levelSettings);

				levelSettings.setRemoveOnGet(true);

				levelData.range = levelSettings.getInteger("Range");
				Valid.checkNotNull(levelData.range, "Missing 'Range' key from level: " + map);

				levelData.health = levelSettings.getInteger("Health");
				Valid.checkNotNull(levelData.health, "Missing 'Health' key from level: " + map);

				levelData.price = levelSettings.getDouble("Price");
				Valid.checkNotNull(levelData.price, "Missing 'Price' key from level: " + map);

				levelData.laserEnabled = levelSettings.getBoolean("Enable_Laser_Pointer");
				Valid.checkNotNull(levelData.laserEnabled, "Missing 'Enable_Laser_Pointers' key from level: " + map);

				levelData.laserDamage = levelSettings.getDouble("Laser_Pointer_Damage");
				Valid.checkNotNull(levelData.price, "Missing 'Laser_Pointer_Damage' key from level: " + map);

				levelData.lootChances = levelSettings.getTupleList("Loot_Drops", ItemStack.class, Double.class);
				Valid.checkNotNull(levelData.lootChances, "Missing 'Loot_Drops' key from level: " + map);

				Valid.checkBoolean(levelSettings.isEmpty(), "Found unrecognized level settings: " + map);

				//levels.put(level, levelData);
				levelDataList.add(level - 1, levelData);
			}

			System.out.println("Levels map: " + levelDataList);

			return levelDataList;
		}*/

		public static LevelData deserialize(final SerializedMap map) {
			final int range = map.getInteger("Range");
			final int health = map.getInteger("Health");
			final double price = map.getDouble("Price");
			final boolean enableLaserPointer = map.getBoolean("Enable_Laser_Pointer");
			final double laserPointerDamage = map.getDouble("Laser_Pointer_Damage");
			final List<Tuple<ItemStack, Double>> lootDrops = map.getTupleList("Loot_Drops", ItemStack.class, Double.class);

			final LevelData levelData = new LevelData(map.getInteger("Level"));

			levelData.setRange(range);
			levelData.setPrice(price);
			levelData.setHealth(health);
			levelData.setLaserEnabled(enableLaserPointer);
			levelData.setLaserDamage(laserPointerDamage);
			levelData.setLootChances(lootDrops);

			return levelData;

			/*System.out.println("Map : " + map);
			System.out.println("Values: " + map.values());
			final List<LevelData> dataList = new ArrayList<>();

			for (final Object object : map.values()) {
				final ConfigSection configSection = (ConfigSection) object;
				System.out.println("Level: " + configSection.retrieve("Level"));

				final int range = (int) configSection.retrieve("Range");
				final int health = (int) configSection.retrieve("Health");
				final double price = (double) configSection.retrieve("Price");
				final boolean enableLaserPointer = (boolean) configSection.retrieve("Enable_Laser_Pointer");
				final double laserPointerDamage = (double) configSection.retrieve("Laser_Pointer_Damage");
				final List<Tuple<ItemStack, Double>> lootDrops = (List<Tuple<ItemStack, Double>>) configSection.retrieve("Loot_Drops");

				final LevelData levelData = new LevelData((Integer) ((ConfigSection) object).retrieve("Level"));

				levelData.setRange(range);
				levelData.setPrice(price);
				levelData.setHealth(health);
				levelData.setLaserEnabled(enableLaserPointer);
				levelData.setLaserDamage(laserPointerDamage);
				levelData.setLootChances(lootDrops);

				dataList.add(levelData);
				System.out.println("LevelData: " + dataList);
			}

			System.out.println("DataList: " + dataList);

			return dataList;*/

			/*final int range = map.getInteger("Range");
			final int health = map.getInteger("Health");
			final double price = map.getDouble("Price");
			final boolean enableLaserPointer = map.getBoolean("Enable_Laser_Pointer");
			final double laserPointerDamage = map.getDouble("Laser_Pointer_Damage");
			final List<Tuple<ItemStack, Double>> lootDrops = map.getTupleList("Loot_Drops", ItemStack.class, Double.class);

			final LevelData levelData = new LevelData(map.getInteger("Level"));

			levelData.setRange(range);
			levelData.setPrice(price);
			levelData.setHealth(health);
			levelData.setLaserEnabled(enableLaserPointer);
			levelData.setLaserDamage(laserPointerDamage);
			levelData.setLootChances(lootDrops);*/
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
