package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;

public class TurretTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();

		for (final Location location : turretRegistry.getLocations()) {
			final Block block = location.getBlock();
			final int level = turretRegistry.getCurrentTurretLevel(block);
			final Player closestPlayer = EntityUtil.findNearestEntity(location, turretRegistry.getTurretRange(block, level), Player.class);

			if (closestPlayer == null)
				continue;

			if (turretRegistry.isLaserEnabled(block, turretRegistry.getCurrentTurretLevel(block)) && turretRegistry.getLaserDamage(block, level) > 0) {
				final double damage = turretRegistry.getLaserDamage(block, level);
				closestPlayer.damage(damage);
				Common.runLater(10, () -> closestPlayer.damage(damage));
			}

			if (turretRegistry.getType(block).equals("arrow")) {
				shootArrow(closestPlayer, block);
			} else if (turretRegistry.getType(block).equals("laser")) {
				closestPlayer.setFireTicks(20);
					/*for (int i = 0; i < 4; i++) { // Shoot 4 times for more flames! Change here to shoot more flames!
						//shootFlame(closestPlayer, block);
					}*/
			} else if (turretRegistry.getType(block).equals("flame")) {
				shootFlames(closestPlayer, block);
			}
		}
	}


	private void shootArrow(final Player target, final Block block) {
		if (target != null) {
			final Location location = block.getLocation();
			final Location targetLocation = target.getLocation().clone().add(-0.5, 1, -0.5);

			location.setY(location.getY() + 1.2);
			location.setX(location.getX() + 0.5);
			location.setZ(location.getZ() + 0.5);

			final Vector vector = targetLocation.subtract(block.getLocation()).toVector().normalize();
			final Arrow arrow = block.getWorld().spawnArrow(location, vector, 1, 0);

			arrow.setVelocity(vector.multiply(2).add(new Vector(0, 0.1, 0)));
		}
	}

	private void shootFlame(final Player target, final Block block) {
		if (target != null) {
			final Location location = block.getLocation();
			final Location targetLocation = target.getLocation().clone().add(-0.5, 1, -0.5);

			location.setY(location.getY() + 1.2);
			location.setX(location.getX() + 0.5);
			location.setZ(location.getZ() + 0.5);

			final Vector vector = targetLocation.subtract(block.getLocation()).toVector().normalize();
			final Consumer<Snowball> consumer = snowball -> {
				//Remain.setInvisible(snowball, true);
				snowball.setVelocity(vector.multiply(2).add(new Vector(0, 0.1, 0)));
			};

			final Snowball snowball = block.getWorld().spawn(location, Snowball.class, consumer);
			EntityUtil.trackFlying(snowball, () -> {
				final Location flameLocation = snowball.getLocation();
				CompParticle.FLAME.spawn(flameLocation);
			});
		}
	}

	private void shootFlames(final Player target, final Block turretBlock) {
		if (target == null)
			return;

		final int length = 50;
		final Location flameLocation = turretBlock.getLocation().clone();
		final Location targetLocation = target.getLocation().clone().add(0, 1, 0);

		flameLocation.setY(turretBlock.getY() + 1.2);
		flameLocation.setX(turretBlock.getX() + 0.5);
		flameLocation.setZ(turretBlock.getZ() + 0.5);

		final double dX = flameLocation.getX() - targetLocation.getX();
		final double dY = flameLocation.getY() - targetLocation.getY();
		final double dZ = flameLocation.getZ() - targetLocation.getZ();

		final double yaw = Math.atan2(dZ, dX);
		final double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

		final double X = Math.sin(pitch) * Math.cos(yaw);
		final double Y = Math.sin(pitch) * Math.sin(yaw);
		final double Z = Math.cos(pitch);

		final Vector vector = new Vector(X, Z, Y);
		final double accuracy = 0.2;
		vector.add(new Vector(Math.random() * accuracy - (accuracy / 2), Math.random() * accuracy - (accuracy / 2), Math.random() * accuracy - (accuracy / 2)));

		for (double waypoint = 1; waypoint < length; waypoint += 0.5) {
			flameLocation.add(vector);
			CompParticle.FLAME.spawn(flameLocation);
			final int randomInt = RandomUtil.nextInt(100);

			if (randomInt < 6) {
				if (!CompMaterial.isAir(flameLocation.getBlock())) {
					final Block block = flameLocation.getBlock();
					final Block blockUp = block.getRelative(BlockFace.UP);

					if (CompMaterial.isAir(blockUp) && blockUp.getRelative(BlockFace.DOWN).getType().isSolid())
						blockUp.setType(Material.FIRE);
				}
			}
		}
	}


