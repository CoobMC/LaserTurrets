package games.coob.laserturrets.tools;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.CompMetadata;

/**
 * An automatically registered tool you can use in the game
 */
@AutoRegister
public final class ArrowTurretTool extends TurretTool {

	@Getter
	private static final Tool instance = new ArrowTurretTool();

	private ArrowTurretTool() {
		super("arrow");
	}

	public static void giveOneUse(final Player player) {
		final ItemStack item = getInstance().getItem();
		CompMetadata.setMetadata(item, "Destroy", "");
		player.getInventory().addItem(item);
	}
}
