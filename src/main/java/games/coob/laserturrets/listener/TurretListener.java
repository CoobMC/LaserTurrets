package games.coob.laserturrets.listener;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.menu.BrokenTurretMenu;
import games.coob.laserturrets.menu.UpgradeMenu;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AutoRegister
public final class TurretListener implements Listener { // TODO remove turret if item no longer existent

	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			Common.runLaterAsync(() -> TurretsDatabase.load(player));
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();

		if (Settings.DatabaseSection.ENABLE_MYSQL) {
			Common.runLaterAsync(() -> {
				TurretsDatabase.save(player);
				PlayerCache.remove(player);
			});
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final Block blockUnder = block.getRelative(BlockFace.DOWN);
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block) || turretRegistry.isRegistered(blockUnder))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		final List<Block> blockList = new ArrayList<>();

		for (final Block block : event.blockList()) {
			final Block blockUnder = block.getRelative(BlockFace.DOWN);

			if (turretRegistry.isRegistered(block)) {
				blockList.add(block);
				damageTurretQuiet(block, 40);
			} else if (turretRegistry.isRegistered(blockUnder))
				blockList.add(block);
		}

		for (final Block block : blockList)
			event.blockList().remove(block);
	}

	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		final List<Block> blockList = new ArrayList<>();

		for (final Block block : event.blockList()) {
			final Block blockUnder = block.getRelative(BlockFace.DOWN);

			if (turretRegistry.isRegistered(block)) {
				blockList.add(block);
				damageTurretQuiet(block, 40);
			} else if (turretRegistry.isRegistered(blockUnder))
				blockList.add(block);
		}

		for (final Block block : blockList)
			event.blockList().remove(block);
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		final Block block = event.getBlock();
		final Block blockUnder = block.getRelative(BlockFace.DOWN);
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block) || turretRegistry.isRegistered(blockUnder))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteractAtBlock(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();

		if (block == null)
			return;

		final Block blockUnder = block.getRelative(BlockFace.DOWN);
		final TurretRegistry registry = TurretRegistry.getInstance();
		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final ItemStack item = player.getItemInHand();

		if (Tool.getTool(item) == null) {
			final Double damage = CompAttribute.GENERIC_ATTACK_DAMAGE.get(player); // TODO doesn't work in 1.18 
			//player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

			if (registry.isRegistered(block)) {
				event.setCancelled(true);

				if (action == Action.RIGHT_CLICK_BLOCK)
					openTurretMenu(player, block);
				else if (action == Action.LEFT_CLICK_BLOCK)
					damageTurret(player, block, damage);
			} else if (registry.isRegistered(blockUnder)) {
				if (action == Action.RIGHT_CLICK_BLOCK)
					openTurretMenu(player, blockUnder);
				else if (action == Action.LEFT_CLICK_BLOCK)
					damageTurret(player, blockUnder, damage);
			}
		}
	}

	private void openTurretMenu(final Player player, final Block block) {
		if (Tool.getTool(player.getInventory().getItemInHand()) != null)
			return;

		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(block);

		if (turretData.isBroken()) {
			if (turretData.getOwner().equals(player.getUniqueId()))
				BrokenTurretMenu.openOwnerMenu(turretData, player);
			else BrokenTurretMenu.openPlayerMenu(turretData, player);
		} else {
			if (turretData.getOwner().equals(player.getUniqueId()))
				new UpgradeMenu(turretData, turretData.getCurrentLevel(), player).displayTo(player);
		}
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final Projectile projectile = event.getEntity();

		if (projectile instanceof Arrow) {
			final Arrow arrow = (Arrow) projectile;

			Common.runLater(() -> {
				if (!arrow.isOnGround())
					return;

				final Location arrowLocation = arrow.getLocation();
				final Vector vector = arrow.getVelocity().normalize().multiply(0.1);

				arrowLocation.add(vector);

				final Block block = arrowLocation.getBlock();
				final Block blockUnder = block.getRelative(BlockFace.DOWN);
				final ProjectileSource source = event.getEntity().getShooter();
				final double damage = 5.0;

				if (registry.isRegistered(blockUnder)) {
					damageTurret((LivingEntity) source, blockUnder, damage);
					Common.runLater(60, () -> event.getEntity().remove());
				} else if (registry.isRegistered(block)) {
					damageTurret((LivingEntity) source, block, damage);
					Common.runLater(60, () -> event.getEntity().remove());
				}
			});
		}
	}

	private void damageTurret(final LivingEntity entity, final Block block, final double damage) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(block);

		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final PlayerCache cache = PlayerCache.from(player);

			if (cache.isTurretHit())
				return;

			cache.setTurretHit(true);
			Common.runLater(20, () -> cache.setTurretHit(false));
		}

		damageTurretQuiet(block, damage);

		if (entity instanceof Player && Settings.TurretSection.DISPLAY_ACTION_BAR) {
			final String health = Lang.of("Turret_Display.Action_Bar_Damage", "{health}", turretData.getCurrentHealth());
			Remain.sendActionBar((Player) entity, turretData.getCurrentHealth() > 0 ? health : Lang.of("Turret_Display.Action_Bar_Damage_But_Broken"));
		}
	}

	private void damageTurretQuiet(final Block block, final double damage) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(block);
		final Location location = block.getLocation().clone();

		registry.setTurretHealth(block, turretData.getCurrentHealth() - damage);
		CompSound.ITEM_BREAK.play(location);
		turretData.getHologram().update(turretData);

		if (turretData.getCurrentHealth() <= 0 && !turretData.isBroken()) {
			registry.setBrokenAndFill(block, true);
			CompSound.EXPLODE.play(location);
			CompParticle.EXPLOSION_LARGE.spawn(location.add(0.5, 1, 0.5), 2);
		}
	}

	@EventHandler
	public void onItemDespawn(final ItemDespawnEvent event) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final ItemStack despawnedItem = event.getEntity().getItemStack();

		for (final Tuple<TurretData, ItemStack> tuple : registry.getRegisteredUnplacedTurrets()) {
			final ItemStack itemStack = tuple.getValue();

			if (itemStack != null && itemStack.isSimilar(despawnedItem))
				registry.unregisterUnplaced(tuple.getKey());
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();
		final TurretRegistry registry = TurretRegistry.getInstance();

		if (entity instanceof Item) {
			final Item despawnedItem = (Item) event.getEntity();

			for (final Tuple<TurretData, ItemStack> tuple : registry.getRegisteredUnplacedTurrets()) {
				final ItemStack itemStack = tuple.getValue();

				if (itemStack.isSimilar(despawnedItem.getItemStack()))
					for (final EntityDamageEvent.DamageCause damageCause : EntityDamageEvent.DamageCause.values())
						if (event.getCause() == damageCause)
							Common.runLater(() -> registry.unregisterUnplaced(tuple.getKey()));
			}
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		final Block blockDown = block.getRelative(BlockFace.DOWN);
		final TurretRegistry registry = TurretRegistry.getInstance();
		final ItemStack item = event.getItemInHand();

		if (CompMetadata.hasMetadata(item, "id"))
			placeTurret(item, event);

		for (final BlockFace blockface : BlockFace.values()) {
			if ((registry.isRegistered(block.getRelative(blockface)) && blockface != BlockFace.UP) || registry.isRegistered(blockDown.getRelative(blockface)))
				event.setCancelled(true);
		}
	}

	private void placeTurret(final ItemStack item, final BlockPlaceEvent event) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final String id = CompMetadata.getMetadata(item, "id");
		final String type = registry.getUnplacedTurretById(id).getType();
		final Player player = event.getPlayer();
		final Block block = event.getBlockAgainst();

		event.setCancelled(true);

		if (!block.getType().isSolid()) {
			Messenger.error(player, Lang.of("Tool.Turret_Cannot_Be_Placed"));
			return;
		}

		if (registry.isRegistered(block) && !Objects.equals(registry.getTurretByBlock(block).getType(), type)) {
			Messenger.error(player, Lang.of("Tool.Block_Is_Already_Turret"));
			return;
		}

		player.getInventory().remove(item);
		Sequence.TURRET_PLACE(block, type, id).start(block.getLocation());
	}
}
