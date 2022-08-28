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

	@Getter
	private final Set<TurretData> arrowTurrets = new HashSet<>();

	@Getter
	private final Set<TurretData> fireballTurrets = new HashSet<>();

	@Getter
	private final Set<TurretData> beamTurrets = new HashSet<>();

	private TurretRegistry() {
		this.loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoad() {
		this.registeredTurrets = this.getSet("Turrets", TurretData.class);

		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getType().equalsIgnoreCase("arrow"))
				this.arrowTurrets.add(turretData);
			if (turretData.getType().equalsIgnoreCase("fireball"))
				this.fireballTurrets.add(turretData);
			if (turretData.getType().equalsIgnoreCase("beam"))
				this.beamTurrets.add(turretData);
		}
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

		switch (type) {
			case "arrow":
				this.arrowTurrets.add(turretData);
				break;
			case "fireball":
				this.fireballTurrets.add(turretData);
				break;
			case "beam":
				this.beamTurrets.add(turretData);
				break;
		}

		this.save();
	}

	public void unregister(final Block block, final String type) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().equals(block.getLocation())) {
				block.getRelative(BlockFace.UP).setType(CompMaterial.AIR.getMaterial());
				this.registeredTurrets.remove(turretData);

				switch (type) {
					case "arrow":
						this.arrowTurrets.remove(turretData);
						break;
					case "fireball":
						this.fireballTurrets.remove(turretData);
						break;
					case "beam":
						this.beamTurrets.remove(turretData);
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

	public void setCurrentTurretLevel(final TurretData turretData, final int level) {
		turretData.setCurrentLevel(level);

		this.save();
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

	public void setTurretHealth(final Block block, final double health) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.setCurrentHealth(health);

		this.save();
	}

	public void setTurretHealth(final TurretData turretData, final double health) {
		turretData.setCurrentHealth(health);

		this.save();
	}

	public void setBroken(final Block block, final boolean destroyed) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.setBroken(destroyed);

		this.save();
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

	public void setCurrentLoot(final TurretData turretData, final List<ItemStack> items) {
		turretData.setCurrentLoot(items);

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

	public List<Location> getTurretLocationsOfType(final String type) {
		final List<Location> locations = new ArrayList<>();

		switch (type) {
			case "arrow":
				for (final TurretData turretData : this.arrowTurrets)
					locations.add(turretData.getLocation());
				break;
			case "fireball":
				for (final TurretData turretData : this.fireballTurrets)
					locations.add(turretData.getLocation());
				break;
			case "beam":
				for (final TurretData turretData : this.beamTurrets)
					locations.add(turretData.getLocation());
				break;
		}

		return locations;
	}

	public boolean isTurretOfType(final Block block, final String type) {
		switch (type) {
			case "arrow":
				for (final TurretData turretData : this.arrowTurrets)
					return turretData.getLocation().equals(block.getLocation());
				break;
			case "fireball":
				for (final TurretData turretData : this.fireballTurrets)
					return turretData.getLocation().equals(block.getLocation());
				break;
			case "beam":
				for (final TurretData turretData : this.beamTurrets)
					return turretData.getLocation().equals(block.getLocation());
				break;
		}

		return false;
	}
}
