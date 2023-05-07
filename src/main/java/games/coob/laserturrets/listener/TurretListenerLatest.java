package games.coob.laserturrets.listener;

import games.coob.laserturrets.tools.TurretTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class TurretListenerLatest implements Listener {

	@EventHandler
	public void onPlayerSwapHandItems(final PlayerSwapHandItemsEvent event) {
		final ItemStack swappedItem = event.getOffHandItem();

		if (TurretTool.getTool(swappedItem) != null)
			event.setCancelled(true);
	}
}
