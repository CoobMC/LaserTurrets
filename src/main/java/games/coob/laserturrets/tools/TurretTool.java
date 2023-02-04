package games.coob.laserturrets.tools;

import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.visual.VisualTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TurretTool extends VisualTool {

	private final String turretType;

	private final String displayName;

	private final boolean oneUse;

	/**
	 * The actual item stored here for maximum performance
	 */
	private ItemStack item;

	protected TurretTool(final String turretType, final String displayName, final boolean oneUse) {
		this.turretType = turretType;
		this.displayName = displayName;
		this.oneUse = oneUse;
	}

	/**
	 * @see Tool#getItem()
	 */
	@Override
	public ItemStack getItem() {
		if (item == null)
			item = ItemCreator.of(
							getTurretMaterial(),
							this.oneUse ? Lang.of("Tool.Title_Infinite_Use_Tool", "{turretType}", TurretUtil.capitalizeWord(this.displayName)) : Lang.of("Tool.Title_1_Use_Tool", "{turretType}", TurretUtil.capitalizeWord(this.displayName)),
							this.oneUse ? Lang.ofArray("Tool.Lore_1_Use_Tool") : Lang.ofArray("Tool.Lore_Infinite_Use_Tool"))
					.glow(true).make();

		return item;
	}

	@Override
	protected void handleBlockClick(final Player player, final ClickType click, final Block block) {
		final String type = this.turretType;
		final TurretRegistry registry = TurretRegistry.getInstance();
		final Location location = block.getLocation();
		final Location closestLocation = getClosestLocation(location, registry.getTurretLocations());

		if (registry.getTurretsOfType(type).size() >= TurretSettings.findTurretSettings(type).getTurretLimit() && !registry.isRegistered(block)) {
			Messenger.error(player, Lang.of("Tool.Turret_Limit_Reached", "{turretType}", this.displayName, "{turretLimit}", TurretSettings.findTurretSettings(type).getTurretLimit()));
			return;
		}

		if (!block.getType().isSolid()) {
			Messenger.error(player, Lang.of("Tool.Turret_Cannot_Be_Placed"));
			return;
		}

		if (registry.isRegistered(block) && !Objects.equals(registry.getTurretByBlock(block).getType(), this.turretType)) {
			Messenger.error(player, Lang.of("Tool.Block_Is_Already_Turret"));
			return;
		}

		if (!registry.isRegistered(block) && closestLocation != null && Settings.TurretSection.TURRET_MIN_DISTANCE > closestLocation.distance(location)) {
			Messenger.error(player, Lang.of("Tool.Turret_Min_Distance_Breached", "{distance}", Settings.TurretSection.TURRET_MIN_DISTANCE));
			return;
		}

		if (block.hasMetadata("IsCreating"))
			return;

		final boolean oneUse = this.oneUse;
		final boolean isTurret = registry.isTurretOfType(block, type);

		if (isTurret && !oneUse) {
			registry.unregister(block);
			Messenger.success(player, Lang.of("Tool.Unregistered_Turret_Message", "{turretType}", this.displayName, "{location}", Common.shortLocation(location)));
		} else if (!isTurret) {
			if (oneUse)
				player.getInventory().removeItem(this.item);

			block.setMetadata("IsCreating", new FixedMetadataValue(SimplePlugin.getInstance(), ""));
			Sequence.TURRET_CREATION(player, block, type).start(location);
			Messenger.success(player, Lang.of("Tool.Registered_Turret_Message", "{turretType}", this.displayName, "{location}", Common.shortLocation(location)));
		}
	}

	private Location getClosestLocation(final Location centerLocation, final List<Location> locations) { // TODO add to foundation
		Location closestLocation = null;

		for (final Location location : locations) {
			if (closestLocation == null || (location.distanceSquared(centerLocation) < closestLocation.distanceSquared(centerLocation)))
				closestLocation = location;
		}

		return closestLocation;
	}

	@Override
	protected List<Location> getVisualizedPoints(final Player player) {
		if (!this.oneUse)
			return TurretRegistry.getInstance().getTurretLocationsOfType(this.turretType);

		return new ArrayList<>();
	}

	@Override
	protected String getBlockName(final Block block, final Player player) {
		if (!this.oneUse)
			return Lang.of("Tool.Registered_Turret_Hologram", "{turretType}", TurretUtil.capitalizeWord(this.displayName));

		return null;
	}

	@Override
	protected CompMaterial getBlockMask(final Block block, final Player player) {
		return CompMaterial.EMERALD_BLOCK;
	}

	/**
	 * Cancel the event so that we don't destroy blocks when selecting them
	 *
	 * @see Tool#autoCancel()
	 */
	@Override
	protected boolean autoCancel() {
		return true;
	}

	private CompMaterial getTurretMaterial() {
		switch (this.turretType) {
			case "arrow":
				return CompMaterial.ARROW;
			case "beam":
				return CompMaterial.BLAZE_ROD;
			case "fireball":
				return CompMaterial.FIRE_CHARGE;
		}

		return null;
	}
}
