package games.coob.laserturrets.tools;

import games.coob.laserturrets.hook.HookSystem;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
		final TurretSettings settings = TurretSettings.findByName(this.turretType);

		if (settings != null)
			item = ItemCreator.of(settings.getToolItem())
					.name(this.oneUse ? Lang.of("Tool.Title_Infinite_Use_Tool", "{turretType}", TurretUtil.capitalizeWord(this.displayName)) : Lang.of("Tool.Title_1_Use_Tool", "{turretType}", TurretUtil.capitalizeWord(this.displayName)))
					.lore(this.oneUse ? Lang.ofArray("Tool.Lore_1_Use_Tool") : Lang.ofArray("Tool.Lore_Infinite_Use_Tool"))
					.glow(true).make();

		return item;
	}

	@Override
	protected void handleBlockClick(final Player player, final ClickType click, final Block block) {
		final String type = this.turretType;
		final Location location = block.getLocation();
		final Location closestLocation = getClosestLocation(location, TurretData.getTurretLocations());
		final TurretSettings settings = TurretSettings.findByName(type);
		final Block blockUp = block.getRelative(BlockFace.UP);

		if (block.getType().isInteractable())
			return;

		if (Settings.TurretSection.BUILD_IN_OWN_TERRITORY && !HookSystem.canBuild(location, player) && !TurretData.isRegistered(block)) {
			Messenger.error(player, "You cannot place turrets in this region.");
			return;
		}

		if (TurretData.getTurretsOfType(type).size() >= settings.getTurretLimit() && !TurretData.isRegistered(block)) {
			Messenger.error(player, Lang.of("Tool.Turret_Limit_Reached", "{turretType}", this.displayName, "{turretLimit}", settings.getTurretLimit()));
			return;
		}

		if (!block.getType().isSolid() || (!CompMaterial.isAir(blockUp) && !TurretData.isRegistered(block))) {
			Messenger.error(player, Lang.of("Tool.Turret_Cannot_Be_Placed"));
			return;
		}

		if (TurretData.isRegistered(block) && !Objects.equals(TurretData.findByBlock(block).getType(), this.turretType)) {
			Messenger.error(player, Lang.of("Tool.Block_Is_Already_Turret"));
			return;
		}

		if (closestLocation != null && !TurretData.isRegistered(block) && Settings.TurretSection.TURRET_MIN_DISTANCE > closestLocation.distance(location)) {
			Messenger.error(player, Lang.of("Tool.Turret_Min_Distance_Breached", "{distance}", Settings.TurretSection.TURRET_MIN_DISTANCE));
			return;
		}

		if (block.hasMetadata("IsCreating") || player.hasMetadata("CreatingTurret")) {
			Messenger.error(player, Lang.of("Tool.Wait_Before_Place"));
			return;
		}

		final boolean oneUse = this.oneUse;
		final boolean isTurret = TurretData.isTurretOfType(block, type);

		if (isTurret && !oneUse) {
			final TurretData turretData = TurretData.findByBlock(block);
			turretData.unregister();
			Messenger.success(player, Lang.of("Tool.Unregistered_Turret_Message", "{turretType}", this.displayName, "{location}", Common.shortLocation(location)));
		} else if (!isTurret) {
			if (oneUse)
				player.getInventory().removeItem(this.item);

			player.setMetadata("CreatingTurret", new FixedMetadataValue(SimplePlugin.getInstance(), ""));
			block.setMetadata("IsCreating", new FixedMetadataValue(SimplePlugin.getInstance(), ""));
			Sequence.TURRET_CREATION(player, block, type).start(location);
			Messenger.success(player, Lang.of("Tool.Registered_Turret_Message", "{turretType}", this.displayName, "{location}", Common.shortLocation(location)));
		}
	}

	private Location getClosestLocation(final Location centerLocation, final List<Location> locations) {
		Location closestLocation = null;

		for (final Location location : locations) {
			if (!location.getWorld().equals(centerLocation.getWorld()))
				continue;

			if (closestLocation == null || (location.distanceSquared(centerLocation) < closestLocation.distanceSquared(centerLocation)))
				closestLocation = location;
		}

		return closestLocation;
	}

	@Override
	protected List<Location> getVisualizedPoints(final Player player) {
		if (!this.oneUse)
			return TurretData.getTurretLocationsOfType(this.turretType);

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
}
