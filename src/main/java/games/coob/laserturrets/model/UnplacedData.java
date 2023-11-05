package games.coob.laserturrets.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UnplacedData extends YamlConfig {

	/**
	 * The folder name where all items are stored
	 */
	private static final String FOLDER = "unplaced";

	private static final ConfigItems<UnplacedData> loadedFiles = ConfigItems.fromFolder(FOLDER, UnplacedData.class);

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

	private ItemStack turretItem;

	private List<ItemStack> ammo;

	private UnplacedData(final String id) {
		this(id, null);
	}

	private UnplacedData(final String id, @Nullable final String type) {
		this.id = id;

		if (type != null) {
			this.type = type;
		}

		this.loadConfiguration(NO_DEFAULT, FOLDER + "/" + id + ".yml");
	}

	@Override
	protected void onLoad() {
		if (this.type == null) {
			Valid.checkBoolean(isSet("Type"), "Corrupted turret file: " + this.getFileName() + ", lacks the 'Type' key to determine the type of the turret.");

			this.type = this.getString("Type");
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
		this.turretItem = this.getItemStack("Turret_Item");
		this.ammo = this.getList("Ammo", ItemStack.class);

		this.save();
	}

	@Override
	protected void onSave() {
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
		this.set("Turret_Item", this.turretItem);
	}

	public void registerFromTurret(final TurretData turretData, final ItemStack turretItem) {
		this.setType(turretData.getType());
		this.setOwner(turretData.getOwner());
		this.setId(turretData.getId());
		this.setCurrentLevel(turretData.getCurrentLevel());
		this.setMobBlacklist(turretData.getMobAllies());
		this.setPlayerBlacklist(turretData.getPlayerAllies());
		this.setPlayerWhitelistEnabled(turretData.isPlayerWhitelistEnabled());
		this.setMobWhitelistEnabled(turretData.isMobWhitelistEnabled());
		this.setCurrentHealth(turretData.getCurrentHealth());
		this.setCurrentLoot(turretData.getCurrentLoot());
		this.setTurretItem(turretItem);
		this.setAmmo(turretData.getAmmo());

		this.save();
	}

	public void unregister() {
		removeTurret(this.id);
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	// -----------------------------------------------------------------
	// Static
	// -----------------------------------------------------------------

	public static boolean isRegistered(final String turretID) {
		for (final UnplacedData turretData : getUnplacedTurrets()) {
			if (turretData.getId().equals(turretID))
				return true;
		}

		return false;
	}

	/**
	 * @return
	 * @see ConfigItems#getItems()
	 */
	public static List<? extends UnplacedData> getUnplacedTurrets() {
		return loadedFiles.getItems();
	}

	/**
	 * @return
	 * @see ConfigItems#getItemNames()
	 */
	public static Set<String> getTurretIDs() {
		return loadedFiles.getItemNames();
	}

	public static UnplacedData createTurret(@NonNull final String turretId, final String type) {
		return loadedFiles.loadOrCreateItem(turretId, () -> new UnplacedData(turretId, type));
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
	public static UnplacedData findById(@NonNull final String id) {
		return loadedFiles.findItem(id);
	}
}
