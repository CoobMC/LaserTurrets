package games.coob.laserturrets.model;

import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlConfig;

import javax.annotation.Nullable;
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

		Valid.checkBoolean(!this.registeredTurrets.contains(turretData), Lang.of("Tool.Already_Registered", "{location}", Common.shortLocation(block.getLocation())));

		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setOwner(player.getUniqueId());
		turretData.setId(uniqueID);
		turretData.setCurrentLevel(1);

		final TurretSettings turretSettings = TurretSettings.findTurretSettings(type);

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
		this.registeredTurrets.add(turretData);

		this.save();
	}

	private Hologram createHologram(final TurretData turretData) {
		final String[] lore = Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
		final int lines = lore.length;
		final Hologram hologram = new Hologram(turretData.getLocation().clone().add(0.5, getYForLines(lines), 0.5));

		hologram.setLore(lore);

		return hologram;
	}

	private double getYForLines(final int numberOfLines) {
		return numberOfLines / 6.0;
	}

	public void unregister(final Block block) {
		final TurretData turretData = getTurretByBlock(block);

		if (turretData.getHologram() != null)
			turretData.getHologram().remove();

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
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation()))
				turretData.setCurrentHealth(health);

		this.save();
	}

	public void setTurretHealth(final TurretData turretData, final double health) {
		turretData.setCurrentHealth(health);

		this.save();
	}

	public void setBrokenAndFill(final Block block, final boolean destroyed) {
		for (final TurretData turretData : this.registeredTurrets) {
			if (turretData.getLocation().getBlock().getLocation().equals(block.getLocation())) {
				turretData.setBrokenAndFill(destroyed);

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

	public void setCurrentLoot(final TurretData turretData, @Nullable final List<ItemStack> items) {
		turretData.setCurrentLoot(items);

		this.save();
	}

	public void removeCurrentLoot(final TurretData turretData, final ItemStack item) {
		turretData.removeCurrentLoot(item);

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