/*	private void shootFlames(Player player) {

		Vector playerDirection = player.getLocation().getDirection();
		Vector particleVector = playerDirection.clone();

		playerDirection.multiply(8); // Set length to 8 blocks out

		// rotate particle location 90 degrees
		double x = particleVector.getX();
		particleVector.setX(-particleVector.getZ());
		particleVector.setZ(x);
		particleVector.divide(new Vector(3, 3, 3)); // Divide it by 2 to shorten length

		Location particleLocation = particleVector.toLocation(player.getWorld()).add(player.getLocation()).add(0, 1.05, 0);

		for (int i = 0; i < 4; i++) { // Shoot 4 times for more flames! Change here to shoot more flames!
			shootSingleFlame(playerDirection, particleLocation);
		}

		if (Math.random() < fireChance) { // Light fire to block one fifth of the time
			Block lookingBlock = player.getTargetBlock((Set<Material>) null, 15); // Get target block in 15 block range
			if (lookingBlock != null && lookingBlock.getType().isBlock()) {
				Block upBlock = lookingBlock.getRelative(BlockFace.UP);
				if (upBlock != null && upBlock.getType() == Material.AIR) {
					new BukkitRunnable() {
						@Override
						public void run() {
							upBlock.setType(Material.FIRE);
						}
					}.runTaskLater(plugin, 10); // run half a second later for a more realistic effect.
				}
			}
		}
	}*/

	// Separate method for efficiency
	/*private void shootSingleFlame(final Vector turretDirection, final World world) {
		final Vector flamePath = turretDirection.clone(); // clone to prevent extra math calculations

		flamePath.add(new Vector(flamePath.getX() - Math.random(), turretDirection.getY() - Math.random(), flamePath.getZ() - Math.random())); // Add some randomness

		final Location offsetLocation = flamePath.toLocation(world);

		CompParticle.FLAME.spawn(new Location(world, offsetLocation.getX(), offsetLocation.getY(), offsetLocation.getZ()));
	}*/

	/*
	private void shootSingleFlame(final Block block, final Player target) {
		final int length = 50; // show twenty blocks ahead
		final Location turretLocation = block.getLocation();
		final Location laserLocation = turretLocation.clone();

		laserLocation.setY(turretLocation.getY() + 1.2);
		laserLocation.setX(turretLocation.getX() + 0.5);
		laserLocation.setZ(turretLocation.getZ() + 0.5);

		final double dX = laserLocation.getX() - target.getLocation().getX();
		final double dY = laserLocation.getY() - target.getLocation().getY();
		final double dZ = laserLocation.getZ() - target.getLocation().getZ();

		final double yaw = Math.atan2(dZ, dX);
		final double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;

		final double X = Math.sin(pitch) * Math.cos(yaw);
		final double Y = Math.sin(pitch) * Math.sin(yaw);
		final double Z = Math.cos(pitch);

		final Vector vector = new Vector(X, Z, Y);

		for (double waypoint = 1; waypoint < length; waypoint += 0.5) {
			laserLocation.add(vector);

			CompParticle.FLAME.spawn(laserLocation);
		}
	}*/
}


