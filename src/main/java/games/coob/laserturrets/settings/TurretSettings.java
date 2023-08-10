package games.coob.laserturrets.settings;

import games.coob.laserturrets.util.Triple;
import lombok.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public abstract class TurretSettings extends YamlConfig {

	// TODO Create possibility to allow players to build their own turrets, creating a file for their own turret types
	// TODO Players will be able to create turret types via a menu

	/**
	 * The folder name where all items are stored
	 */
	private static final String FOLDER = "types";

	/**
	 * The config helper instance which loads and saves items
	 */
	private static final ConfigItems<? extends TurretSettings> loadedFiles = ConfigItems.fromFolder(FOLDER, fileName -> {
		final YamlConfig config = YamlConfig.fromFileFast(FileUtil.getFile(FOLDER + "/" + fileName + ".yml"));
		final TurretType type = config.get("Type", TurretType.class);

		Valid.checkNotNull(type, "Unrecognized TurretType." + config.getObject("Type") + " in " + fileName + "! Available: " + Common.join(TurretType.values()));
		return type.getInstanceClass();
	});

	private List<LevelData> levels;

	private Set<UUID> playerList = new HashSet<>();

	private Set<EntityType> mobList = new HashSet<>();

	private boolean enableMobWhitelist;

	private boolean enablePlayerWhitelist;

	private boolean invincible;

	private int turretLimit;

	private Triple<Boolean, ItemStack, Double> ammo;

	protected TurretSettings(final String turretName, @Nullable final TurretType type) {
		this.loadConfiguration(FOLDER + "/" + type.toString().toLowerCase() + ".yml", FOLDER + "/" + turretName + ".yml");
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	@Override
	protected void onLoad() {
		this.turretLimit = this.getInteger("Turret_Limit");
		this.playerList = this.getSet("Player_Blacklist", UUID.class);
		this.mobList = this.getSet("Mob_Blacklist", EntityType.class);
		this.enableMobWhitelist = this.getBoolean("Use_Mob_Whitelist");
		this.enablePlayerWhitelist = this.getBoolean("Use_Player_Whitelist");
		this.invincible = this.getBoolean("Invincible");
		this.ammo = this.isSet("Ammo") ? this.getTriple("Ammo", Boolean.class, ItemStack.class, Double.class) : null;
		this.levels = this.getList("Levels", LevelData.class);
	}

	@Override
	protected void onSave() {
		this.set("Turret_Limit", this.turretLimit);
		this.set("Player_Blacklist", this.playerList);
		this.set("Mob_Blacklist", this.mobList);
		this.set("Use_Player_Whitelist", this.enablePlayerWhitelist);
		this.set("Use_Mob_Whitelist", this.enableMobWhitelist);
		this.set("Invincible", this.invincible);
		this.set("Ammo", this.ammo);
		this.set("Levels", this.levels);
	}

	public abstract String getHeadTexture();

	public abstract void setHeadTexture(String texture);

	public abstract ItemStack getToolItem();

	public abstract void setToolItem(ItemStack itemStack);

	public void setSettingsRange(final LevelData levelData, final int range) {
		levelData.setRange(range);

		this.save();
	}

	public void setTurretLimit(final int turretLimit) {
		this.turretLimit = turretLimit;

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

	public void setInvincible(final boolean invincible) {
		this.invincible = invincible;

		this.save();
	}

	public void setAmmoEnabled(final boolean enableAmmo) {
		this.ammo.setFirstValue(enableAmmo);

		this.save();
	}

	public void setAmmoItem(final ItemStack itemStack) {
		this.ammo.setSecondValue(itemStack);

		this.save();
	}

	public void setAmmoPrice(final double price) {
		this.ammo.setThirdValue(price);

		this.save();
	}

	public void createAmmo(final boolean enableAmmo, final ItemStack itemStack, final double price) {
		this.ammo = new Triple<>(enableAmmo, itemStack, price);

		this.save();
	}

	private <A, B, C> Triple<A, B, C> getTriple(final String key, final Class<A> firstType, final Class<B> secondType, final Class<C> thirdType) {
		return this.getTriple(key, null, firstType, secondType, thirdType);
	}

	private <A, B, C> Triple<A, B, C> getTriple(final String key, final Triple<A, B, C> def, final Class<A> firstType, final Class<B> secondType, final Class<C> thirdType) {
		return this.get(key, Triple.class, def, firstType, secondType, thirdType);
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

	/**
	 * @return
	 * @see ConfigItems#getItems()
	 */
	public static List<? extends TurretSettings> getTurrets() {
		return loadedFiles.getItems();
	}

	/**
	 * @return
	 * @see ConfigItems#getItemNames()
	 */
	public static Set<String> getTurretNames() {
		return loadedFiles.getItemNames();
	}

	/**
	 * @param name
	 * @param type
	 * @see ConfigItems#loadOrCreateItem(String, java.util.function.Supplier)
	 */
	public static void createTurretType(@NonNull final String name, @NonNull final TurretType type) {
		loadedFiles.loadOrCreateItem(name, () -> type.instantiate(name));
	}

	// 1) /game new bedwars Arena1
	// 2) when we load a disk file

	/**
	 * @see ConfigItems#loadItems()
	 */
	public static void loadTurrets() {
		loadedFiles.loadItems();
	}

	/**
	 * @param gameName
	 */
	public static void removeTurretType(final String gameName) {
		loadedFiles.removeItemByName(gameName);
	}

	/**
	 * @param name
	 * @return
	 * @see ConfigItems#isItemLoaded(String)
	 */
	public static boolean isTurretLoaded(final String name) {
		return loadedFiles.isItemLoaded(name);
	}

	/**
	 * @param name
	 * @return
	 * @see ConfigItems#findItem(String)
	 */
	public static TurretSettings findByName(@NonNull final String name) {
		return loadedFiles.findItem(name);
	}
}
