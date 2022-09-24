package games.coob.laserturrets.tools;

import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.util.StringUtil;
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

/**
 * Handles tools that click within an arena.
 */
public abstract class TurretTool extends VisualTool {

	private final String turretType;

	private final boolean oneUse;

	/**
	 * The actual item stored here for maximum performance
	 */
	private ItemStack item;

	protected TurretTool(final String turretType, final boolean oneUse) {
		this.turretType = turretType;
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
					"&a" + StringUtil.capitalize(this.turretType) + " Turret Tool",
					"&7Click blocks to",
					"&7un/register a turret.",
					this.oneUse ? "&61 use" : "").glow(true).make();

		return item;
	}

	@Override
	protected void handleBlockClick(final Player player, final ClickType click, final Block block) {
		if (!block.getType().isSolid()) {
			Messenger.error(player, "Turrets can only be created on solid blocks.");
			return;
		}

		if (block.hasMetadata("IsCreating"))
			return;

		final boolean oneUse = this.item.getItemMeta().getLore().toString().contains("1 use");
		final String type = this.turretType;
		final TurretRegistry registry = TurretRegistry.getInstance();
		final boolean isTurret = registry.isTurretOfType(block, type);

		if (isTurret && !oneUse) {
			registry.unregister(block, type);
			Messenger.success(player, "Successfully &cunregistered &7the " + type + " turret at " + Common.shortLocation(block.getLocation()) + ".");
		} else if (!isTurret) {
			if (oneUse)
				player.getInventory().removeItem(this.item);

			block.setMetadata("IsCreating", new FixedMetadataValue(SimplePlugin.getInstance(), ""));
			Sequence.TURRET_CREATION(player, block, type).start(block.getLocation());
			Messenger.success(player, "Successfully &aregistered &7the " + type + " turret at " + Common.shortLocation(block.getLocation()) + ".");
		}
	}

	@Override
	protected List<Location> getVisualizedPoints(final Player player) {
		if (!this.item.getItemMeta().getLore().toString().contains("1 use"))
			return TurretRegistry.getInstance().getTurretLocationsOfType(this.turretType);

		return new ArrayList<>();
	}

	@Override
	protected String getBlockName(final Block block, final Player player) {
		if (!this.item.getItemMeta().getLore().toString().contains("1 use"))
			return "&aRegistered " + StringUtil.capitalize(this.turretType) + " Turret";

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

		return CompMaterial.STICK;
	}
}
