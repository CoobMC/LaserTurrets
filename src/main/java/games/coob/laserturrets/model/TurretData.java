package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.util.*;

@Getter
@Setter
public class TurretData extends YamlConfig {

	/**
	 * The folder name where all items are stored
	 */
	private static final String FOLDER = "turrets";

	private static final ConfigItems<TurretData> loadedFiles = ConfigItems.fromFolder(FOLDER, TurretData.class);

	private Location location;

	private CompMaterial material;

	private String id;

	private String type;

	private UUID owner;

	private boolean broken;

	private List<ItemStack> currentLoot;

	private Set<UUID> playerBlacklist = new HashSet<>();

	private Set<EntityType> mobBlacklist = new HashSet<>();

	private boolean mobWhitelistEnabled;

	private boolean playerWhitelistEnabled;

	private List<TurretLevel> turretLevels = new ArrayList<>();

	private Double currentHealth;

	private int currentLevel;

	@Nullable
	private ItemStack unplacedTurret;

	private Hologram hologram;

	private TurretData(final String id) {
		this.id = id;
		/*this.material = CompMaterial.fromMaterial(block.getType());
		this.location = block.getLocation();*/

		this.loadConfiguration(NO_DEFAULT, FOLDER + "/" + id + ".yml");
	}

	@Override
	protected void onLoad() {
		if (this.location == null && this.material == null) {
			Valid.checkBoolean(isSet("Block"), "Corrupted turret file: " + this.getFileName() + ", lacks the 'Block' key to determine the block where the turret is placed.");

			final String hash = this.getString("Block");

			final String[] split = hash.split(" \\| ");
			final Location location = SerializeUtil.deserializeLocation(split[0]);
			final CompMaterial material = CompMaterial.valueOf(split[1]);

			this.location = location;
			this.material = material;
		}

		this.id = this.getString("Id", "id");
		this.type = this.getString("Type", "type");
		this.owner = this.get("Owner", UUID.class, new UUID(1, 5));
		this.currentHealth = this.getDouble("Current_Health", 0.0);
		this.playerBlacklist = this.getSet("Player_Blacklist", UUID.class);
		this.mobBlacklist = this.getSet("Mob_Blacklist", EntityType.class);
		this.currentLoot = this.getList("Current_Loot", ItemStack.class);
		this.playerWhitelistEnabled = this.getBoolean("Use_Player_Whitelist", false);
		this.mobWhitelistEnabled = this.getBoolean("Use_Mob_Whitelist", false); // Add default value to load it if the key doesn't exist
		this.currentLevel = this.getInteger("Current_Level", 1);
		this.broken = this.getBoolean("Broken", false);
		this.unplacedTurret = this.getItemStack("Unplaced_Turret");
		this.turretLevels = this.getList("Levels", TurretLevel.class);

		this.save();
	}

	private String toHash(final Location location, final CompMaterial material) {
		return SerializeUtil.serializeLoc(location) + " | " + material;
	}

	@Override
	protected void onSave() {
		this.set("Block", toHash(this.location, this.material));
		this.set("Id", this.id);
		this.set("Type", this.type);
		this.set("Owner", this.owner);
		this.set("Player_Blacklist", this.playerBlacklist);
		this.set("Mob_Blacklist", this.mobBlacklist);
		this.set("Current_Loot", this.currentLoot);
		this.set("Use_Player_Whitelist", this.playerWhitelistEnabled);
		this.set("Use_Mob_Whitelist", this.mobWhitelistEnabled);
		this.set("Current_Health", this.currentHealth);
		this.set("Current_Level", this.currentLevel);
		this.set("Broken", this.broken);
		this.set("Hologram", this.hologram);
		this.set("Unplaced_Turret", this.unplacedTurret);
		this.set("Levels", this.turretLevels);
	}

	public void register(final Player player, final Block block, final String type, final String uniqueID) { // TODO register and create file
		//Valid.checkBoolean(!loadedFiles.isItemLoaded(uniqueID), Lang.of("Tool.Already_Registered", "{location}", Common.shortLocation(block.getLocation())));

		this.setLocation(block.getLocation());
		this.setMaterial(CompMaterial.fromMaterial(block.getType()));
		this.setType(type);
		this.setOwner(player.getUniqueId());
		this.setId(uniqueID);
		this.setCurrentLevel(1);

		final TurretSettings turretSettings = TurretSettings.findByName(type);

		this.setMobBlacklist(turretSettings.getMobList());
		this.setPlayerBlacklist(turretSettings.getPlayerList());
		this.setPlayerWhitelistEnabled(turretSettings.isEnablePlayerWhitelist());
		this.setMobWhitelistEnabled(turretSettings.isEnableMobWhitelist());

		if (!this.isPlayerWhitelistEnabled())
			this.playerBlacklist.add(player.getUniqueId());

		for (final TurretSettings.LevelData levelData : turretSettings.getLevels()) {
			final TurretData.TurretLevel level = addLevel();
			levelData.setLevelData(level);
		}

		this.setCurrentHealth(this.getLevel(1).getMaxHealth());
		this.setHologram(createHologram());

		this.save();
	}

