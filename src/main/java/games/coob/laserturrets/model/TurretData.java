package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class TurretData extends YamlConfig { // TODO store number of kills

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

	private Double currentHealth;

	private int currentLevel;

	private Hologram hologram;

	private List<ItemStack> ammo;

	private TurretData(final String id) {
		this(id, null);
	}

	private TurretData(final String id, @Nullable final Block block) {
		this.id = id;

		if (block != null) {
			this.material = CompMaterial.fromMaterial(block.getType());
			this.location = block.getLocation();
		}

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
		this.ammo = this.getList("Ammo", ItemStack.class);

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
		this.set("Ammo", this.ammo);
		this.set("Hologram", this.hologram);
	}

	public void setLocation(final Location location) {
		this.location = location;

		this.save();
	}

	public void setMaterial(final CompMaterial material) {
		this.material = material;

		this.save();
	}

	public void setType(final String type) {
		this.type = type;

		this.save();
	}

	public void setOwner(final UUID owner) {
		this.owner = owner;

		this.save();
	}

	public void setId(final String id) {
		this.id = id;

		this.save();
	}

	public void setCurrentHealth(final Double currentHealth) {
		this.currentHealth = currentHealth;

		this.save();
	}

	public void setCurrentLevel(final int currentLevel) {
		this.currentLevel = currentLevel;

		this.save();
	}

	public void setMobBlacklist(final Set<EntityType> mobBlacklist) {
		this.mobBlacklist = mobBlacklist;

		this.save();
	}

	public void setPlayerBlacklist(final Set<UUID> playerBlacklist) {
		this.playerBlacklist = playerBlacklist;

		this.save();
	}

	public void setPlayerWhitelistEnabled(final boolean playerWhitelistEnabled) {
		this.playerWhitelistEnabled = playerWhitelistEnabled;

		this.save();
	}

	public void setMobWhitelistEnabled(final boolean mobWhitelistEnabled) {
		this.mobWhitelistEnabled = mobWhitelistEnabled;

		this.save();
	}

	public void setHologram(final Hologram hologram) {
		this.hologram = hologram;

		this.save();
	}

	public void setAmmo(final List<ItemStack> ammo) {
		this.ammo = ammo;

		this.save();
	}

	public void deductAmmo() {
		int remainingDeduction = 1;
		
		for (final ItemStack itemStack : this.ammo) {

			if (itemStack == null)
				continue;

			final int currentStackSize = itemStack.getAmount();

			if (currentStackSize > remainingDeduction) {
				itemStack.setAmount(currentStackSize - remainingDeduction);
				break;
			} else {
				itemStack.setAmount(0);
				if (currentStackSize < remainingDeduction) {
					remainingDeduction -= currentStackSize;
				} else {
					remainingDeduction = 0;
				}

				if (remainingDeduction == 0) {
					break;
				}
			}
		}

		this.save();
	}

	public boolean hasAmmo() {
		return !this.ammo.isEmpty();
	}


	public void register(final Player player, final Block block, final String type, final String uniqueID) { // TODO register and create file
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

		this.setCurrentHealth(turretSettings.getLevel(1).getHealth());
		this.setHologram(createHologram());

		this.save();
	}

	public void unregister() {
		removeTurret(this.id);

		if (this.getHologram() != null)
			this.getHologram().remove();

		this.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
	}

	public void registerFromUnplaced(final UnplacedData data, final Block block) {
		this.setLocation(block.getLocation());
		this.setMaterial(CompMaterial.fromMaterial(block.getType()));
		this.setType(data.getType());
		this.setOwner(data.getOwner());
		this.setId(data.getId());
		this.setCurrentLevel(data.getCurrentLevel());
		this.setMobBlacklist(data.getMobBlacklist());
		this.setPlayerBlacklist(data.getPlayerBlacklist());
		this.setPlayerWhitelistEnabled(data.isPlayerWhitelistEnabled());
		this.setMobWhitelistEnabled(data.isMobWhitelistEnabled());
		this.setCurrentHealth(data.getCurrentHealth());
		this.setCurrentLoot(data.getCurrentLoot());
		this.setHologram(createHologram());

		this.save();
	}

	private Hologram createHologram() { // TODO
		final String type = this.type;
		final List<String> lore = Lang.ofList("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(type), "{owner}", Remain.getOfflinePlayerByUUID(this.getOwner()).getName(), "{level}", MathUtil.toRoman(this.getCurrentLevel()), "{health}", this.getCurrentHealth());
		final List<String> loreList = new ArrayList<>(lore);

		if (TurretSettings.findByName(type).isInvincible())
			loreList.removeIf(line -> line.contains(String.valueOf(this.getCurrentHealth())));

		final Object[] objects = loreList.toArray();
		final String[] lines = Arrays.copyOf(objects, objects.length, String[].class);
		final int linesLength = objects.length;
		final Hologram hologram = new Hologram(this.getLocation().clone().add(0.5, TurretUtil.getYForLines(linesLength), 0.5));

		hologram.setLore(lines);

		return hologram;
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

	public void updateHologram() {
		if (this.getHologram() != null)
			this.getHologram().remove();

		this.setHologram(createHologram());

		this.save();
	}

	public void setCurrentTurretLevel(final int level) {
		this.currentLevel = level;

		this.save();
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

			final TurretSettings turretSettings = TurretSettings.findByName(type);

			if (this.isBroken()) {
				final List<Tuple<ItemStack, Double>> lootChances = turretSettings.getLevel(this.currentLevel).getLootChances();
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

		final TurretSettings turretSettings = TurretSettings.findByName(type);

		if (!this.isBroken())
			setTurretHealth(turretSettings.getLevel(this.getCurrentLevel()).getHealth());

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
	// Static
	// -----------------------------------------------------------------

	public static boolean isRegistered(final Block block) {
		for (final TurretData turretData : getTurrets()) {
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return true;
		}

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

	public static List<TurretData> getTurretsOfType(final String turretType) {
		final List<TurretData> dataList = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			if (turretData.getType().equals(turretType))
				dataList.add(turretData);

		return dataList;
	}

	public static List<Location> getTurretLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			locations.add(turretData.getLocation());

		return locations;
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
	public static Set<String> getTurretIDs() {
		return loadedFiles.getItemNames();
	}

	public static TurretData createTurret(@NonNull final String turretId, final Block block) {
		return loadedFiles.loadOrCreateItem(turretId, () -> new TurretData(turretId, block));
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
