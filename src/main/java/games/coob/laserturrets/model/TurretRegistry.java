/*package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.settings.TurretType;
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
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.ConfigItems;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
import java.util.*;

public class TurretData extends YamlConfig {



import org.kingdoms.constants.land.turrets.TurretData;

private static final String FOLDER = "turrets";

	private static final ConfigItems<TurretData> loadedFiles = ConfigItems.fromFolder(FOLDER, TurretData.class);

	@Getter
	private TurretData turret;

	protected TurretData(final String type, final String id) {
		this.loadConfiguration(NO_DEFAULT, FOLDER + "/" + type.toLowerCase() + "-" + id + ".yml");
	}

	public void register(final Player player, final Block block, final String type) {
		final String uniqueID = UUID.randomUUID().toString().substring(0, 6);
		final TurretData turretData = new TurretData();

		Valid.checkBoolean(!loadedFiles.isItemLoaded(uniqueID), Lang.of("Tool.Already_Registered", "{location}", Common.shortLocation(block.getLocation())));

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setOwner(player.getUniqueId());
		turretData.setId(uniqueID);
		turretData.setCurrentLevel(1);

		final TurretSettings turretSettings = TurretSettings.findByName(type);

		turretData.setMobBlacklist(turretSettings.getMobList());
		turretData.setPlayerBlacklist(turretSettings.getPlayerList());
		turretData.setPlayerWhitelistEnabled(turretSettings.isEnablePlayerWhitelist());
		turretData.setMobWhitelistEnabled(turretSettings.isEnableMobWhitelist());

		if (!turretData.isPlayerWhitelistEnabled())
			turretData.addPlayerToBlacklist(player.getUniqueId());

		for (final TurretSettings.LevelData levelData : turretSettings.getLevels()) {
			final TurretData.TurretLevel level = turretData.addLevel();
			levelData.setLevelData(level);
		}

		turretData.setCurrentHealth(turretData.getLevel(1).getMaxHealth());
		turretData.setHologram(createHologram(turretData));
		this.turret = turretData;

		this.save();
	}

	public void registerUnplacedTurret(final Block block) {
		final TurretData turretData = this.turret;

		if (turretData.getUnplacedTurret() != null) {

			turretData.setUnplacedTurret(null);
			turretData.setLocation(block.getLocation());
			turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
			turretData.setHologram(createHologram(turretData));
		}

		this.save();
	}

	public void registerTurretById(final Block block, final String turretId) { // TODO create unplaced boolean value
		final TurretData turretData = getUnplacedTurretById(turretId);

		this.registeredUnplacedTurrets.removeIf(tuple -> tuple.getKey().equals(turretData));

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setHologram(createHologram(turretData));

		this.registeredTurrets.add(turretData);
		this.save();
	}

import org.kingdoms.constants.land.turrets.TurretData;

private Hologram createHologram(final TurretData turretData) {
		final List<String> lore = Lang.ofList("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
		final List<String> loreList = new ArrayList<>(lore);

		if (!Settings.TurretSection.ENABLE_DAMAGEABLE_TURRETS)
			loreList.removeIf(line -> line.contains(String.valueOf(turretData.getCurrentHealth())));

		final Object[] objects = loreList.toArray();
		final String[] lines = Arrays.copyOf(objects, objects.length, String[].class);
		final int linesLength = objects.length;
		final Hologram hologram = new Hologram(turretData.getLocation().clone().add(0.5, TurretUtil.getYForLines(linesLength), 0.5));

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
	}

import org.kingdoms.constants.land.turrets.TurretData;

public void unregister(final String turretID) {
		final TurretData turretData = getTurretByID(turretID);

		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

		turretData.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		this.registeredTurrets.remove(turretData);

		this.save();
	}

import org.kingdoms.constants.land.turrets.TurretData;

public void setUnplacedTurret(@Nullable final ItemStack turretItem) {
		this.turret.setUnplacedTurret(turretItem);

		this.save();
	}

	public void addPlayerToBlacklist(final TurretData turretData, final UUID uuid) {
		this.turret.addPlayerToBlacklist(uuid);

		this.save();
	}

	public void removePlayerFromBlacklist(final TurretData turretData, final UUID uuid) {
		turretData.removePlayerFromBlacklist(uuid);

		this.save();
	}

	public void addMobToBlacklist(final TurretData turretData, final EntityType entityType) {
		turretData.addMobToBlacklist(entityType);

		this.save();
	}

	public void removeMobFromBlacklist(final TurretData turretData, final EntityType entityType) {
		turretData.removeMobFromBlacklist(entityType);

		this.save();
	}

	public void enableMobWhitelist(final TurretData turretData, final boolean enableWhitelist) {
		turretData.setMobWhitelistEnabled(enableWhitelist);

		this.save();
	}

	public void enablePlayerWhitelist(final TurretData turretData, final boolean enableWhitelist) {
		turretData.setPlayerWhitelistEnabled(enableWhitelist);

		this.save();
	}

	public void setRange(final TurretData turretData, final int level, final int range) {
		turretData.getLevel(level).setRange(range);

		this.save();
	}

	public void setLaserEnabled(final TurretData turretData, final int level, final boolean laserEnabled) {
		turretData.getLevel(level).setLaserEnabled(laserEnabled);

		this.save();
	}

	public void setLaserDamage(final TurretData turretData, final int level, final double damage) {
		turretData.getLevel(level).setLaserDamage(damage);

		this.save();
	}

	public void updateHologram(final TurretData turretData) {
		turretData.getHologram().remove();
		turretData.setHologram(createHologram(turretData));

		this.save();
	}

	public void setLevelPrice(final TurretData turretData, final int level, final double price) {
		turretData.getLevel(level).setPrice(price);

		this.save();
	}

	public void createLevel(final TurretData turretData) {
		turretData.createLevel(turretData.getType());

		this.save();
	}

	public void removeLevel(final TurretData turretData, final int level) {
		turretData.removeLevel(level);

		this.save();
	}

	public void setCurrentTurretLevel(final TurretData turretData, final int level) {
		turretData.setCurrentLevel(level);

		this.save();
	}

	public void setTurretHealth(final Block block, final double health) {
		if (isRegistered(block))
			this.turret.setCurrentHealth(health);

		this.save();
	}

	public void setTurretHealth(final TurretData turretData, final double health) {
		turretData.setCurrentHealth(health);

		this.save();
	}

	public void setBrokenAndFill(final Block block, final boolean destroyed) {
		if (isRegistered(block)) {
			this.turret.setBrokenAndFill(destroyed);

			this.save();
		}
	}

	public void setBroken(final TurretData turretData, final boolean destroyed) {
		turretData.setBroken(destroyed);

		if (!turretData.isBroken())
			setTurretHealth(turretData, turretData.getLevel(turretData.getCurrentLevel()).getMaxHealth());

		this.save();
	}

	public List<Tuple<ItemStack, Double>> getTurretLootChances(final TurretData turretData, final int level) {
		return turretData.getLevel(level).getLootChances();
	}

	public void setTurretLootChances(final TurretData turretData, final int level, final List<Tuple<ItemStack, Double>> lootChances) {
		turretData.getLevel(level).setLootChances(lootChances);

		this.save();
	}

	public void setCurrentLoot(final TurretData turretData, @Nullable final List<ItemStack> items) {
		turretData.setCurrentLoot(items);

		this.save();
	}

	public void removeCurrentLoot(final TurretData turretData, final ItemStack item) {
		turretData.removeCurrentLoot(item);

		this.save();
	}

	@Override
	protected boolean saveComments() {
		return false;
	}

	@Override
	protected void onLoad() {
		this.turret = this.get("Registered_Turret", TurretData.class);
	}

	@Override
	protected void onSave() {
		this.set("Registered_Turret", this.turret);
	}

	// -----------------------------------------------------------------
	// Static TODO check how to use these methods
	// -----------------------------------------------------------------

	public static void syncTurretDataWithSettings(final TurretSettings settings, final TurretData turretData) {
		final TurretData turretData = TurretData.findByName(turretData.getId());
		turretData.setMobBlacklist(settings.getMobList());
		turretData.setPlayerBlacklist(settings.getPlayerList());
		turretData.setPlayerWhitelistEnabled(settings.isEnablePlayerWhitelist());
		turretData.setMobWhitelistEnabled(settings.isEnableMobWhitelist());

		if (!turretData.isPlayerWhitelistEnabled())
			turretData.addPlayerToBlacklist(turretData.getOwner());

		for (int i = 1; i <= turretData.getLevels() + 2; i++)
			turretData.removeLevel(1);

		for (final TurretSettings.LevelData levelData : settings.getLevels()) {
			final TurretData.TurretLevel level = turretData.addLevel();
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
			return turretData.turret.getLocation().getBlock().getLocation().equals(block.getLocation());

		return false;
	}

	public static TurretData getTurretByBlock(final Block block) {
		for (final TurretData turretData : getTurrets())
			if (turretData.turret.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return turretData.turret;

		return null;
	}

	public static Set<TurretData> getTurretsOfType(final String turretType) {
		final Set<TurretData> dataList = new HashSet<>();

		for (final TurretData turretData : getTurrets())
			if (turretData.turret.getType().equals(turretType))
				dataList.add(turretData.turret);

		return dataList;
	}

	public static Set<TurretData> getRegisteredTurrets() {
		final Set<TurretData> dataList = new HashSet<>();

		for (final TurretData turretData : getTurrets())
			dataList.add(turretData.turret);

		return dataList;
	}

	public static List<Location> getTurretLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			locations.add(turretData.turret.getLocation());

		return locations;
	}

	public static List<TurretData> getUnplacedTurrets() {
		final List<TurretData> turretData = new ArrayList<>();

		for (final TurretData turretData : getTurrets())
			if (turretData.turret.getUnplacedTurret() != null)
				turretData.add(turretData.turret);

		return turretData;
	}

	public static Set<String> getTurretIDs() {
		final Set<String> turretIDs = new HashSet<>();

		for (final TurretData turretData : getTurrets())
			turretIDs.add(turretData.turret.getId());

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



import org.kingdoms.constants.land.turrets.TurretData;

public static List<? extends TurretData> getTurrets() {
		return loadedFiles.getItems();
	}


	public static Set<String> getTurretNames() {
		return loadedFiles.getItemNames();
	}

	public static void createTurret(@NonNull final String name, @NonNull final TurretType type) {
		//loadedFiles.loadOrCreateItem(name, () -> type.instantiate(name));
	}

	public static void createSettings(@NonNull final String turretType, final String string) {
		//loadedFiles.loadOrCreateItem(turretType, () -> new TurretData(string, turretType));
	}

	// 1) /game new bedwars Arena1
	// 2) when we load a disk file


	public static void loadTurrets() {
		loadedFiles.loadItems();
	}

	public static void removeTurret(final TurretData turretData) {
		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

		turretData.getLocation().getBlock().getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		loadedFiles.removeItemByName(turretData.getId());
	}


	public static boolean isTurretLoaded(final String id) {
		return loadedFiles.isItemLoaded(id);
	}


	public static TurretData findByName(@NonNull final String id) {
		return loadedFiles.findItem(id);
	}
}*/