	public void unregister() {
		if (this.getHologram() != null)
			this.getHologram().remove();

		this.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		removeTurret(this.id);

		this.save();
	}

	public void registerUnplacedTurret(final Block block) {
		if (this.unplacedTurret != null) {
			this.setUnplacedTurret(null);
			this.setLocation(block.getLocation());
			this.setMaterial(CompMaterial.fromMaterial(block.getType()));
			this.setHologram(createHologram());
		}

		this.save();
	}

	/*public void registerTurretById(final Block block, final String turretId) { // TODO create unplaced boolean value
		final TurretData turretData = getUnplacedTurretById(turretId);

		this.registeredUnplacedTurrets.removeIf(tuple -> tuple.getKey().equals(turretData));

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setHologram(createHologram(turretData));

		this.registeredTurrets.add(turretData);
		this.save();
	}*/

	private Hologram createHologram() {
		final List<String> lore = Lang.ofList("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(this.getType()), "{owner}", Remain.getOfflinePlayerByUUID(this.getOwner()).getName(), "{level}", MathUtil.toRoman(this.getCurrentLevel()), "{health}", this.getCurrentHealth());
		final List<String> loreList = new ArrayList<>(lore);

		if (!Settings.TurretSection.ENABLE_DAMAGEABLE_TURRETS)
			loreList.removeIf(line -> line.contains(String.valueOf(this.getCurrentHealth())));

		final Object[] objects = loreList.toArray();
		final String[] lines = Arrays.copyOf(objects, objects.length, String[].class);
		final int linesLength = objects.length;
		final Hologram hologram = new Hologram(this.getLocation().clone().add(0.5, TurretUtil.getYForLines(linesLength), 0.5));

		hologram.setLore(lines);

		return hologram;
	}

	/*public void unregister(final Block block) {
		final TurretData turretData = getTurretByBlock(block);

		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

		block.getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		this.registeredTurrets.remove(turretData);

		this.save();
	}*/

	/*public void unregister(final TurretData turretData) {
		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

		turretData.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		this.registeredTurrets.remove(turretData);

		this.save();
	}*/

	/*public void unregister(final String turretID) {
		final TurretData turretData = getTurretByID(turretID);

		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

		turretData.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		this.registeredTurrets.remove(turretData);

		this.save();
	}*/

	public void setUnplacedTurret(@Nullable final ItemStack turretItem) {
		this.unplacedTurret = turretItem;

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
		this.mobBlacklist.remove(entityType);

		this.save();
	}

	public void enableMobWhitelist(final boolean enableWhitelist) {
		this.mobWhitelistEnabled = enableWhitelist;

		this.save();
	}

	public boolean isMobListedAsAlly(final EntityType entityType) {
		return this.mobBlacklist.contains(entityType);
	}

	public void enablePlayerWhitelist(final boolean enableWhitelist) {
		this.playerWhitelistEnabled = enableWhitelist;

		this.save();
	}

	public boolean isPlayerListedAsAlly(final UUID uuid) {
		if (this.playerBlacklist != null)
			return this.playerBlacklist.contains(uuid);
		else return false;
	}

	public void setRange(final int level, final int range) {
		this.getLevel(level).setRange(range);

		this.save();
	}

	public void setLaserEnabled(final int level, final boolean laserEnabled) {
		this.getLevel(level).setLaserEnabled(laserEnabled);

		this.save();
	}

	public void setLaserDamage(final int level, final double damage) {
		this.getLevel(level).setLaserDamage(damage);

		this.save();
	}

	public void updateHologram() {
		this.getHologram().remove();
		this.setHologram(createHologram());

		this.save();
	}

	public void setLevelPrice(final int level, final double price) {
		this.getLevel(level).setPrice(price);

		this.save();
	}

	public TurretLevel getLevel(final int level) {
		final boolean outOfBounds = level <= 0 || level >= this.turretLevels.size() + 1;

		if (!outOfBounds)
			return this.turretLevels.get(level - 1);

		return null;
	}

	public void createLevel() {
		final TurretLevel level = addLevel();
		final List<TurretSettings.LevelData> levels = TurretSettings.findByName(this.type).getLevels();

		levels.get(levels.size() - 1).setLevelData(level);

		this.save();
	}

	public void removeLevel(final int level) {
		Valid.checkBoolean(getLevels() >= level, "Cannot remove level " + level + " because the turret only has " + getLevels() + " levels.");
		this.turretLevels.remove(level - 1);

		this.save();
	}

	public int getLevels() {
		return this.turretLevels.size();
	}

	public void setCurrentTurretLevel(final int level) {
		this.currentLevel = level;

		this.save();
	}

	public TurretLevel addLevel() {
		final TurretLevel level = new TurretLevel(this);

		turretLevels.add(level);

		return this.turretLevels.get(this.turretLevels.size() - 1);
	}

	public void setTurretHealth(final Block block, final double health) {
		if (isRegistered(block))
			this.setCurrentHealth(health);

		this.save();
	}

	public void setTurretHealth(final double health) {
		this.currentHealth = health;

		this.save();
	}

	public void setBrokenAndFill(final Block block, final boolean broken) {
		if (isRegistered(block)) {
			this.broken = broken;

			if (this.isBroken()) {
				final List<Tuple<ItemStack, Double>> lootChances = this.turretLevels.get(this.currentLevel - 1).getLootChances();
				this.currentLoot = randomItemPercentageList(lootChances);
			}

			this.save();
		}
	}

	private List<ItemStack> randomItemPercentageList(final List<Tuple<ItemStack, Double>> lootChanceList) {
		final List<ItemStack> items = new ArrayList<>();

		for (final Tuple<ItemStack, Double> lootChance : lootChanceList) {
			if (lootChance != null) {
				final Random random = new Random();
				final double randomPercentage = random.nextDouble();

				if (lootChance.getValue() >= randomPercentage)
					items.add(lootChance.getKey());
			}
		}

		return items;
	}

	public void setBroken(final boolean broken) {
		this.broken = broken;

		if (!this.isBroken())
			setTurretHealth(this.getLevel(this.getCurrentLevel()).getMaxHealth());

		this.save();
	}

	public List<Tuple<ItemStack, Double>> getTurretLootChances(final int level) {
		return this.getLevel(level).getLootChances();
	}

	public void setTurretLootChances(final int level, final List<Tuple<ItemStack, Double>> lootChances) {
		this.getLevel(level).setLootChances(lootChances);

		this.save();
	}

	public void setCurrentLoot(@Nullable final List<ItemStack> items) {
		this.currentLoot = items;

		this.save();
	}

	public void removeCurrentLoot(final ItemStack item) {
		this.currentLoot.remove(item);

		this.save();
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	// -----------------------------------------------------------------
	// Turret levels
	// -----------------------------------------------------------------

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public final static class TurretLevel implements ConfigSerializable {

		private final TurretData turretData;

		@Getter
		private double price;

		@Getter
		private List<Tuple<ItemStack, Double>> lootChances = new ArrayList<>();

		@Getter
		private int range;

		@Getter
		private boolean laserEnabled;

		@Getter
		private double laserDamage;

		@Getter
		private double maxHealth;

		public void setPrice(final double price) {
			this.price = price;
		}

		public void setRange(final int range) {
			this.range = range;
		}

		public void setLaserEnabled(final boolean laserEnabled) {
			this.laserEnabled = laserEnabled;
		}

		public void setLaserDamage(final double laserDamage) {
			this.laserDamage = laserDamage;
		}

		public void setLootChances(final @Nullable List<Tuple<ItemStack, Double>> lootChances) {
			this.lootChances = lootChances;
		}

		public void setMaxHealth(final double health) {
			this.maxHealth = health;
		}

		public static TurretLevel deserialize(final SerializedMap map, final TurretData turretData) {
			final double price = map.getDouble("Price");
			final List<Tuple<ItemStack, Double>> lootChances = map.getTupleList("Loot_Chances", ItemStack.class, Double.class);
			final int range = map.getInteger("Range");
			final boolean laserEnabled = map.getBoolean("Laser_Enabled");
			final double laserDamage = map.getDouble("Laser_Damage");
			final double maxHealth = map.getDouble("Max_Health");

			final TurretLevel level = new TurretLevel(turretData);

			level.setPrice(price);
			level.setLootChances(lootChances);
			level.setRange(range);
			level.setLaserEnabled(laserEnabled);
			level.setLaserDamage(laserDamage);
			level.setMaxHealth(maxHealth);

			return level;
		}

		@Override
		public SerializedMap serialize() {
			final SerializedMap map = new SerializedMap();

			map.put("Price", this.price);
			map.put("Range", this.range);
			map.put("Max_Health", this.maxHealth);
			map.put("Laser_Enabled", this.laserEnabled);
			map.put("Laser_Damage", this.laserDamage);
			map.putIf("Loot_Chances", this.lootChances);

			return map;
		}
	}

	// -----------------------------------------------------------------
	// Static
	// -----------------------------------------------------------------

	public static void syncTurretDataWithSettings(final TurretSettings settings, final TurretData turretData) {
		turretData.setMobBlacklist(settings.getMobList());
		turretData.setPlayerBlacklist(settings.getPlayerList());
		turretData.setPlayerWhitelistEnabled(settings.isEnablePlayerWhitelist());
		turretData.setMobWhitelistEnabled(settings.isEnableMobWhitelist());

		if (!turretData.isPlayerWhitelistEnabled())
			turretData.addPlayerToBlacklist(turretData.getOwner());

		for (int i = 1; i <= turretData.getLevels() + 2; i++)
			turretData.removeLevel(1);

		for (final TurretSettings.LevelData levelData : settings.getLevels()) {
			final TurretLevel level = turretData.addLevel();
			levelData.setLevelData(level);
		}

		final double maxHealth = turretData.getLevel(turretData.getCurrentLevel()).getMaxHealth();

		if (turretData.getCurrentHealth() > maxHealth)
			turretData.setCurrentHealth(maxHealth);

		TurretUtil.updateHologramAndTexture(turretData);

		turretData.save();
	}

	public static boolean isRegistered(final Block block) {
		for (final TurretData turretData : getTurrets())
			return turretData.getLocation().getBlock().getLocation().equals(block.getLocation());

		return false;
	}

	public static boolean isRegistered(final String turretID) {
		for (final TurretData turretData : getTurrets()) {
			if (turretData.getId().equals(turretID))
				return true;
		}

		return false;
	}

	public static TurretData findByBlock(final Block block) {
		for (final TurretData turretData : getTurrets())
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return turretData;

		return null;
	}

	public static Set<TurretData> getTurretsOfType(final String turretType) {
		final Set<TurretData> dataList = new HashSet<>();

		for (final TurretData turretData : getTurrets())
			if (turretData.getType().equals(turretType))
				dataList.add(turretData);

		return dataList;
	}

	public static Set<TurretData> getRegisteredTurrets() {
		return new HashSet<>(getTurrets());
	}

	public static List<Location> getTurretLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			locations.add(turretData.getLocation());

		return locations;
	}

	public static List<TurretData> getUnplacedTurrets() {
		final List<TurretData> turretDataList = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			if (turretData.getUnplacedTurret() != null)
				turretDataList.add(turretData);

		return turretDataList;
	}

	public static Set<String> getTurretIDs() {
		final Set<String> turretIDs = new HashSet<>();

		for (final TurretData turretData : getTurrets())
			turretIDs.add(turretData.getId());

		return turretIDs;
	}

	public static List<Location> getTurretLocationsOfType(final String type) {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : getTurretsOfType(type))
			locations.add(turretData.getLocation());

		return locations;
	}

