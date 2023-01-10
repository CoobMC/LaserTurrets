package games.coob.laserturrets.sequence;

import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.TurretSettings;
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

	private final ItemStack persistentItem;

	TurretCreationSequence(final Player player, final Block block, final String type, final ItemStack persistentItem) {
		super("turret-creation");

		this.player = player;
		this.block = block;
		this.type = type;
		this.persistentItem = persistentItem;
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
		Common.runLater(() -> registry.register(this.player, this.block, this.type));

		this.removeLast();
		this.block.removeMetadata("IsCreating", SimplePlugin.getInstance());
	}

	/*private Map<String, Object> retrieveData(final ItemStack itemStack) {
		final NamespacedKey key = new NamespacedKey(SimplePlugin.getInstance(), "TurretData");
		final ItemMeta itemMeta = itemStack.getItemMeta();
		final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

		if (container.has(key, PersistentDataType.STRING)) {
			final String dataValue = container.get(key, PersistentDataType.STRING);
			final String deserializedValue = SerializeUtil.deserialize(SerializeUtil.Mode.JSON, String.class, dataValue);


		}

		return null;
	}*/
}
