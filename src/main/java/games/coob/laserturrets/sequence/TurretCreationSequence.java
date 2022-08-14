package games.coob.laserturrets.sequence;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.model.SimpleHologram;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a sample crate drop sequence with many scenes.
 */
public final class TurretCreationSequence extends Sequence {

	/**
	 * The giant entity we spawn throughout this sequence stored
	 * here for convenient use.
	 */
	// private Giant giant;

	private final Block block;

	private final String type;

	/**
	 * The items in the circle around the giant we spawn during this
	 * sequence stored here for convenient use.
	 */
	private List<AnimatedHologram> circleItems = new ArrayList<>();

	TurretCreationSequence(final Block block, final String type) {
		super("turret-creation");

		this.block = block;
		this.type = type;
	}

	/*
	 * Start this sequence
	 */
	@Override
	protected void onStart() {
		this.getLastLocation().add(0.5, 0, 0.5);

		final ItemStack item = ItemCreator.of(SkullCreator.itemFromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGNjNzI1NzhhNjBjMGViMWEzZmEzODFhYTYyMmEwYzkyNzZkYTdmOTU4YWU5YTBjNzFlZTQ4ZTc3MWZiMmNjNSJ9fX0=")).make();

		// Step 1
		this.lightning();
		this.glowingStand(item);

		this.nextSequence(this::rotate);
	}

	@Override
	public void disable() {
		for (final SimpleHologram stand : this.circleItems)
			stand.remove();

		this.circleItems.clear();
		this.removeLast();
	}

	private void rotate() {

		// Step 2
		this.lightning();
		this.getLastLocation().add(0, 0.4, 0);
		this.getLastStand().setAnimated(true);

		this.nextSequence(this::circleItems);
	}

	/*private void giant() {

		// Step 3
		this.removeLast();

		this.giant = this.getLastLocation().getWorld().spawn(this.getLastLocation().add(2, -6.1, -4.5), Giant.class);

		CompProperty.AI.apply(giant, false);
		CompProperty.GRAVITY.apply(giant, false);
		Remain.setInvisible(giant, true);

		try {
			giant.setCollidable(false);
		} catch (final Throwable t) {
			// silence
		}

		giant.getEquipment().setItemInHand(new ItemStack(CompMaterial.CHEST.getMaterial()));
		giant.getWorld().playEffect(giant.getLocation().add(0, 1.7, 0), Effect.MOBSPAWNER_FLAMES, 0);

		this.getLastLocation().add(0, 1.5, 0);

		this.nextSequence(this::circleItems);
	}*/

	private void circleItems() {

		// Step 4
		final Location chestLocation = this.getLastStand().getLocation().clone().add(0, 2, 0);
		final double itemY = chestLocation.getY() + 2;

		final List<Integer> degrees = Arrays.asList(45, 90, 135, 180, 225, 270, 315, 360);
		this.circleItems = new ArrayList<>();

		for (int i = 0; i < degrees.size(); i++) {
			final int degree = degrees.get(i);
			final int circleRadius = 5;

			final double radians = Math.toRadians(degree);
			final double x = Math.cos(radians) * circleRadius;
			final double z = Math.sin(radians) * circleRadius;

			final boolean lastDegree = i + 1 == degrees.size();

			Common.runLater(20 + i * 20, () -> {

				final Location higherPosition = chestLocation.clone().add(x, 0, z);
				higherPosition.setY(itemY);

				this.setLastLocation(chestLocation);
				this.animatedStand(CompMaterial.PLAYER_HEAD.toItem());

				this.getLastStand().addParticleEffect(CompParticle.VILLAGER_HAPPY);

				this.getLastStand().teleport(higherPosition);

				final Entity entity = this.getLastStand().getEntity();

				Common.runTimer(2, new BukkitRunnable() {
					@Override
					public void run() {
						if (!entity.isValid()) {
							this.cancel();

							return;
						}

						if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_16))
							entity.getWorld().playEffect(entity.getLocation().add(0, 1.7, 0), Effect.SMOKE, BlockFace.DOWN);
						else
							entity.getWorld().playEffect(entity.getLocation().add(0, 1.7, 0), Effect.SMOKE, 6);
					}
				});

				this.circleItems.add(this.getLastStand());

				if (lastDegree)
					this.nextSequence(this::cleanUp);
			});
		}
	}

	/*private void dropItems() {

		// Step 5
		for (int i = 0; i < this.circleItems.size(); i++) {
			final SimpleHologram circleItem = this.circleItems.get(i);
			final boolean lastCircleItem = i + 1 == this.circleItems.size();

			Common.runLater(20 + i * 20, () -> {
				this.getLastLocation().getWorld().dropItem(circleItem.getLocation(), ItemCreator.of(CompMaterial.DIAMOND, "Special Diamond").make());

				if (lastCircleItem)
					this.nextSequence(this::cleanUp);
			});
		}
	}*/

	private void cleanUp() {

		// Step 6
		for (int i = 0; i < this.circleItems.size(); i++) {
			final SimpleHologram circleItem = this.circleItems.get(i);
			final boolean lastCircleItem = i + 1 == this.circleItems.size();

			Common.runLater(20 + i * 20, () -> {

				circleItem.getEntity().getWorld().playEffect(circleItem.getEntity().getLocation().add(0, 1.7, 0), Effect.MOBSPAWNER_FLAMES, 0);
				circleItem.remove();

				if (lastCircleItem) {
					Common.broadcast(Messenger.getSuccessPrefix() + "Sequence finished.");
					TurretRegistry.getInstance().register(this.block, this.type);
				}
			});
		}

		this.circleItems.clear();
	}
}
