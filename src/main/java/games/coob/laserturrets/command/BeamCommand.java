package games.coob.laserturrets.command;

import games.coob.laserturrets.util.BeamUtil_v1_8;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.command.SimpleSubCommand;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.util.List;

public class BeamCommand extends SimpleSubCommand {
	protected BeamCommand() {
		super("beam");
	}

	@Override
	protected void onCommand() {
		final Location location = getPlayer().getEyeLocation();
		final LivingEntity entity = EntityUtil.findNearestEntity(location, 50, Player.class);

		if (args.length == 1) {
			final String param = this.args[0];

			if (param.equals("beam"))
				BeamUtil_v1_8.spawnGuardianBeam(location, entity, 20);
			else if (param.equals("particle"))
				//spawnParticlesInArc(location, entity.getEyeLocation(), 400, 1.5, Particle.VILLAGER_HAPPY, 10, 0.1, 9.81, 10.0);
				spawnParticlesInArc(location, entity.getEyeLocation(), 12, Particle.EXPLOSION_NORMAL, Particle.FLAME);
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (this.args.length == 1)
			return completeLastWord("beam", "particle");

		return NO_COMPLETE;
	}

	public void shootParticleBullet(final Location startLoc, final Location targetLoc, final double bulletSpacing, final int bulletSpeedTicks, final Particle particle) {
		// Get the direction vector from startLoc to targetLoc
		final Vector direction = targetLoc.toVector().subtract(startLoc.toVector()).normalize();

		// Calculate the velocity vector of the particle bullet
		final Vector velocity = direction.multiply(bulletSpacing);

		// Calculate the number of steps needed to create a smooth particle trail
		final int numSteps = (int) (startLoc.distance(targetLoc) / 0.2);

		// Calculate the time interval between each particle step
		final long interval = bulletSpeedTicks / numSteps;

		// Spawn particles along the path from startLoc to targetLoc
		new BukkitRunnable() {
			int step = 0;

			@Override
			public void run() {
				if (step >= numSteps) {
					this.cancel();
				} else {
					// Calculate the location of the particle based on the current step
					final double progress = (double) step / (double) numSteps;
					final Location currentLoc = startLoc.clone().add(direction.clone().multiply(startLoc.distance(targetLoc) * progress));

					// Spawn the particle at the current location
					currentLoc.getWorld().spawnParticle(particle, currentLoc, 1, 0, 0, 0, 0);

					step++;
				}
			}
		}.runTaskTimer(SimplePlugin.getInstance(), 0, interval);
	}

	public void spawnParticlesInArc(final Location source, final Location target, final double heightOffset, final Particle particle, final Particle particle2) {
		final double distance = source.distance(target);
		final double xDiff = target.getX() - source.getX();
		final double yDiff = target.getY() - source.getY();
		final double zDiff = target.getZ() - source.getZ();

		final int trajectoryCount = 80; // Smaller count for smoother trajectory
		final double trajectoryInterval = distance / trajectoryCount;
		//final Vector arcVector = new Vector(0, 0, 0);

		Location blockHitLocation = target;
		for (double totalDistance = 0; totalDistance <= distance; totalDistance += trajectoryInterval) {
			final double s = totalDistance / distance;
			final double trajectoryX = source.getX() + xDiff * s;
			final double trajectoryY = source.getY() + yDiff * s + heightOffset * Math.sin(Math.PI * s);
			final double trajectoryZ = source.getZ() + zDiff * s;

			final Location particleLocation = new Location(source.getWorld(), trajectoryX, trajectoryY, trajectoryZ);

			if (particleLocation.getBlock().getType().isSolid()) {
				blockHitLocation = particleLocation.getBlock().getLocation();
				break;
			}

			Common.runLater((int) (totalDistance / trajectoryInterval), () -> {
				source.getWorld().spawnParticle(particle, trajectoryX, trajectoryY, trajectoryZ, 1, 0, 0, 0, 0);
				source.getWorld().spawnParticle(particle2, trajectoryX, trajectoryY, trajectoryZ, 1, 0, 0, 0, 0);
			});
			//arcVector.add(new Vector(trajectoryX, trajectoryY, trajectoryZ));
		}

		final Location explodeLocation = blockHitLocation;
		Common.runLater((int) (distance / trajectoryInterval), () -> explodeLocation.getWorld().createExplosion(new Location(explodeLocation.getWorld(), explodeLocation.getX(), explodeLocation.getY(), explodeLocation.getZ()), 2f));
	}
}
