package games.coob.laserturrets.util.test;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Public Hologram utility by ParaPhoenix aka Phoenix1123.
 * Original credit goes to Janhektor on the Spigot forums.
 * https://www.spigotmc.org/members/janhektor.14134/
 * <p>
 * Feel free to use this however you see fit, and I hope that it is
 * of use. ^-^
 * <p>
 * To stay up to date with the latest version, you can find it on my Gist here:
 * https://gist.github.com/Phoenix1123/4a264e530368f96a435df7b0e3ae65fa
 * <p>
 * A brief explanation on how this class works for those who may not know or are wondering,
 * it uses reflection to access CB/NMS classes, which use an invisible armor stand to create floating names
 * (known as Holograms). These armor stands cannot be interacted through, so please do take note of this,
 * it may be a future feature however it is not an aim for this utility as of current.
 * <p>
 * If you do wish, you can remove this message however the update link would be useful!
 * <p>
 * Thank you! o/
 */
public class Hologram { // TODO 1.10+

	private static String version;
	private static Class<?> craftWorld, entityClass, nmsWorld, armorStand, entityLiving, spawnPacket, removePacket;

	static {
		version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

		try {
			craftWorld = Class.forName("org.bukkit.craftbukkit." + version + "CraftWorld");
			entityClass = Class.forName("net.minecraft.server." + version + "Entity");
			nmsWorld = Class.forName("net.minecraft.server." + version + "World");
			armorStand = Class.forName("net.minecraft.server." + version + "EntityArmorStand");
			entityLiving = Class.forName("net.minecraft.server." + version + "EntityLiving");
			spawnPacket = Class.forName("net.minecraft.server." + version + "PacketPlayOutSpawnEntityLiving");
			removePacket = Class.forName("net.minecraft.server." + version + "PacketPlayOutEntityDestroy");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private Location location;
	private List<String> lines = new ArrayList<>();
	private List<Integer> ids = new ArrayList<>();
	private List<Object> entities = new ArrayList<>();
	private double offset = 0.23D;

	public Hologram(final Location location, final String... text) {
		this.location = location;
		addLine(text);
	}

	public Hologram(final String... text) {
		this(null, text);
	}

	public Hologram(final Location location) {
		this(location, null);
	}

	public Hologram() {
		this(null, null);
	}

	/**
	 * Returns the CB/NMS version string. For example v1_10_R1
	 *
	 * @return - The CB/NMS version.
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * Add a line or multiple, character colours will be converted.
	 *
	 * @param text - The text to add.
	 */
	public void addLine(final String... text) {
		lines.addAll(Arrays.asList(text));
		update();
	}

	/**
	 * Returns a List of the lines the hologram is displaying.
	 *
	 * @return
	 */
	public List<String> getLines() {
		return lines;
	}

	/**
	 * Sets the hologram lines, removing any currently lines previously added.
	 *
	 * @param text
	 */
	public void setLines(final String... text) {
		lines = Arrays.asList(text);
		update();
	}

	/**
	 * Return the current stored location of the Hologram.
	 *
	 * @return - The current hologram location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Set the location of the hologram.
	 *
	 * @param location - The location to set.
	 */
	public void setLocation(final Location location) {
		this.location = location;
		update();
	}

	public void teleport(final Location loc) {
		update();
	}

	/**
	 * Display the hologram to a player or multiple
	 *
	 * @param players - The players to show the hologram to.
	 */
	public void displayTo(final Player... players) {
		final Location current = location.clone().add(0, (offset * lines.size()) - 1.97D, 0);

		for (final String str : lines) {
			final Object[] packet = getCreatePacket(location, ChatColor.translateAlternateColorCodes('&', str));
			ids.add((Integer) packet[1]);

			for (final Player player : players)
				sendPacket(player, packet[0]);

			current.subtract(0, offset, 0);
		}
	}

	/**
	 * Delete a hologram from a player or multiple.
	 *
	 * @param players
	 */
	public void removeFrom(final Player... players) {
		Object packet = null;

		for (final int id : ids)
			packet = getRemovePacket(id);

		for (final Player player : players)
			if (packet != null)
				sendPacket(player, packet);
	}

	/**
	 * Spawn the hologram for everyone to see.
	 */
	public void spawn() {
		final Location current = location.clone().add(0, (offset * lines.size()) - 1.97D, 0).add(0, offset, 0);

		for (final String str : lines)
			spawnHologram(ChatColor.translateAlternateColorCodes('&', str), current.subtract(0, offset, 0));
	}

	/**
	 * Spawns a hologram with -text- at -location-
	 */
	private void spawnHologram(final String text, final Location location) {
		try {
			// The ArmorStand
			final Object craftWorld = Hologram.craftWorld.cast(location.getWorld());
			final Object entityObject = armorStand.getConstructor(nmsWorld).newInstance(Hologram.craftWorld.getMethod("getHandle").invoke(craftWorld));

			configureHologram(entityObject, text, location);

			Hologram.craftWorld.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class).invoke(craftWorld, entityObject, CreatureSpawnEvent.SpawnReason.CUSTOM);

			entities.add(entityObject);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Delete the hologram from the world.
	 */
	public void remove() {
		for (final Object ent : entities)
			removeEntity(ent);
	}

	private void removeEntity(final Object entity) {
		try {
			final Object craftWorld = Hologram.craftWorld.cast(location.getWorld());

			nmsWorld.getMethod("removeEntity", entityClass).invoke(Hologram.craftWorld.getMethod("getHandle").invoke(craftWorld), entity);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Get the packet for creating a new Hologram, using EntityArmorStands and PacketPlayOutSpawnEntityLiving
	 *
	 * @param location - The location for which to spawn the hologram.
	 * @param text     - The text (entity name) of the hologram.
	 * @return Object - The PacketPlayOutSpawnEntityLiving packet in the form of an Object (Because of reflection, duh ^^)
	 */
	private Object[] getCreatePacket(final Location location, final String text) {
		try {
			// The ArmorStand
			final Object entityObject = armorStand.getConstructor(nmsWorld).newInstance(craftWorld.getMethod("getHandle").invoke(craftWorld.cast(location.getWorld())));
			final Object id = entityObject.getClass().getMethod("getId").invoke(entityObject);

			configureHologram(entityObject, text, location);

			// Return the packet, and the entity id so we can later remove it.
			return new Object[]{spawnPacket.getConstructor(entityLiving).newInstance(entityObject), id};
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the removal packet for the hologram.
	 *
	 * @param id - The entity ID to remove (ArmorStand)
	 * @return The destroy packet object.
	 */
	private Object getRemovePacket(final int id) {
		try {
			final Class<?> packet = Class.forName("net.minecraft.server." + version + "PacketPlayOutEntityDestroy");
			return packet.getConstructor(int[].class).newInstance(new int[]{id});
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Updates the currently existant hologram.
	 */
	public void update() {
		try {
			if (!entities.isEmpty()) { // spawned as an actual entity, moving is ezpz.

				for (int i = 0; i < entities.size(); i++) {
					final Object ent = entities.get(i);

					if (i > lines.size() - 1) // 1 'hologram' per line
						removeEntity(ent);
				}

				final Location current = location.clone().add(0, (offset * lines.size()) - 1.97D, 0);

				for (int i = 0; i < lines.size(); i++) {
					final String text = ChatColor.translateAlternateColorCodes('&', lines.get(i));

					if (i >= entities.size()) {
						spawnHologram(text, current);
					} else {
						configureHologram(entities.get(i), text, current);
					}

					current.subtract(0, offset, 0);
				}

			} else { // TODO allow the user to update packet holograms

			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Configures the hologram properties.
	 *
	 * @param entityObject - The EntityArmorStand object to modify.
	 * @param text         - The text the hologram has.
	 * @throws Exception
	 */
	private void configureHologram(final Object entityObject, final String text, final Location location) throws Exception {
		// Methods for modifying the properties
		final Method setCustomName = entityObject.getClass().getMethod("setCustomName", String.class);
		final Method setCustomNameVisible = entityObject.getClass().getMethod("setCustomNameVisible", boolean.class);
		final Method setNoGravity = entityObject.getClass().getMethod("setNoGravity", boolean.class); // Previously setGravity(boolean) prior to 1.10
		final Method setLocation = entityObject.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
		final Method setInvisible = entityObject.getClass().getMethod("setInvisible", boolean.class);

		// Setting the properties
		setCustomName.invoke(entityObject, text);
		setCustomNameVisible.invoke(entityObject, true);
		setNoGravity.invoke(entityObject, true);
		setLocation.invoke(entityObject, location.getX(), location.getY(), location.getZ(), 0.0F, 0.0F);
		setInvisible.invoke(entityObject, true);
	}

	/**
	 * Send a packet to a player.
	 *
	 * @param player
	 * @param packet
	 */
	private void sendPacket(final Player player, final Object packet) {
		try {
			if (packet == null)
				return;

			final Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
			final Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
			connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + "Packet")).invoke(connection, packet);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

}