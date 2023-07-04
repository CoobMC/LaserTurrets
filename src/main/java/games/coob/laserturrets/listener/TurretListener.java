package games.coob.laserturrets.listener;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.HookSystem;
import games.coob.laserturrets.menu.BrokenTurretMenu;
import games.coob.laserturrets.menu.UpgradeMenu;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.UnplacedData;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.TurretTool;
import games.coob.laserturrets.util.BlockUtil;
import games.coob.laserturrets.util.CompAttribute;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AutoRegister
public final class TurretListener implements Listener {

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

		if (TurretData.isRegistered(block) || TurretData.isRegistered(blockUnder))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {
		final List<Block> blockList = new ArrayList<>();

		for (final Block block : event.blockList()) {
			final Block blockUnder = block.getRelative(BlockFace.DOWN);

			if (TurretData.isRegistered(block)) {
				blockList.add(block);
				damageTurretQuiet(block, 40);
			} else if (TurretData.isRegistered(blockUnder))
				blockList.add(block);
		}

		for (final Block block : blockList)
			event.blockList().remove(block);
	}

	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {
		final List<Block> blockList = new ArrayList<>();

		for (final Block block : event.blockList()) {
			final Block blockUnder = block.getRelative(BlockFace.DOWN);

			if (TurretData.isRegistered(block)) {
				blockList.add(block);
				damageTurretQuiet(block, 40);
			} else if (TurretData.isRegistered(blockUnder))
				blockList.add(block);
		}

		for (final Block block : blockList)
			event.blockList().remove(block);
	}

	@EventHandler
	public void onBlockBurn(final BlockBurnEvent event) {
		final Block block = event.getBlock();
		final Block blockUnder = block.getRelative(BlockFace.DOWN);

		if (TurretData.isRegistered(block) || TurretData.isRegistered(blockUnder))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteractAtBlock(final PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();

		if (block == null)
			return;

		final Block blockUnder = block.getRelative(BlockFace.DOWN);
		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final ItemStack item = player.getItemInHand();

		if (Tool.getTool(item) == null) {
			final double damage = CompAttribute.GENERIC_ATTACK_DAMAGE.get(player);

			if (TurretData.isRegistered(block)) {
				event.setCancelled(true);

				if (action == Action.RIGHT_CLICK_BLOCK)
					openTurretMenu(player, block);
				else if (action == Action.LEFT_CLICK_BLOCK)
					damageTurret(player, block, damage);
			} else if (TurretData.isRegistered(blockUnder)) {
				if (action == Action.RIGHT_CLICK_BLOCK)
					openTurretMenu(player, blockUnder);
				else if (action == Action.LEFT_CLICK_BLOCK)
					damageTurret(player, blockUnder, damage);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		final ItemStack itemStack = event.getCursor();

		if ((event.getSlotType() == InventoryType.SlotType.ARMOR || event.getRawSlot() == 45) && itemStack != null && TurretTool.getTool(itemStack) != null)
			event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryDrag(final InventoryDragEvent event) {
		for (final int slot : event.getInventorySlots())
			if ((slot >= 36 && slot <= 39) || slot == 45)
				for (final ItemStack item : event.getNewItems().values())
					if (item != null && TurretTool.getTool(item) != null)
						event.setCancelled(true);
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event) {
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

				if (TurretData.isRegistered(blockUnder)) {
					damageTurret((LivingEntity) source, blockUnder, damage);
					Common.runLater(60, () -> event.getEntity().remove());
				} else if (TurretData.isRegistered(block)) {
					damageTurret((LivingEntity) source, block, damage);
					Common.runLater(60, () -> event.getEntity().remove());
				}
			});
		}
	}

	@EventHandler
	public void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent event) {
		final ArmorStand armorStand = event.getRightClicked();

		if (armorStand.hasMetadata("AnimatedStand"))
			event.setCancelled(true);
	}

