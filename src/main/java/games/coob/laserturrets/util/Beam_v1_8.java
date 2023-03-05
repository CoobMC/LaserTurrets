package games.coob.laserturrets.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class Beam_v1_8 {

	protected Location start;
	protected Location end;
	protected int duration;
	protected int distanceSquared;

	public Beam_v1_8(final Location start, final Location end, final int duration, final int distance) {
		if (start.getWorld() != end.getWorld())
			throw new IllegalArgumentException("Locations do not belong to the same worlds.");

		this.start = start;
		this.end = end;
		this.duration = duration;
		distanceSquared = distance < 0 ? -1 : distance * distance;
	}

	private final static Field entityCountField;
	private final static Field entityIdField;
	private final static Field entityTypeField;
	private final static Field posXField;
	private final static Field posYField;
	private final static Field posZField;
	private final static Field dataWatcherField;

	static {
		try {
			entityCountField = Entity.class.getDeclaredField("entityCount");

			final Class<?> psc = PacketPlayOutSpawnEntityLiving.class;
			entityIdField = psc.getDeclaredField("a");
			entityTypeField = psc.getDeclaredField("b");
			posXField = psc.getDeclaredField("c");
			posYField = psc.getDeclaredField("d");
			posZField = psc.getDeclaredField("e");
			dataWatcherField = psc.getDeclaredField("l");

			entityCountField.setAccessible(true);

			entityIdField.setAccessible(true);
			entityTypeField.setAccessible(true);
			posXField.setAccessible(true);
			posYField.setAccessible(true);
			posZField.setAccessible(true);
			dataWatcherField.setAccessible(true);

		} catch (final ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static int getAndRefreshEntityId() throws IllegalAccessException {
		final int entityId = (int) entityCountField.get(null) + 1;
		entityCountField.set(null, entityId);
		return entityId;
	}

	//Create packet
	// a : entityId
	// b: EntityTypes -> Guardian : 68 ; Squid : 94
	// Position : pPos = MathHelper.floor(rPos * 32.0);
	// c : X
	// d : Y
	// e : Z
	// Angle : pAngle = (rAngle * 256.0F / 360.0F))
	// i : yaw
	// j : pitch
	// k : headRotation
	// Motion : -3.9 < rMotion < 3.9 -> pMotion = rMotion * 80000
	// f : motX
	// g : motY
	// h : motZ
	// l : DataWatcher

	private PacketPlayOutSpawnEntityLiving createSpawnPacket(final Location loc, final int entityType) throws IllegalAccessException {
		final PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving();
		entityIdField.set(spawnPacket, getAndRefreshEntityId());
		entityTypeField.set(spawnPacket, entityType);
		posXField.set(spawnPacket, getPacketPos(loc.getX()));
		posYField.set(spawnPacket, getPacketPos(loc.getY()));
		posZField.set(spawnPacket, getPacketPos(loc.getZ()));
		return spawnPacket;
	}

	private int getPacketPos(final double realPos) {
		return MathHelper.floor(realPos * 32.0);
	}

	private void setupGuardianDataWatcher(final PacketPlayOutSpawnEntityLiving spawnPacket, final int targetEntityId) throws IllegalAccessException {
		final DataWatcher dataWatcher = new DataWatcher(null); // We will not invoke methods that need an Entity so we can give 'null' without problems
		// Setup general data. Each bit represent a data property such as invisibility
		final byte CURRENT_DATA_ID = 0;
		final byte currentData = 0;
		final byte INVISIBLE_DATA_FLAG = 5; // The invisibility bit is the fifth
		final byte generalData = (currentData | 1 << INVISIBLE_DATA_FLAG); // Can directly be set as 0b100000 = 2^5 = 32
		dataWatcher.a(CURRENT_DATA_ID, generalData); // DataWatcher#a() -> register an entry with an initial value
		// Set the target
		final byte TARGET_ENTITY_ID = 17;
		dataWatcher.a(TARGET_ENTITY_ID, targetEntityId);
		dataWatcherField.set(spawnPacket, dataWatcher);
	}

	public void spawnGuardian(final LivingEntity entity) throws IllegalAccessException {
		final WorldServer world = ((CraftWorld) this.start.getWorld()).getHandle();
		// Test purpose only
		final int GUARDIAN_ENTITY_TYPE = 68;
		final PacketPlayOutSpawnEntityLiving spawnPacket = createSpawnPacket(this.start.clone().add(2, 0, 0), GUARDIAN_ENTITY_TYPE);

		setupGuardianDataWatcher(spawnPacket, entity.getEntityId());

		world.players.stream().filter(p -> p instanceof EntityPlayer).map(p -> (EntityPlayer) p)
				.forEach(p -> {
					// TODO send packet to all players within radius (add condition)

					// Send a packet to remove the guardian from the player's view
					p.playerConnection.sendPacket(spawnPacket);
					//final PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(guardian.getId()); TODO
					//Common.runLater(duration * 20, () -> p.playerConnection.sendPacket(destroyPacket));
				});

	}
}


