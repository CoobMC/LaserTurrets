package games.coob.laserturrets.tools;

import games.coob.laserturrets.sequence.Sequence;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.visual.VisualTool;

/**
 * Handles tools that click within an arena.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TurretTool extends VisualTool {

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
		Sequence.TURRET_CREATION.start(block.getLocation());
	}
}
