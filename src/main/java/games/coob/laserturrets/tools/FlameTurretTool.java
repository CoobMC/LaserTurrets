package games.coob.laserturrets.tools;

import games.coob.laserturrets.model.TurretRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.visual.VisualTool;

import java.util.List;

/**
 * An automatically registered tool you can use in the game
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AutoRegister
public final class FlameTurretTool extends VisualTool {

	/**
	 * The singular tool instance
	 */
	@Getter
	private static final Tool instance = new FlameTurretTool();

	/**
	 * The actual item stored here for maximum performance
	 */
	private ItemStack item;

	/**
	 * @see Tool#getItem()
	 */
	@Override
	public ItemStack getItem() {

		if (item == null)
			item = ItemCreator.of(
							CompMaterial.LAVA_BUCKET,
							"&aFlame Turret Tool",
							"",
							"&7Click blocks to",
							"&7un/register a turret.").glow(true)
					.make();

		return item;
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

	@Override
	protected void handleBlockClick(final Player player, final ClickType click, final Block block) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final String type = "flame";
		final boolean isRegisteredExclude = registry.isRegisteredExclude(block, type);
		final boolean isFlameTurret = registry.isFlameTurret(block);

		if (!isRegisteredExclude) {
			if (isFlameTurret && !CompMetadata.hasMetadata(this.item, "Destroy")) {
				registry.unregister(block, type);
				Messenger.success(player, "Successfully &cunregistered &7the laser turret at " + Common.shortLocation(block.getLocation()) + ".");
			} else {
				if (CompMetadata.hasMetadata(this.item, "Destroy"))
					player.getInventory().remove(this.item);
				registry.register(block, type);
				Messenger.success(player, "Successfully &aregistered &7the laser turret at " + Common.shortLocation(block.getLocation()) + ".");
			}
		} else
			Messenger.error(player, "This block is already a turret, you can only have 1 type of turret per a block.");
	}

	@Override
	protected List<Location> getVisualizedPoints(final Player player) {
		if (!CompMetadata.hasMetadata(this.item, "Destroy"))
			return TurretRegistry.getInstance().getFlameLocations();

		return null;
	}

	@Override
	protected String getBlockName(final Block block, final Player player) {
		if (!CompMetadata.hasMetadata(this.item, "Destroy"))
			return "&aRegistered Flame Turret";

		return null;
	}

	@Override
	protected CompMaterial getBlockMask(final Block block, final Player player) {
		return CompMaterial.EMERALD_BLOCK;
	}

	public static void giveOneUse(final Player player) {
		final ItemStack item = getInstance().getItem();
		CompMetadata.setMetadata(item, "Destroy", "");
		player.getInventory().addItem(item);
	}
}
