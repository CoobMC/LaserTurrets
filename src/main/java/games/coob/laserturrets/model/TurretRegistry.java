package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.Settings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
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

	private Set<TurretData> registeredTurrets = new HashSet<>();

	@Getter
	private final List<TurretData> arrowTurrets = new ArrayList<>();

	@Getter
	private final List<TurretData> flameTurrets = new ArrayList<>();

	@Getter
	private final List<TurretData> laserTurrets = new ArrayList<>();

	private TurretRegistry() {
		this.loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoad() {
		this.registeredTurrets = this.getSet("Turrets", TurretData.class);
	}

	@Override
	protected void onSave() {
		this.set("Turrets", this.registeredTurrets);
	}

	public void register(final Block block, final String type) { // TODO create default settings for level 1, 2 and 3
		final TurretData turretData = new TurretData();
		//final TurretData.TurretLevel turretLevel = turretData.addLevel();
		final String uniqueID = UUID.randomUUID().toString().substring(0, 4);

		Valid.checkBoolean(!this.registeredTurrets.contains(turretData), block + " has already been registered");

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setId(uniqueID);
		turretData.setCurrentLevel(1);

		int level = 0;

		for (int i = 0; i < 3; i++) {
			level = level + 1;
			turretData.addLevel();

			if (level == 1) {
				turretData.getLevel(level).setRange(Settings.DefaultLevel1TurretSection.TURRET_RANGE);
				turretData.getLevel(level).setLaserEnabled(Settings.DefaultLevel1TurretSection.ENABLE_LASERS);
				turretData.getLevel(level).setLaserDamage(Settings.DefaultLevel1TurretSection.LASER_DAMAGE);
				turretData.getLevel(level).setPrice(Settings.DefaultLevel1TurretSection.PRICE);
			} else if (level == 2) {
				turretData.getLevel(level).setRange(Settings.DefaultLevel2TurretSection.TURRET_RANGE);
				turretData.getLevel(level).setLaserEnabled(Settings.DefaultLevel2TurretSection.ENABLE_LASERS);
				turretData.getLevel(level).setLaserDamage(Settings.DefaultLevel2TurretSection.LASER_DAMAGE);
				turretData.getLevel(level).setPrice(Settings.DefaultLevel2TurretSection.PRICE);
			} else {
				turretData.getLevel(level).setRange(Settings.DefaultLevel3TurretSection.TURRET_RANGE);
				turretData.getLevel(level).setLaserEnabled(Settings.DefaultLevel3TurretSection.ENABLE_LASERS);
				turretData.getLevel(level).setLaserDamage(Settings.DefaultLevel3TurretSection.LASER_DAMAGE);
				turretData.getLevel(level).setPrice(Settings.DefaultLevel3TurretSection.PRICE);
			}
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

	public void addPlayerToBlacklist(final Block block, final String playerName) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.addPlayerToBlacklist(playerName);

		this.save();
	}

	public void removePlayerFromBlacklist(final Block block, final String playerName) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.removePlayerFromBlacklist(playerName);

		this.save();
	}

	public boolean isPlayerBlacklisted(final Block block, final String playerName) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.isPlayerBlacklisted(playerName);

		return false;
	}

	public boolean isLaserEnabled(final Block block, final int level) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel(level).isLaserEnabled();

		return Settings.DefaultLevel1TurretSection.ENABLE_LASERS;
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

	public int getCurrentTurretLevel(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getCurrentLevel();

		return 0;
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

		return Settings.DefaultLevel1TurretSection.TURRET_RANGE;
	}

	public double getLaserDamage(final Block block, final int level) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel(level).getLaserDamage();

		return Settings.DefaultLevel1TurretSection.LASER_DAMAGE;
	}

	public TurretData getTurretByBlock(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData;

		return null;
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

	@RequiredArgsConstructor
	private enum TurretSettingsValueByLevel {
		LEVEL1(1, Settings.DefaultLevel1TurretSection.PRICE, Settings.DefaultLevel1TurretSection.TURRET_RANGE, Settings.DefaultLevel1TurretSection.ENABLE_LASERS, Settings.DefaultLevel1TurretSection.LASER_DAMAGE),
		LEVEL2(2, Settings.DefaultLevel2TurretSection.PRICE, Settings.DefaultLevel2TurretSection.TURRET_RANGE, Settings.DefaultLevel2TurretSection.ENABLE_LASERS, Settings.DefaultLevel2TurretSection.LASER_DAMAGE),
		LEVEL3(3, Settings.DefaultLevel3TurretSection.PRICE, Settings.DefaultLevel3TurretSection.TURRET_RANGE, Settings.DefaultLevel3TurretSection.ENABLE_LASERS, Settings.DefaultLevel3TurretSection.LASER_DAMAGE);

		private final double level;
		private final double price;
		private final int range;
		private final boolean laserEnabled;
		private final double laserDamage;
	}
}
