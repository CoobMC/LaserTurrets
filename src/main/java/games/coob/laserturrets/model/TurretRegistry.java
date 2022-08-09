package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.TurretSettings;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.YamlConfig;

import java.util.*;

public class TurretRegistry extends YamlConfig {

	@Getter
	private static final TurretRegistry instance = new TurretRegistry();

	@Getter
	private Set<TurretData> registeredTurrets = new HashSet<>();

	@Getter
	private final Set<TurretData> arrowTurrets = new HashSet<>();

	@Getter
	private final Set<TurretData> flameTurrets = new HashSet<>();

	@Getter
	private final Set<TurretData> laserTurrets = new HashSet<>();

	private TurretRegistry() {
		this.loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoad() {
		this.registeredTurrets = this.getSet("Turrets", TurretData.class);

		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getType().equalsIgnoreCase("arrow"))
				this.arrowTurrets.add(turretData);
			if (turretData.getType().equalsIgnoreCase("flame"))
				this.flameTurrets.add(turretData);
			if (turretData.getType().equalsIgnoreCase("laser"))
				this.laserTurrets.add(turretData);
		}
	}

	@Override
	protected void onSave() {
		this.set("Turrets", this.registeredTurrets);
	}

	public void register(final Block block, final String type) {
		final TurretData turretData = new TurretData();
		final String uniqueID = UUID.randomUUID().toString().substring(0, 4);

		Valid.checkBoolean(!this.registeredTurrets.contains(turretData), block + " has already been registered");

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setId(uniqueID);
		turretData.setCurrentLevel(1);

		final TurretSettings turretSettings = TurretSettings.findTurretSettings(type + "-turrets");

		turretData.setMobBlacklist(turretSettings.getMobBlacklist());
		turretData.setPlayerBlacklist(turretSettings.getPlayerBlacklist());

		for (final TurretSettings.LevelData levelData : turretSettings.getLevels()) {
			turretData.addLevel();
			levelData.setLevelData(turretData.getLevel(levelData.getLevel()));
		}

		this.registeredTurrets.add(turretData);

		switch (type) {
			case "arrow":
				this.arrowTurrets.add(turretData);
				break;
			case "flame":
				this.flameTurrets.add(turretData);
				break;
			case "laser":
				this.laserTurrets.add(turretData);
				break;
		}

		this.save();
	}

	public void unregister(final Block block, final String type) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().equals(block.getLocation())) {
				// synchronized (registeredBlocks) { // synchronized is used for anyscronous processing (Common.runLaterAsync)
				this.registeredTurrets.remove(turretData);

				switch (type) {
					case "arrow":
						this.arrowTurrets.remove(turretData);
						break;
					case "flame":
						this.flameTurrets.remove(turretData);
						break;
					case "laser":
						this.laserTurrets.remove(turretData);
						break;
				}
			}
		}

		this.save();
	}

	public boolean isRegistered(final Block block) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().equals(block.getLocation()))
				return true;
		}

		return false;
	}

	public boolean isRegisteredExclude(final Block block, final String type) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().equals(block.getLocation()) && !turretData.getType().equals(type))
				return true;
		}

		return false;
	}

	public void addPlayerToBlacklist(final Block block, final UUID uuid) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.addPlayerToBlacklist(uuid);

		this.save();
	}

	public void addPlayerToBlacklist(final TurretData turretData, final UUID uuid) {
		turretData.addPlayerToBlacklist(uuid);

		this.save();
	}

	public void removePlayerFromBlacklist(final Block block, final UUID uuid) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.removePlayerFromBlacklist(uuid);

		this.save();
	}

	public void removePlayerFromBlacklist(final TurretData turretData, final UUID uuid) {
		turretData.removePlayerFromBlacklist(uuid);

		this.save();
	}

	public boolean isPlayerBlacklisted(final Block block, final UUID uuid) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.isPlayerBlacklisted(uuid);

		return false;
	}


	public void addMobToBlacklist(final TurretData turretData, final EntityType entityType) {
		turretData.addMobToBlacklist(entityType);

		this.save();
	}

	public void removeMobFromBlacklist(final TurretData turretData, final EntityType entityType) {
		turretData.removeMobFromBlacklist(entityType);

		this.save();
	}

	public boolean isMobBlacklisted(final Block block, final EntityType entityType) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.isMobBlacklisted(entityType);

		return false;
	}

	public boolean isLaserEnabled(final Block block, final int level) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel(level).isLaserEnabled();

		return false;
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

	public void setLevelPrice(final TurretData turretData, final int level, final double price) {
		turretData.getLevel(level).setPrice(price);

		this.save();
	}

	public void createLevel(final TurretData turretData) {
		turretData.createLevel(turretData.getType());

		this.save();
	}

	public int getCurrentTurretLevel(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getCurrentLevel();

		return 0;
	}

	public void setCurrentTurretLevel(final TurretData turretData, final int level) {
		turretData.setCurrentLevel(level);

		save();
	}

	public String getType(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getType();

		return null;
	}

	public int getTurretRange(final Block block, final int level) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel(level).getRange();

		return 15;
	}

	public double getLaserDamage(final Block block, final int level) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel(level).getLaserDamage();

		return 0.5;
	}

	public List<Tuple<ItemStack, Double>> getTurretLootChances(final TurretData turretData, final int level) {
		return turretData.getLevel(level).getLootChances();
	}

	public void setTurretLootChances(final TurretData turretData, final int level, final List<Tuple<ItemStack, Double>> lootChances) {
		turretData.getLevel(level).setLootChances(lootChances);

		this.save();
	}

	public List<Location> getLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : this.registeredTurrets)
			locations.add(turretData.getLocation());

		return locations;
	}

	public TurretData getTurretByBlock(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData;

		return null;
	}

	public TurretData getTurretByLocation(final Location location) {
		for (final TurretData turretData : getRegisteredTurrets())
			if (turretData.getLocation().equals(location))
				return turretData;

		return null;
	}

	public List<Location> getArrowLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : this.arrowTurrets)
			locations.add(turretData.getLocation());

		return locations;
	}

	public List<Location> getFlameLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : this.flameTurrets)
			locations.add(turretData.getLocation());

		return locations;
	}

	public List<Location> getLaserLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : this.laserTurrets)
			locations.add(turretData.getLocation());

		return locations;
	}

	public boolean isArrowTurret(final Block block) {
		for (final TurretData turretData : this.arrowTurrets)
			return turretData.getLocation().equals(block.getLocation());
		return false;
	}

	public boolean isFlameTurret(final Block block) {
		for (final TurretData turretData : this.flameTurrets)
			return turretData.getLocation().equals(block.getLocation());
		return false;
	}

	public boolean isLaserTurret(final Block block) {
		for (final TurretData turretData : this.laserTurrets)
			return turretData.getLocation().equals(block.getLocation());
		return false;
	}
}