	@EventHandler
	public void onItemDespawn(final ItemDespawnEvent event) {
		final ItemStack despawnedItem = event.getEntity().getItemStack();

		for (final UnplacedData data : UnplacedData.getUnplacedTurrets()) {
			final ItemStack itemStack = data.getTurretItem();

			if (itemStack != null && itemStack.isSimilar(despawnedItem))
				data.unregister();
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();

		if (entity instanceof LivingEntity && entity.hasMetadata("TurretDamage")) {
			Common.runLater(2, () -> entity.removeMetadata("TurretDamage", SimplePlugin.getInstance()));
		} else if (entity instanceof Item) {
			final Item despawnedItem = (Item) event.getEntity();

			for (final UnplacedData data : UnplacedData.getUnplacedTurrets()) {
				final ItemStack itemStack = data.getTurretItem();

				if (itemStack.isSimilar(despawnedItem.getItemStack()))
					for (final EntityDamageEvent.DamageCause damageCause : EntityDamageEvent.DamageCause.values())
						if (event.getCause() == damageCause)
							Common.runLater(data::unregister);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		final Block blockDown = block.getRelative(BlockFace.DOWN);
		final ItemStack item = event.getItemInHand();

		if (CompMetadata.hasMetadata(item, "id"))
			placeTurret(item, event);

		for (final BlockFace blockface : BlockFace.values()) {
			if ((TurretData.isRegistered(block.getRelative(blockface)) && blockface != BlockFace.UP) || TurretData.isRegistered(blockDown.getRelative(blockface)))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDeath(final EntityDeathEvent event) {
		final LivingEntity entity = event.getEntity();
		final Player player = entity.getKiller();
		final EntityDamageEvent lastDamageCause = entity.getLastDamageCause();

		if (player != null)
			return;

		if (lastDamageCause instanceof EntityDamageByEntityEvent) {
			final EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) lastDamageCause;
			final Entity damager = entityDamageByEntityEvent.getDamager();

			if (damager instanceof Projectile) {
				final Projectile projectile = (Projectile) damager;

				if (projectile.hasMetadata("LaserTurrets"))
					turretKillAction(projectile, event, "LaserTurrets");
			}
		} else if (entity.hasMetadata("TurretDamage"))
			turretKillAction(entity, event, "TurretDamage");
	}

	private void turretKillAction(final Entity entity, final EntityDeathEvent event, final String metadataKey) {
		String turretId = null;
		final LivingEntity victim = event.getEntity();

		for (final MetadataValue value : entity.getMetadata(metadataKey))
			turretId = value.asString();

		final TurretKillEvent turretKillEvent = new TurretKillEvent(victim, turretId);

		if (Settings.TurretSection.REMOVE_DROPS_ON_MOB_KILL && !(entity instanceof Player))
			event.getDrops().clear();

		Bukkit.getPluginManager().callEvent(turretKillEvent);

		if (event instanceof PlayerDeathEvent && Settings.TurretSection.ENABLE_TURRET_KILL_MESSAGE) {
			final PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;
			final TurretData turretData = turretKillEvent.getTurretData();
			final String message = Common.colorize(Lang.of("Turret_Display.Turret_Player_Kill_Message", "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{turretType}", turretData.getType(), "{victim}", victim.getName()));

			playerDeathEvent.setDeathMessage(message);
		}
	}

	private void placeTurret(final ItemStack item, final BlockPlaceEvent event) {
		final String id = CompMetadata.getMetadata(item, "id");
		final UnplacedData turretData = UnplacedData.findById(id);
		final String type = turretData.getType();
		final Player player = event.getPlayer();
		final Block block = event.getBlockAgainst();
		final Location location = block.getLocation();

		event.setCancelled(true);

		if (Settings.TurretSection.BLACKLISTED_WORLDS.contains(block.getWorld().getName())) {
			Messenger.error(player, Lang.of("Tool.Blacklisted_World"));
			return;
		}

		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_13) ? block.getType().isInteractable() : BlockUtil.isInteractable(block.getType()))
			return;

		if (Settings.TurretSection.BUILD_IN_OWN_TERRITORY && !HookSystem.canBuild(location, player) && !TurretData.isRegistered(block)) {
			Messenger.error(player, Lang.of("Tool.Not_Permitted_In_Region"));
			return;
		}

		if (!block.getType().isSolid()) {
			Messenger.error(player, Lang.of("Tool.Turret_Cannot_Be_Placed"));
			return;
		}

		if (TurretData.isRegistered(block) && !Objects.equals(TurretData.findByBlock(block).getType(), type)) {
			Messenger.error(player, Lang.of("Tool.Block_Is_Already_Turret"));
			return;
		}

		player.getInventory().remove(item);
		Sequence.TURRET_PLACE(player, block, type, id).start(location);
	}

	private void damageTurret(final LivingEntity entity, final Block block, final double damage) {
		final TurretData turretData = TurretData.findByBlock(block);

		if (turretData != null && TurretSettings.findByName(turretData.getType()).isInvincible())
			return;

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
		final TurretData turretData = TurretData.findByBlock(block);
		final Location location = block.getLocation().clone();
		final boolean canDisplayHologram = Settings.TurretSection.DISPLAY_HOLOGRAM;

		if (turretData == null)
			return;

		turretData.setTurretHealth(block, turretData.getCurrentHealth() - damage);
		CompSound.ITEM_BREAK.play(location);

		if (turretData.getCurrentHealth() <= 0 && !turretData.isBroken()) {
			turretData.setBrokenAndFill(block, true);
			CompSound.EXPLODE.play(location);
			CompParticle.EXPLOSION_LARGE.spawn(location.add(0.5, 1, 0.5), 2);
		}

		if (canDisplayHologram) {
			if (turretData.isBroken()) {
				final String[] lore = Lang.ofArray("Turret_Display.Broken_Turret_Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()));
				turretData.getHologram().updateLore(lore);
			} else {
				final String[] loreHologram = Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
				turretData.getHologram().updateLore(loreHologram);
			}
		}
	}

	private void openTurretMenu(final Player player, final Block block) {
		if (Tool.getTool(player.getInventory().getItemInHand()) != null)
			return;

		final TurretData turretData = TurretData.findByBlock(block);

		if (turretData.isBroken()) {
			if (turretData.getOwner().equals(player.getUniqueId()))
				BrokenTurretMenu.openOwnerMenu(turretData, player);
			else BrokenTurretMenu.openPlayerMenu(turretData, player);
		} else {
			if (turretData.getOwner().equals(player.getUniqueId()))
				new UpgradeMenu(turretData, turretData.getCurrentLevel(), player).displayTo(player);
		}
	}
}
