package games.coob.laserturrets.util.test;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mineacademy.fo.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class HologramUtil { // TODO 12-15

	private static final Class<?> CraftWorld = ReflectionUtil.getOBCClass("CraftWorld"),
			World = ReflectionUtil.getNMSClass("World"),
			EntityArmorStand = ReflectionUtil.getNMSClass("EntityArmorStand"),
			PacketPlayOutSpawnEntityLiving = ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntityLiving"),
			PacketPlayOutEntityDestroy = ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy"),
			PacketPlayOutEntityMetadata = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata"),
			PacketPlayOutEntityTeleport = ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport"),
			Entity = ReflectionUtil.getNMSClass("Entity"),
			DataWatcher = ReflectionUtil.getNMSClass("DataWatcher"),
			EntityLiving = ReflectionUtil.getNMSClass("EntityLiving");

	private static Constructor<?> EntityArmorStandConstructor = null,
			PacketPlayOutSpawnEntityLivingConstructor = null,
			PacketPlayOutEntityDestroyConstructor = null,
			PacketPlayOutEntityMetadataConstructor = null,
			PacketPlayOutEntityTeleportConstructor = null;

	private static Method setInvisible = null, setCustomNameVisible = null,
			setCustomName = null, getId = null, getDataWatcher = null,
			setLocation = null;

	static {
		try {
			EntityArmorStandConstructor = EntityArmorStand.getConstructor(World, double.class, double.class, double.class);
			PacketPlayOutSpawnEntityLivingConstructor = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving);
			PacketPlayOutEntityDestroyConstructor = PacketPlayOutEntityDestroy.getConstructor(int[].class);
			PacketPlayOutEntityMetadataConstructor = PacketPlayOutEntityMetadata.getConstructor(int.class, DataWatcher, boolean.class);
			PacketPlayOutEntityTeleportConstructor = PacketPlayOutEntityTeleport.getConstructor(Entity);
			setInvisible = EntityArmorStand.getMethod("setInvisible", boolean.class);
			setCustomNameVisible = EntityArmorStand.getMethod("setCustomNameVisible", boolean.class);
			setLocation = Entity.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
			try {
				setCustomName = EntityArmorStand.getMethod("setCustomName", String.class);
			} catch (final NoSuchMethodException x) {
				setCustomName = EntityArmorStand.getMethod("setCustomName", NMSUtil.IChatBaseComponent.IChatBaseComponent);
			}
			getId = EntityArmorStand.getMethod("getId");
			getDataWatcher = Entity.getMethod("getDataWatcher");
		} catch (final NoSuchMethodException ignored) {
		}
	}

	private Location location;
	private String text;
	private final Object armorStand;
	private final int id;
	private final Object packetPlayOutSpawnEntityLiving;
	private final Object packetPlayOutEntityDestroy;
	private final Set<Player> viewers = new HashSet<>();

	public HologramUtil(final Location location, final String text) {
		this.location = location;
		this.text = text;
		try {
			this.armorStand = EntityArmorStandConstructor.newInstance(NMSUtil.getHandle(CraftWorld.cast(location.getWorld())), location.getX(), location.getY(), location.getZ());
			setInvisible.invoke(armorStand, true);
			setCustomNameVisible.invoke(armorStand, true);
			if (setCustomName.getParameterTypes()[0].equals(String.class)) {
				setCustomName.invoke(armorStand, text);
			} else {
				setCustomName.invoke(armorStand, NMSUtil.IChatBaseComponent.of(text));
			}
			this.id = (int) getId.invoke(armorStand);
			this.packetPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLivingConstructor.newInstance(armorStand);
			this.packetPlayOutEntityDestroy = PacketPlayOutEntityDestroyConstructor.newInstance((Object) new int[]{id});
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			throw new RuntimeException("An error occurred while creating the hologram.", e);
		}
	}

	public void display(final Player... players) {
		try {
			for (final Player player : players) {
				if (viewers.add(player)) {
					NMSUtil.sendPacket(player, packetPlayOutSpawnEntityLiving);
					updateMetadata(player);
				}
			}
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
		}
	}

	public void hide(final Player... players) {
		for (final Player player : players) {
			if (viewers.remove(player)) {
				NMSUtil.sendPacket(player, packetPlayOutEntityDestroy);
			}
		}
	}

	public void setLocation(final Location location) {
		try {
			setLocation.invoke(armorStand, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			this.location = location;
			updateLocation();
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
		}
	}

	public void setText(final String text) {
		try {
			if (setCustomName.getParameterTypes()[0].equals(String.class)) {
				setCustomName.invoke(armorStand, text);
			} else {
				setCustomName.invoke(armorStand, NMSUtil.IChatBaseComponent.of(text));
			}
			this.text = text;
			updateMetadata();
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException ignored) {
		}
	}

	public Location getLocation() {
		return location;
	}

	public String getText() {
		return text;
	}

	private void updateMetadata() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		final Object packet = PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true);
		for (final Player player : viewers) {
			NMSUtil.sendPacket(player, packet);
		}
	}

	private void updateMetadata(final Player player) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		NMSUtil.sendPacket(player, PacketPlayOutEntityMetadataConstructor.newInstance(id, getDataWatcher.invoke(armorStand), true));
	}

	private void updateLocation() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		for (final Player player : viewers) {
			NMSUtil.sendPacket(player, PacketPlayOutEntityTeleportConstructor.newInstance(armorStand));
		}
	}

}