package games.coob.laserturrets.tools;

import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.util.StringUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.visual.VisualTool;

import java.util.List;

/**
 * Handles tools that click within an arena.
 */
public abstract class TurretTool extends VisualTool {

	@Getter
	private final String turretType;

	/**
	 * The actual item stored here for maximum performance
	 */
	private ItemStack item;

	/**
	 * Create a tool that may be used for any arena
	 */
	protected TurretTool(final String turretType) {
		this.turretType = turretType;
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
							"",
							"&7Click blocks to",
							"&7un/register a turret.").glow(true)
					.make();

		return item;
	}

	@Override
	protected void handleBlockClick(final Player player, final ClickType click, final Block block) {
		final String type = this.turretType;

		final TurretRegistry registry = TurretRegistry.getInstance();
		final boolean isRegisteredExclude = registry.isRegisteredExclude(block, type);
		final boolean isTurret = registry.isTurretOfType(block, type);

		if (!isRegisteredExclude) {
			if (isTurret && !CompMetadata.hasMetadata(this.item, "Destroy")) {
				registry.unregister(block, type);
				Messenger.success(player, "Successfully &cunregistered &7the " + type + " turret at " + Common.shortLocation(block.getLocation()) + ".");
			} else {
				if (CompMetadata.hasMetadata(this.item, "Destroy"))
					player.getInventory().remove(this.item);
				Sequence.TURRET_CREATION(block, type).start(block.getLocation());
				Messenger.success(player, "Successfully &aregistered &7the " + type + " turret at " + Common.shortLocation(block.getLocation()) + ".");
			}
		} else
			Messenger.error(player, "This block is already a turret, you can only have 1 type of turret per a block.");
	}

	@Override
	protected List<Location> getVisualizedPoints(final Player player) {
		if (!CompMetadata.hasMetadata(this.item, "Destroy"))
			return TurretRegistry.getInstance().getTurretLocationsOfType(this.turretType);

		return null;
	}

	@Override
	protected String getBlockName(final Block block, final Player player) {
		if (!CompMetadata.hasMetadata(this.item, "Destroy"))
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
			case "laser":
				return CompMaterial.BLAZE_ROD;
			case "flame":
				return CompMaterial.LAVA_BUCKET;
		}

		return null;
	}
}
