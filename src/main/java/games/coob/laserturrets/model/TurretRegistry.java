package games.coob.laserturrets.model;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.settings.Settings;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
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

	public void register(final Block block, final String type) { // TODO create 2 more levels on register
		final TurretData turretData = new TurretData();
		final String uniqueID = UUID.randomUUID().toString().substring(0, 4);

		Valid.checkBoolean(!this.registeredTurrets.contains(turretData), block + " has already been registered");

		turretData.setLevel(1);
		turretData.setLocation(block.getLocation());
		turretData.setMaterial(CompMaterial.fromMaterial(block.getType()));
		turretData.setType(type);
		turretData.setId(uniqueID);
		turretData.setRange(Settings.DefaultTurretSection.TURRET_RANGE);
		turretData.setLaserEnabled(Settings.DefaultTurretSection.ENABLE_LASERS);
		turretData.setLaserDamage(Settings.DefaultTurretSection.LASER_DAMAGE);

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

		this.registeredTurrets.add(turretData);
		this.save();
	}

	public void unregister(final Block block, final String type) {
		// synchronized (registeredBlocks) { // synchronized is used for anyscronous processing (Common.runLaterAsync)
		this.registeredTurrets.removeIf(turretData -> turretData.getLocation().getBlock().equals(block) && turretData.getType().equals(type));

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

	public boolean isLaserEnabled(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.isLaserEnabled();

		return Settings.DefaultTurretSection.ENABLE_LASERS;
	}

	public void setRange(final TurretData turretData, final int range) {
		turretData.setRange(range);

		this.save();
	}

	public void setLaserEnabled(final TurretData turretData, final boolean laserEnabled) {
		turretData.setLaserEnabled(laserEnabled);

		this.save();
	}

	public void setLaserDamage(final TurretData turretData, final double damage) {
		turretData.setLaserDamage(damage);

		this.save();
	}

	public void increaseLevel(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				turretData.increaseLevel();

		this.save();
	}

	public int getLevel(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLevel();

		return 0;
	}

	public String getType(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getType();

		return null;
	}

	public int getTurretRange(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getRange();

		return Settings.DefaultTurretSection.TURRET_RANGE;
	}

	public double getLaserDamage(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData.getLaserDamage();

		return Settings.DefaultTurretSection.LASER_DAMAGE;
	}

	public TurretData getTurretByBlock(final Block block) {
		for (final TurretData turretData : this.registeredTurrets)
			if (turretData.getLocation().equals(block.getLocation()))
				return turretData;

		return null;
	}

	public List<Tuple<ItemStack, Double>> getTurretLootChances(final Player player) {
		final PlayerCache cache = PlayerCache.from(player);

		return cache.getSelectedTurret().getLootChances();
	}

	public void setTurretLootChances(final TurretData turretData, final List<Tuple<ItemStack, Double>> lootChances) {
		turretData.setLootChances(lootChances);

		this.save();
	}

	public List<Location> getLocations() {
		final List<Location> locations = new ArrayList<>();

		for (final TurretData turretData : this.registeredTurrets)
			locations.add(turretData.getLocation());

		return locations;
	}
}
