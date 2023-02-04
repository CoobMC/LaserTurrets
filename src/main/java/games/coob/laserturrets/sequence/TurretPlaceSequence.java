package games.coob.laserturrets.sequence;

import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.SkullCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompParticle;

public final class TurretPlaceSequence extends Sequence {

	private final Player player;

	private final Block block;

	private final String type;

	private final String turretId;

	TurretPlaceSequence(final Player player, final Block block, final String type, final String turretId) {
		super("turret-creation");

		this.player = player;
		this.block = block;
		this.type = type;
		this.turretId = turretId;
	}

	/*
	 * Start this sequence
	 */
	@Override
	protected void onStart() {
		this.getLastLocation().add(0.5, 1.2, 0.5);

		final TurretSettings turretSettings = TurretSettings.findTurretSettings(this.type);
		final ItemStack item = ItemCreator.of(SkullCreator.itemFromBase64(turretSettings.getBase64Texture())).make();

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

		final TurretSettings turretSettings = TurretSettings.findTurretSettings(this.type);
		final Block skullBlock = this.block.getRelative(BlockFace.UP);
		final TurretRegistry registry = TurretRegistry.getInstance();

		SkullCreator.blockWithBase64(skullBlock, turretSettings.getBase64Texture());
		SkullCreator.rotateSkull((Skull) skullBlock.getState(), PlayerUtil.getFacing(this.player)); // TODO getOpposite
		Common.runLater(() -> registry.registerTurretById(this.block, this.turretId));

		this.removeLast();
		this.block.removeMetadata("IsCreating", SimplePlugin.getInstance());
	}
}
