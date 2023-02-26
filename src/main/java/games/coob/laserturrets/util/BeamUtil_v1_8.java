package games.coob.laserturrets.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.mineacademy.fo.Common;

public class BeamUtil_v1_8 {

	public static void spawnGuardianBeam(final Location start, final LivingEntity target, final int duration) {

		// Get the NMS world from the Bukkit world
		final WorldServer world = ((CraftWorld) start.getWorld()).getHandle();

		// Create a new EntityGuardian at the starting location
		final EntityGuardian guardian = new EntityGuardian(world);

		// Set the guardian's beam target using the datawatcher
		guardian.getDataWatcher().watch(17, target.getEntityId());

		// Set the NoAI flag to true using the datawatcher
		guardian.getDataWatcher().watch(15, (byte) 1);

		// TODO  find out why this isn't working
		//guardian.setInvisible(true);
		// get the MobEffectList for the invisible effect
				/*final MobEffectList invisibilityEffect = MobEffectList.INVISIBILITY;
		// create a new MobEffect instance for the invisible effect with infinite duration and amplifier 1
				final MobEffect invisibility = new MobEffect(invisibilityEffect.id, Integer.MAX_VALUE, 1, true, false);
		// add the invisible effect to the guardian's list of active effects
				guardian.addEffect(invisibility);*/


		// Set the location of the fake entity
		guardian.setLocation(start.getX(), start.getY(), start.getZ(), 0, 0);

		// Send a packet to nearby players to make the beam visible
		final PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(guardian);
		final PacketPlayOutEntityEffect packetInvisible = new PacketPlayOutEntityEffect(guardian.getId(), new MobEffect(MobEffectList.INVISIBILITY.id, Integer.MAX_VALUE, 0, false, false)); // TODO
		world.players.stream().filter(p -> p instanceof EntityPlayer).map(p -> (EntityPlayer) p)
				.forEach(p -> {
					// Add the guardian to the player's entity tracker
					p.playerConnection.sendPacket(packet);
					p.playerConnection.sendPacket(packetInvisible); // TODO doesn't work
					// Send a packet to remove the guardian from the player's view
					final PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(guardian.getId());
					Common.runLater(duration * 20, () -> p.playerConnection.sendPacket(destroyPacket));
				});
	}
}

