package games.coob.laserturrets.listener;

import games.coob.laserturrets.menu.BrokenTurretMenu;
import games.coob.laserturrets.menu.UpgradeMenu;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.remain.Remain;

@AutoRegister
public final class TurretListener implements Listener {

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
		final Block block = event.getBlock();
		final Block blockUnder = block.getRelative(BlockFace.DOWN);
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block) || turretRegistry.isRegistered(blockUnder)) {
			event.setCancelled(true);
			damageTurret(block, 50);
		}
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		final Block block = event.getBlock();
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		if (turretRegistry.isRegistered(block))
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
		final double damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();

		if (registry.isRegistered(block)) {
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

	private void openTurretMenu(final Player player, final Block block) {
		if (Tool.getTool(player.getInventory().getItemInMainHand()) != null)
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
		final Block block = event.getHitBlock();
		final TurretRegistry registry = TurretRegistry.getInstance();

		if (event.getEntity() instanceof Arrow) {
			if (block == null)
				return;

			final Block blockUnder = block.getRelative(BlockFace.DOWN);
			final ProjectileSource source = event.getEntity().getShooter();
			final double damage = 10.0;

			if (source != null) {
				if (registry.isRegistered(block))
					damageTurret((LivingEntity) source, block, damage);
				else if (registry.isRegistered(blockUnder))
					damageTurret((LivingEntity) source, blockUnder, damage);

				Common.runLater(60, () -> event.getEntity().remove());
			}
		}
	}

	private void damageTurret(final LivingEntity entity, final Block block, final double damage) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(block);

		registry.setTurretHealth(block, turretData.getCurrentHealth() - damage);

		if (turretData.getCurrentHealth() <= 0) {
			registry.setBroken(block, true);
			block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, block.getLocation().add(0.5, 1, 0.5), 2);
			registry.setTurretHealth(block, 0);
		}

		if (entity instanceof Player)
			Remain.sendActionBar((Player) entity, turretData.getCurrentHealth() > 0 ? turretData.getCurrentHealth() + "&c❤" : "&cTurret is broken!");
	}

	private void damageTurret(final Block block, final double damage) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final TurretData turretData = registry.getTurretByBlock(block);

		registry.setTurretHealth(block, turretData.getCurrentHealth() - damage);

		if (turretData.getCurrentHealth() <= 0) {
			registry.setBroken(block, true);
			block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, block.getLocation().add(0.5, 1, 0.5), 2);
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		final Block blockDown = block.getRelative(BlockFace.DOWN);
		final TurretRegistry registry = TurretRegistry.getInstance();

		for (final BlockFace blockface : BlockFace.values()) {
			if ((registry.isRegistered(block.getRelative(blockface)) && blockface != BlockFace.UP) || registry.isRegistered(blockDown.getRelative(blockface)))
				event.setCancelled(true);
		}
	}
}
