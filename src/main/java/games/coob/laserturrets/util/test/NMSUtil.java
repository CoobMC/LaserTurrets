package games.coob.laserturrets.util.test;

import org.bukkit.entity.Player;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMSUtil {

	private NMSUtil() {
	}

	private static Method sendPacket = null;
	private static Method a = null;

	public static Object getHandle(final Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		return object.getClass().getMethod("getHandle").invoke(object);
	}

	public static void sendPacket(final Player player, final Object packet) {
		try {
			final Object handle = getHandle(player);
			final Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (sendPacket == null) {
				sendPacket = playerConnection.getClass().getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
			}
			sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public static void a(final Player player, final Object packet) {
		try {
			final Object handle = getHandle(player);
			final Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (a == null) {
				a = playerConnection.getClass().getMethod("a", packet.getClass());
			}
			a.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	/**
	 * org.bukkit.craftbukkit
	 */
	public static Class<?> getOBCClass(final String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + MinecraftVersion.getServerVersion() + "." + className);
		} catch (final ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static class IChatBaseComponent {

		private static final Logger logger = Logger.getLogger(IChatBaseComponent.class.getName());
		public static final Class<?> IChatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
		private static Method newIChatBaseComponent = null;

		static {
			try {
				newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
			} catch (final NoSuchMethodException e) {
				logger.log(Level.SEVERE, "An error occurred while initializing IChatBaseComponent.");
			}
		}

		public static Object of(final String string) throws InvocationTargetException, IllegalAccessException {
			return newIChatBaseComponent.invoke(null, "{\"text\": \"" + string + "\"}");
		}

	}
}