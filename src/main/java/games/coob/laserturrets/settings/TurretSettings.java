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

	private Set<UUID> playerList = new HashSet<>();

	private Set<EntityType> mobList = new HashSet<>();

	private boolean enableMobWhitelist;

	private boolean enablePlayerWhitelist;

	private String base64Texture;

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
		this.playerList = this.getSet("Player_Blacklist", UUID.class);
		this.mobList = this.getSet("Mob_Blacklist", EntityType.class);
		this.enableMobWhitelist = this.getBoolean("Use_Mob_Whitelist");
		this.enablePlayerWhitelist = this.getBoolean("Use_Player_Whitelist");
		this.base64Texture = this.getString("Head_Texture");
		this.levels = this.getList("Levels", LevelData.class);
	}

	@Override
	protected void onSave() {
		this.set("Turret_Limit", this.turretLimit);
		this.set("Player_Blacklist", this.playerList);
		this.set("Mob_Blacklist", this.mobList);
		this.set("Use_Player_Whitelist", this.enablePlayerWhitelist);
		this.set("Use_Mob_Whitelist", this.enableMobWhitelist);
		this.set("Head_Texture", this.base64Texture);
		this.set("Levels", this.levels);
	}

	public void setSettingsRange(final LevelData levelData, final int range) {
		levelData.setRange(range);

		this.save();
	}

	public void setTurretLimit(final int turretLimit) {
		this.turretLimit = turretLimit;

		this.save();
	}

	public void setBase64Texture(final String texture) {
		this.base64Texture = texture;

		this.save();
	}

	public int getLevelsSize() {
		return this.levels.size();
	}

	public LevelData addLevel() {
		final List<LevelData> levels = this.levels;
		final LevelData level = new LevelData();

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

	public void setHealth(final LevelData levelData, final double health) {
		levelData.setHealth(health);

		this.save();
	}

	public void setLootChances(final LevelData levelData, final List<Tuple<ItemStack, Double>> lootChances) {
		levelData.setLootChances(lootChances);

		this.save();
	}

	public void addPlayerToBlacklist(final UUID uuid) {
		this.playerList.add(uuid);

		this.save();
	}

	public void removePlayerFromBlacklist(final UUID uuid) {
		if (this.playerList != null)
			this.playerList.remove(uuid);

		this.save();
	}

	public void addMobToBlacklist(final EntityType entityType) {
		this.mobList.add(entityType);

		this.save();
	}

	public void removeMobFromBlacklist(final EntityType entityType) {
		if (this.mobList != null)
			this.mobList.remove(entityType);

		this.save();
	}

	public void enableMobWhitelist(final boolean enableMobWhitelist) {
		this.enableMobWhitelist = enableMobWhitelist;

		this.save();
	}

	public void enablePlayerWhitelist(final boolean enablePlayerWhitelist) {
		this.enablePlayerWhitelist = enablePlayerWhitelist;

		this.save();
	}

	@Getter
	@Setter
	@ToString
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class LevelData implements ConfigSerializable {


		private double price;

		private boolean laserEnabled;

		private double laserDamage;

		private int range;

		private double health;

		private List<Tuple<ItemStack, Double>> lootChances;

		public void setLevelData(final TurretData.TurretLevel turretLevel) {
			//turretLevel.setLevel(this.level + upgradeValue);
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

			//map.put("Level", this.level);
			map.put("Range", this.range);
			map.put("Price", this.price);
			map.put("Health", this.health);
			map.put("Enable_Laser_Pointer", this.laserEnabled);
			map.put("Laser_Pointer_Damage", this.laserDamage);
			map.putIf("Loot_Drops", this.lootChances);

			return map;
		}

		public static LevelData deserialize(final SerializedMap map) {
			final int range = map.getInteger("Range");
			final double health = map.getDouble("Health");
			final double price = map.getDouble("Price");
			final boolean enableLaserPointer = map.getBoolean("Enable_Laser_Pointer");
			final double laserPointerDamage = map.getDouble("Laser_Pointer_Damage");
			final List<Tuple<ItemStack, Double>> lootDrops = map.getTupleList("Loot_Drops", ItemStack.class, Double.class);

			final LevelData levelData = new LevelData();

			levelData.setRange(range);
			levelData.setPrice(price);
			levelData.setHealth(health);
			levelData.setLaserEnabled(enableLaserPointer);
			levelData.setLaserDamage(laserPointerDamage);
			levelData.setLootChances(lootDrops);

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
