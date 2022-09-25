package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.TurretSettings;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

	public void register(final Player player, final Block block, final String type) {
		final TurretData turretData = new TurretData();
		final String uniqueID = UUID.randomUUID().toString().substring(0, 4);

		Valid.checkBoolean(!this.registeredTurrets.contains(turretData), block + " has already been registered");

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setOwner(player.getUniqueId());
		turretData.setId(uniqueID);
		turretData.setCurrentLevel(1);

		final TurretSettings turretSettings = TurretSettings.findTurretSettings(type);

		turretData.setMobBlacklist(turretSettings.getMobBlacklist());
		turretData.setPlayerBlacklist(turretSettings.getPlayerBlacklist());
		turretData.addPlayerToBlacklist(player.getUniqueId());

		for (final TurretSettings.LevelData levelData : turretSettings.getLevels()) {
			final TurretData.TurretLevel level = turretData.addLevel();
			levelData.setLevelData(level);
		}

		turretData.setCurrentHealth(turretData.getLevel(1).getMaxHealth());
		this.registeredTurrets.add(turretData);

		this.save();
	}

	public void unregister(final Block block, final String type) {
		final TurretData turretData = getTurretByBlock(block);
		block.getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
		this.registeredTurrets.remove(turretData);

		this.save();
	}

	public boolean isRegistered(final Block block) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return true;
		}

		return false;
	}

	public Set<TurretData> getTurretsOfType(final String turretType) {
		final Set<TurretData> dataList = new HashSet<>();

		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getType().equals(turretType))
				dataList.add(turretData);

		return dataList;
	}

	public void addPlayerToBlacklist(final TurretData turretData, final UUID uuid) {
		turretData.addPlayerToBlacklist(uuid);

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

	public void setCurrentTurretLevel(final TurretData turretData, final int level) {
		turretData.setCurrentLevel(level);

		this.save();
	}

	public void setTurretHealth(final Block block, final double health) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				turretData.setCurrentHealth(health);

		this.save();
	}

	public void setTurretHealth(final TurretData turretData, final double health) {
		turretData.setCurrentHealth(health);

		this.save();
	}

	public void setBroken(final Block block, final boolean destroyed) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation())) {
				turretData.setBroken(destroyed);

				this.save();
			}
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

	public void setTurretLootChances(final TurretData turretData, final int level,
									 final List<Tuple<ItemStack, Double>> lootChances) {
		turretData.getLevel(level).setLootChances(lootChances);

		this.save();
	}

	public void setCurrentLoot(final TurretData turretData, final List<ItemStack> items) {
		turretData.setCurrentLoot(items);

		this.save();
	}

	public TurretData getTurretByBlock(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return turretData;

		return null;
	}

	public List<Location> getTurretLocationsOfType(final String type) {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : getTurretsOfType(type))
			locations.add(turretData.getLocation());

		return locations;
	}

	public boolean isTurretOfType(final Block block, final String type) {
		for (final TurretData turretData : getTurretsOfType(type))
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				return true;

		return false;
	}
}
