package games.coob.laserturrets.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TurretData implements ConfigSerializable {

	private Location location;

	private CompMaterial material;

	private String id;

	private String type;

	private List<String> playerBlacklist;

	private List<TurretLevel> turretLevels = new ArrayList<>(10);

	private int currentLevel;

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setMaterial(final CompMaterial material) {
		this.material = material;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setCurrentLevel(final int level) {
		this.currentLevel = level;
	}

	public void addPlayerToBlacklist(final String playerName) {
		this.playerBlacklist.add(playerName);
	}

	public void removePlayerFromBlacklist(final String playerName) {
		this.playerBlacklist.remove(playerName);
	}

	public boolean isPlayerBlacklisted(final String playerName) {
		if (this.playerBlacklist != null)
			return this.playerBlacklist.contains(playerName);
		else return false;
	}

	public void setPlayerBlacklist(final List<String> playerBlacklist) {
		this.playerBlacklist = playerBlacklist;
	}

	public TurretLevel getLevel(final int level) {
		return this.turretLevels.get(level - 1);
	}

	public TurretLevel addLevel() {
		final TurretLevel level = new TurretLevel(this);

		turretLevels.add(level);

		return turretLevels.get(turretLevels.size() - 1);
	}

	public void removeLevel(final int level) {
		Valid.checkBoolean(getLevels() >= level, "Cannot remove level " + level + " because the turret only has " + getLevels() + " levels.");

		turretLevels.remove(level - 1);
	}

	public void setTurretLevels(final List<TurretLevel> levels) {
		this.turretLevels = levels;
	}

	public int getLevels() {
		return turretLevels.size();
	}

	private String toHash(final Location location, final CompMaterial material) {
		return SerializeUtil.serializeLoc(location) + " | " + material;
	}

	@Override
	public SerializedMap serialize() {
		return SerializedMap.ofArray(
				"hash", toHash(this.location, this.material),
				"Id", this.id,
				"Type", this.type,
				"Player_Blacklist", this.playerBlacklist,
				"Levels", this.turretLevels,
				"Current_Level", this.currentLevel);
	}

	public static TurretData deserialize(final SerializedMap map) {
		final String hash = map.getString("hash");
		final String id = map.getString("Id");
		final String type = map.getString("Type");
		final List<String> blacklist = map.getStringList("Player_Blacklist");
		final List<TurretLevel> levels = map.getList("Levels", TurretLevel.class);
		final Integer level = map.getInteger("Current_Level");

		final String[] split = hash.split(" \\| ");
		final Location location = SerializeUtil.deserializeLocation(split[0]);
		final CompMaterial material = CompMaterial.valueOf(split[1]);

		final TurretData turretData = new TurretData();

		turretData.setMaterial(material);
		turretData.setLocation(location);
		turretData.setTurretLevels(levels);
		turretData.setId(id);
		turretData.setType(type);
		turretData.setPlayerBlacklist(blacklist);
		turretData.setCurrentLevel(level);

		return turretData;
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	public final static class TurretLevel implements ConfigSerializable {

		private final TurretData turretData;

		private double price;

		private List<Tuple<ItemStack, Double>> lootChances;

		private int range;

		private boolean laserEnabled;

		private double laserDamage;

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

		public void setLootChances(final List<Tuple<ItemStack, Double>> lootChances) {
			this.lootChances = lootChances;
		}

		@Override
		public SerializedMap serialize() {
			return SerializedMap.ofArray(
					"Price", this.price,
					"Loot_Chances", this.lootChances,
					"Range", this.range,
					"Laser_Enabled", this.laserEnabled,
					"Laser_Damage", this.laserDamage);
		}
	}

	public static TurretLevel deserialize(final SerializedMap map, final TurretData turretData) {
		final double price = map.getDouble("Price");
		final List<Tuple<ItemStack, Double>> lootChances = map.getTupleList("Loot_Chances", ItemStack.class, Double.class);
		final int range = map.getInteger("Range");
		final boolean laserEnabled = map.getBoolean("Laser_Enabled");
		final double laserDamage = map.getDouble("Laser_Damage");

		final TurretLevel level = new TurretLevel(turretData);

		level.setPrice(price);
		level.setLootChances(lootChances);
		level.setRange(range);
		level.setLaserEnabled(laserEnabled);
		level.setLaserDamage(laserDamage);

		return level;
	}
}