	public static boolean isTurretOfType(final Block block, final String type) {
		for (final TurretData turretData : getTurretsOfType(type))
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return true;

		return false;
	}

	/**
	 * @return
	 * @see ConfigItems#getItems()
	 */
	public static List<? extends TurretData> getTurrets() {
		return loadedFiles.getItems();
	}

	/**
	 * @return
	 * @see ConfigItems#getItemNames()
	 */
	public static Set<String> getTurretNames() {
		return loadedFiles.getItemNames();
	}

	public static TurretData createTurret(@NonNull final String turretId) { // TODO
		return loadedFiles.loadOrCreateItem(turretId, () -> new TurretData(turretId));
	}

	/**
	 * @see ConfigItems#loadItems()
	 */
	public static void loadTurrets() {
		loadedFiles.loadItems();
	}

	public static void removeTurret(final String turretId) {
		loadedFiles.removeItemByName(turretId);
	}

	/**
	 * @see ConfigItems#isItemLoaded(String)
	 */
	public static boolean isTurretLoaded(final String id) {
		return loadedFiles.isItemLoaded(id);
	}

	/**
	 * @return
	 * @see ConfigItems#findItem(String)
	 */
	public static TurretData findById(@NonNull final String id) {
		return loadedFiles.findItem(id);
	}
}
