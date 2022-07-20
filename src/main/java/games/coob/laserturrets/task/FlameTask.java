package games.coob.laserturrets.task;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;

public class FlameTask extends BukkitRunnable {

	@Override
	public void run() {
		final TurretRegistry turretRegistry = TurretRegistry.getInstance();
		for (final TurretData turretData : turretRegistry.getFlameTurrets()) {
			final Location location = turretData.getLocation();
			final Block block = location.getBlock();
			final int level = turretRegistry.getCurrentTurretLevel(block);
			final LivingEntity nearestEntity = EntityUtil.findNearestEntity(location, turretRegistry.getTurretRange(block, level), LivingEntity.class);

			if (nearestEntity == null)
				continue;

			if (turretRegistry.isLaserEnabled(block, turretRegistry.getCurrentTurretLevel(block)) && turretRegistry.getLaserDamage(block, level) > 0) {
				final double damage = turretRegistry.getLaserDamage(block, level);
				nearestEntity.damage(damage);
				shootFlames(nearestEntity, block);
			}
		}
	}

	private void shootFlame(final LivingEntity target, final Block block) {
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

	private void shootFlames(final LivingEntity target, final Block turretBlock) {
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
}


