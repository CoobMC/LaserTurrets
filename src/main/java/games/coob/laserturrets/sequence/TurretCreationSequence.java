package games.coob.laserturrets.sequence;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompParticle;

public final class TurretCreationSequence extends Sequence {

	private final Player player;

	private final Block block;

	private final String type;

	TurretCreationSequence(final Player player, final Block block, final String type) {
		super("turret-creation");

		this.player = player;
		this.block = block;
		this.type = type;
	}

	/*
	 * Start this sequence
	 */
	@Override
	protected void onStart() {
		this.getLastLocation().add(0.5, 1.2, 0.5);

		final ItemStack item = ItemCreator.of(SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGNjNzI1NzhhNjBjMGViMWEzZmEzODFhYTYyMmEwYzkyNzZkYTdmOTU4YWU5YTBjNzFlZTQ4ZTc3MWZiMmNjNSJ9fX0=")).make();

		this.lightning();
		this.glowingStand(item);
		this.lightning();
		this.getLastStand().setAnimated(true);

		this.nextSequence(this::onFinish);
	}

	@Override
	public void disable() {
		this.removeLast();
	}

	private void onFinish() {
		CompParticle.EXPLOSION_LARGE.spawn(this.block.getLocation().add(0.5, 1, 0.5), 2);

		final Block skullBlock = this.block.getRelative(BlockFace.UP);
		games.coob.laserturrets.util.SkullCreator.blockWithBase64(skullBlock, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGNjNzI1NzhhNjBjMGViMWEzZmEzODFhYTYyMmEwYzkyNzZkYTdmOTU4YWU5YTBjNzFlZTQ4ZTc3MWZiMmNjNSJ9fX0=");

		Common.runLater(() -> TurretRegistry.getInstance().register(this.player, this.block, this.type));
		this.removeLast();
		this.block.removeMetadata("IsCreating", SimplePlugin.getInstance());
	}
}
