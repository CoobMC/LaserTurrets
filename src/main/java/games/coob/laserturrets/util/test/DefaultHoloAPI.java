package games.coob.laserturrets.util.test;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Janhektor
 * @version 1.4 (December 30, 2015)
 */
public class DefaultHoloAPI implements HoloAPI { // TODO 1.8
	private final List<Object> destroyCache;
	private final List<Object> spawnCache;
	private final List<UUID> players;
	private static final double ABS = 0.23D;
	private static Class<?> nmsEntity;
	private static Class<?> craftWorld;
	private static Class<?> packetClass;
	private static Class<?> entityLivingClass;
	private static Constructor<?> armorStandConstructor;

	private static Constructor<?> destroyPacketConstructor;
	/*
	 * Cache for sendPacket()-Method
	 */
	private static Class<?> nmsPacket;

	static {
		final String path = Bukkit.getServer().getClass().getPackage().getName();
		final String version = path.substring(path.lastIndexOf(".") + 1);

		try {
			/*
			 * Cache for getPacket()-Method
			 */
			final Class<?> armorStand = Class.forName("net.minecraft.server." + version + ".EntityArmorStand");
			final Class<?> worldClass = Class.forName("net.minecraft.server." + version + ".World");
			nmsEntity = Class.forName("net.minecraft.server." + version + ".Entity");
			craftWorld = Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
			packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutSpawnEntityLiving");
			entityLivingClass = Class.forName("net.minecraft.server." + version + ".EntityLiving");
			armorStandConstructor = armorStand.getConstructor(worldClass);

			/*
			 * Cache for getDestroyPacket()-Method
			 */
			final Class<?> destroyPacketClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutEntityDestroy");
			destroyPacketConstructor = destroyPacketClass.getConstructor(int[].class);

			nmsPacket = Class.forName("net.minecraft.server." + version + ".Packet");
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
			System.err.println("Error - Classes not initialized!");
			ex.printStackTrace();
		}
	}

	public DefaultHoloAPI(final Location loc, final String... lines) {
		this(loc, Arrays.asList(lines));
	}

	public DefaultHoloAPI(final Location loc, final List<String> lines) {
		this.players = new ArrayList<>();
		this.spawnCache = new ArrayList<>();
		this.destroyCache = new ArrayList<>();

		// Init
		final Location displayLoc = loc.clone().add(0, (ABS * lines.size()) - 1.97D, 0);
		for (final String line : lines) {
			final Object packet = this.getPacket(loc.getWorld(), displayLoc.getX(), displayLoc.getY(), displayLoc.getZ(), line);
			this.spawnCache.add(packet);
			try {
				final Field field = packetClass.getDeclaredField("a");
				field.setAccessible(true);
				this.destroyCache.add(this.getDestroyPacket((int) field.get(packet)));
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			displayLoc.add(0, ABS * (-1), 0);
		}
	}

	@Override
	public boolean display(final Player p) {
		for (final Object o : this.spawnCache) {
			this.sendPacket(p, o);
		}

		this.players.add(p.getUniqueId());
		return true;
	}

	@Override
	public boolean destroy(final Player p) {
		if (this.players.contains(p.getUniqueId())) {
			for (final Object o : this.destroyCache) {
				this.sendPacket(p, o);
			}
			this.players.remove(p.getUniqueId());
			return true;
		}
		return false;
	}

	private Object getPacket(final World w, final double x, final double y, final double z, final String text) {
		try {
			final Object craftWorldObj = craftWorld.cast(w);
			final Method getHandleMethod = craftWorldObj.getClass().getMethod("getHandle");
			final Object entityObject = armorStandConstructor.newInstance(getHandleMethod.invoke(craftWorldObj));
			final Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
			setCustomName.invoke(entityObject, text);
			final Method setCustomNameVisible = nmsEntity.getMethod("setCustomNameVisible", boolean.class);
			setCustomNameVisible.invoke(entityObject, true);
			final Method setGravity = entityObject.getClass().getMethod("setGravity", boolean.class);
			setGravity.invoke(entityObject, false);
			final Method setLocation = entityObject.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
			setLocation.invoke(entityObject, x, y, z, 0.0F, 0.0F);
			final Method setInvisible = entityObject.getClass().getMethod("setInvisible", boolean.class);
			setInvisible.invoke(entityObject, true);
			final Constructor<?> cw = packetClass.getConstructor(entityLivingClass);
			return cw.newInstance(entityObject);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Object getDestroyPacket(final int... id) {
		try {
			return destroyPacketConstructor.newInstance((Object) id);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void sendPacket(final Player p, final Object packet) {
		try {
			final Method getHandle = p.getClass().getMethod("getHandle");
			final Object entityPlayer = getHandle.invoke(p);
			final Object pConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
			final Method sendMethod = pConnection.getClass().getMethod("sendPacket", nmsPacket);
			sendMethod.invoke(pConnection, packet);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}