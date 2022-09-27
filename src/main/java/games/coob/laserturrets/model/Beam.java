package games.coob.laserturrets.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.mineacademy.fo.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A whole class to create Guardian beams using packets and reflection.<br>
 * Inspired by the API <a href="https://www.spigotmc.org/resources/GuardianBeamapi.18329">GuardianBeamAPI</a><br>
 * <b>1.9 -> 1.19</b>
 *
 * @author SkytAsul
 * @version 2.3.0
 * @see <a href="https://github.com/SkytAsul/GuardianBeam">GitHub repository</a>
 */
public abstract class Beam {

	protected final int distanceSquared;
	protected final int duration;
	protected boolean durationInTicks = false;
	protected Location start;
	protected Location end;

	protected Plugin plugin;
	protected BukkitRunnable main;

	protected BukkitTask startMove;
	protected BukkitTask endMove;

	protected Set<Player> show = ConcurrentHashMap.newKeySet();
	private Set<Player> seen = new HashSet<>();

	private List<Runnable> executeEnd = new ArrayList<>(1);

	protected Beam(final Location start, final Location end, final int duration, final int distance) {
		if (!Packets.enabled)
			throw new IllegalStateException("The beam has been disabled. An error has occurred during initialization.");
		if (start.getWorld() != end.getWorld())
			throw new IllegalArgumentException("Locations do not belong to the same worlds.");
		this.start = start;
		this.end = end;
		this.duration = duration;
		distanceSquared = distance < 0 ? -1 : distance * distance;
	}

	/**
	 * Adds a runnable to execute when the beam reaches its final duration
	 *
	 * @param runnable action to execute
	 * @return this {@link Beam} instance
	 */
	public Beam executeEnd(final Runnable runnable) {
		executeEnd.add(runnable);
		return this;
	}

	/**
	 * Makes the duration provided in the constructor passed as ticks and not seconds
	 *
	 * @return this {@link Beam} instance
	 */
	public Beam durationInTicks() {
		durationInTicks = true;
		return this;
	}

	/**
	 * Starts this beam.
	 * <p>
	 * It will make the beam visible for nearby players and start the countdown to the final duration.
	 * <p>
	 * Once finished, it will destroy the beam and execute all runnables passed with {@link Beam#executeEnd}.
	 *
	 * @param plugin plugin used to start the task
	 */
	public void start(final Plugin plugin) {
		if (main != null) throw new IllegalStateException("Task already started");
		this.plugin = plugin;
		main = new BukkitRunnable() {
			int time = 0;

			@Override
			public void run() {
				try {
					if (time == duration) {
						cancel();
						return;
					}
					if (!durationInTicks || time % 20 == 0) {
						for (final Player player : start.getWorld().getPlayers()) {
							if (isCloseEnough(player)) {
								if (show.add(player)) {
									sendStartPackets(player, !seen.add(player));
								}
							} else if (show.remove(player)) {
								sendDestroyPackets(player);
							}
						}
					}
					time++;
				} catch (final ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}

			@Override
			public synchronized void cancel() throws IllegalStateException {
				super.cancel();
				main = null;
				try {
					for (final Player p : show) {
						sendDestroyPackets(p);
					}
					show.clear();
					executeEnd.forEach(Runnable::run);
				} catch (final ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		};
		main.runTaskTimerAsynchronously(plugin, 0L, durationInTicks ? 1L : 20L);
	}

	/**
	 * Stops this beam.
	 * <p>
	 * This will destroy the beam for every player and start execute all runnables passed with {@link Beam#executeEnd}
	 */
	public void stop() {
		if (main == null) throw new IllegalStateException("Task not started");
		main.cancel();
	}

	/**
	 * Gets beam status.
	 *
	 * @return <code>true</code> if the beam is currently running
	 * (i.e. {@link #start} has been called and the duration is not over)
	 */
	public boolean isStarted() {
		return main != null;
	}

	/**
	 * Instantly moves the start of the beam to the location provided.
	 *
	 * @param location New start location
	 * @throws ReflectiveOperationException if a reflection exception occurred during beam moving
	 */
	public abstract void moveStart(Location location) throws ReflectiveOperationException;

	/**
	 * Instantly moves the end of the beam to the location provided.
	 *
	 * @param location New end location
	 * @throws ReflectiveOperationException if a reflection exception occurred during beam moving
	 */
	public abstract void moveEnd(Location location) throws ReflectiveOperationException;

	/**
	 * Gets the start location of the beam.
	 *
	 * @return where exactly is the start position of the beam located
	 */
	public Location getStart() {
		return start;
	}

	/**
	 * Gets the end location of the beam.
	 *
	 * @return where exactly is the end position of the beam located
	 */
	public Location getEnd() {
		return end;
	}

	/**
	 * Moves the start of the beam smoothly to the new location, within a given time.
	 *
	 * @param location New start location to go to
	 * @param ticks    Duration (in ticks) to make the move
	 * @param callback {@link Runnable} to execute at the end of the move (nullable)
	 */
	public void moveStart(final Location location, final int ticks, final Runnable callback) {
		startMove = moveInternal(location, ticks, startMove, this::getStart, this::moveStart, callback);
	}

	/**
	 * Moves the end of the beam smoothly to the new location, within a given time.
	 *
	 * @param location New end location to go to
	 * @param ticks    Duration (in ticks) to make the move
	 * @param callback {@link Runnable} to execute at the end of the move (nullable)
	 */
	public void moveEnd(final Location location, final int ticks, final Runnable callback) {
		endMove = moveInternal(location, ticks, endMove, this::getEnd, this::moveEnd, callback);
	}

	private BukkitTask moveInternal(final Location location, final int ticks, final BukkitTask oldTask, final Supplier<Location> locationSupplier, final ReflectiveConsumer<Location> moveConsumer, final Runnable callback) {
		if (ticks <= 0) throw new IllegalArgumentException("Ticks must be a positive value");
		if (plugin == null) throw new IllegalStateException("The beam must have been started a least once");
		if (oldTask != null && !oldTask.isCancelled()) oldTask.cancel();
		return new BukkitRunnable() {
			double xPerTick = (location.getX() - locationSupplier.get().getX()) / ticks;
			double yPerTick = (location.getY() - locationSupplier.get().getY()) / ticks;
			double zPerTick = (location.getZ() - locationSupplier.get().getZ()) / ticks;
			int elapsed = 0;

			@Override
			public void run() {
				try {
					moveConsumer.accept(locationSupplier.get().add(xPerTick, yPerTick, zPerTick));
				} catch (final ReflectiveOperationException e) {
					e.printStackTrace();
					cancel();
					return;
				}

				if (++elapsed == ticks) {
					cancel();
					if (callback != null) callback.run();
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	protected void moveFakeEntity(final Location location, final int entityId, final Object fakeEntity) throws ReflectiveOperationException {
		if (fakeEntity != null) Packets.moveFakeEntity(fakeEntity, location);
		if (main == null) return;

		final Object packet;
		if (fakeEntity == null) {
			packet = Packets.createPacketMoveEntity(location, entityId);
		} else {
			packet = Packets.createPacketMoveEntity(fakeEntity);
		}
		for (final Player p : show) {
			Packets.sendPackets(p, packet);
		}
	}

	protected abstract void sendStartPackets(Player p, boolean hasSeen) throws ReflectiveOperationException;

	protected abstract void sendDestroyPackets(Player p) throws ReflectiveOperationException;

	protected boolean isCloseEnough(final Player player) {
		if (distanceSquared == -1) return true;
		final Location location = player.getLocation();
		return getStart().distanceSquared(location) <= distanceSquared ||
				getEnd().distanceSquared(location) <= distanceSquared;
	}

	public static class GuardianBeam extends Beam {
		private static AtomicInteger teamID = new AtomicInteger(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));

		private Object createGuardianPacket;
		private Object createSquidPacket;
		private Object teamCreatePacket;
		private Object[] destroyPackets;
		private Object metadataPacketGuardian;
		private Object metadataPacketSquid;
		private Object fakeGuardianDataWatcher;

		private final UUID squidUUID = UUID.randomUUID();
		private final UUID guardianUUID = UUID.randomUUID();
		private final int squidID = Packets.generateEID();
		private final int guardianID = Packets.generateEID();
		private Object squid;
		private Object guardian;

		private int targetID;
		private UUID targetUUID;

		protected LivingEntity endEntity;

		private Location correctStart;
		private Location correctEnd;

		/**
		 * Creates a new Guardian beam instance
		 *
		 * @param start    Location where beam will starts
		 * @param end      Location where beam will ends
		 * @param duration Duration of beam in seconds (<i>-1 if infinite</i>)
		 * @param distance Distance where beam will be visible (<i>-1 if infinite</i>)
		 * @throws ReflectiveOperationException if a reflection exception occurred during beam creation
		 * @see Beam#start(Plugin) to start the beam
		 * @see #durationInTicks() to make the duration in ticks
		 * @see #executeEnd(Runnable) to add Runnable-s to execute when the beam will stop
		 * @see #GuardianBeam(Location, LivingEntity, int, int) to create a beam which follows an entity
		 */
		public GuardianBeam(final Location start, final Location end, final int duration, final int distance) throws ReflectiveOperationException {
			super(start, end, duration, distance);

			initSquid();

			targetID = squidID;
			targetUUID = squidUUID;

			initBeam();
		}

		/**
		 * Creates a new Guardian beam instance
		 *
		 * @param start     Location where beam will starts
		 * @param endEntity Entity who the beam will follow
		 * @param duration  Duration of beam in seconds (<i>-1 if infinite</i>)
		 * @param distance  Distance where beam will be visible (<i>-1 if infinite</i>)
		 * @throws ReflectiveOperationException if a reflection exception occurred during beam creation
		 * @see Beam#start(Plugin) to start the beam
		 * @see #durationInTicks() to make the duration in ticks
		 * @see #executeEnd(Runnable) to add Runnable-s to execute when the beam will stop
		 * @see #GuardianBeam(Location, Location, int, int) to create a beam with a specific end location
		 */
		public GuardianBeam(final Location start, final LivingEntity endEntity, final int duration, final int distance) throws ReflectiveOperationException {
			super(start, endEntity.getEyeLocation(), duration, distance);

			targetID = endEntity.getEntityId();
			targetUUID = endEntity.getUniqueId();

			initBeam();
		}

		private void initBeam() throws ReflectiveOperationException {
			fakeGuardianDataWatcher = Packets.createFakeDataWatcher();
			Packets.initGuardianWatcher(fakeGuardianDataWatcher, targetID);
			if (Packets.version >= 17) {
				guardian = Packets.createGuardian(getCorrectStart(), guardianUUID, guardianID);
			}
			metadataPacketGuardian = Packets.createPacketMetadata(guardianID, fakeGuardianDataWatcher);

			teamCreatePacket = Packets.createPacketTeamCreate("noclip" + teamID.getAndIncrement(), squidUUID, guardianUUID);
			destroyPackets = Packets.createPacketsRemoveEntities(squidID, guardianID);
		}

		private void initSquid() throws ReflectiveOperationException {
			if (Packets.version >= 17) {
				squid = Packets.createSquid(getCorrectEnd(), squidUUID, squidID);
			}
			metadataPacketSquid = Packets.createPacketMetadata(squidID, Packets.fakeSquidWatcher);
			Packets.setDirtyWatcher(Packets.fakeSquidWatcher);
		}

		private Object getGuardianSpawnPacket() throws ReflectiveOperationException {
			if (createGuardianPacket == null) {
				if (Packets.version < 17) {
					createGuardianPacket = Packets.createPacketEntitySpawnLiving(getCorrectStart(), Packets.mappings.getGuardianID(), guardianUUID, guardianID);
				} else {
					createGuardianPacket = Packets.createPacketEntitySpawnLiving(guardian);
				}
			}
			return createGuardianPacket;
		}

		private Object getSquidSpawnPacket() throws ReflectiveOperationException {
			if (createSquidPacket == null) {
				if (Packets.version < 17) {
					createSquidPacket = Packets.createPacketEntitySpawnLiving(getCorrectEnd(), Packets.mappings.getSquidID(), squidUUID, squidID);
				} else {
					createSquidPacket = Packets.createPacketEntitySpawnLiving(squid);
				}
			}
			return createSquidPacket;
		}

		/**
		 * Makes the beam follow an entity (moving end location).
		 * <p>
		 * This is done client-side by making the fake guardian follow the existing entity.
		 * Hence, there is no consuming of server resources.
		 *
		 * @param entity living entity the beam will follow
		 * @throws ReflectiveOperationException if a reflection operation fails
		 */
		public void attachEndEntity(final LivingEntity entity) throws ReflectiveOperationException {
			if (entity.getWorld() != start.getWorld())
				throw new IllegalArgumentException("Attached entity is not in the same world as the beam.");
			this.endEntity = entity;
			setTargetEntity(entity.getUniqueId(), entity.getEntityId());
		}

		public Entity getEndEntity() {
			return endEntity;
		}

		private void setTargetEntity(final UUID uuid, final int id) throws ReflectiveOperationException {
			targetUUID = uuid;
			targetID = id;
			fakeGuardianDataWatcher = Packets.createFakeDataWatcher();
			Packets.initGuardianWatcher(fakeGuardianDataWatcher, targetID);
			metadataPacketGuardian = Packets.createPacketMetadata(guardianID, fakeGuardianDataWatcher);

			for (final Player p : show) {
				Packets.sendPackets(p, metadataPacketGuardian);
			}
		}

		@Override
		public Location getEnd() {
			return endEntity == null ? end : endEntity.getEyeLocation();
		}

		protected Location getCorrectStart() {
			if (correctStart == null) {
				correctStart = start.clone();
				correctStart.subtract(0, 0.5, 0);
			}
			return correctStart;
		}

		protected Location getCorrectEnd() {
			if (correctEnd == null) {
				correctEnd = end.clone();
				correctEnd.subtract(0, 0.5, 0);

				final Vector corrective = correctEnd.toVector().subtract(getCorrectStart().toVector()).normalize();
				correctEnd.subtract(corrective);

			}
			return correctEnd;
		}

		@Override
		protected boolean isCloseEnough(final Player player) {
			return player == endEntity || super.isCloseEnough(player);
		}

		@Override
		protected void sendStartPackets(final Player p, final boolean hasSeen) throws ReflectiveOperationException {
			if (squid == null) {
				Packets.sendPackets(p,
						getGuardianSpawnPacket(),
						metadataPacketGuardian);
			} else {
				Packets.sendPackets(p,
						getGuardianSpawnPacket(),
						getSquidSpawnPacket(),
						metadataPacketGuardian,
						metadataPacketSquid);
			}

			if (!hasSeen) Packets.sendPackets(p, teamCreatePacket);
		}

		@Override
		protected void sendDestroyPackets(final Player p) throws ReflectiveOperationException {
			Packets.sendPackets(p, destroyPackets);
		}

		@Override
		public void moveStart(final Location location) throws ReflectiveOperationException {
			this.start = location;
			correctStart = null;

			createGuardianPacket = null; // will force re-generation of spawn packet
			moveFakeEntity(getCorrectStart(), guardianID, guardian);

			if (squid != null) {
				correctEnd = null;
				createSquidPacket = null;
				moveFakeEntity(getCorrectEnd(), squidID, squid);
			}
		}

		@Override
		public void moveEnd(final Location location) throws ReflectiveOperationException {
			this.end = location;
			createSquidPacket = null; // will force re-generation of spawn packet
			correctEnd = null;

			if (squid == null) {
				initSquid();
				for (final Player p : show) {
					Packets.sendPackets(p, getSquidSpawnPacket(), metadataPacketSquid);
				}
			} else {
				moveFakeEntity(getCorrectEnd(), squidID, squid);
			}
			if (targetUUID != squidUUID) {
				endEntity = null;
				setTargetEntity(squidUUID, squidID);
			}
		}

		/**
		 * Asks viewers' clients to change the color of this beam
		 *
		 * @throws ReflectiveOperationException
		 */
		public void callColorChange() throws ReflectiveOperationException {
			for (final Player p : show) {
				Packets.sendPackets(p, metadataPacketGuardian);
			}
		}

	}

	private static class Packets {
		private static AtomicInteger lastIssuedEID = new AtomicInteger(2000000000);

		static int generateEID() {
			return lastIssuedEID.getAndIncrement();
		}

		private static int version;
		private static int versionMinor;
		private static String npack = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		private static String cpack = Bukkit.getServer().getClass().getPackage().getName() + ".";
		private static ProtocolMappings mappings;

		private static Object squidType;
		private static Object guardianType;

		private static Constructor<?> squidConstructor;
		private static Constructor<?> guardianConstructor;

		private static Object watcherObject1; // invisilibity
		private static Object watcherObject2; // spikes
		private static Object watcherObject3; // attack id

		private static Constructor<?> watcherConstructor;
		private static Method watcherSet;
		private static Method watcherRegister;
		private static Method watcherDirty;

		private static Constructor<?> blockPositionConstructor;

		private static Constructor<?> packetSpawnLiving;
		private static Constructor<?> packetSpawnNormal;
		private static Constructor<?> packetRemove;
		private static Constructor<?> packetTeleport;
		private static Constructor<?> packetMetadata;
		private static Class<?> packetTeam;

		private static Method createTeamPacket;
		private static Constructor<?> createTeam;
		private static Constructor<?> createScoreboard;
		private static Method setTeamPush;
		private static Object pushNever;
		private static Method getTeamPlayers;

		private static Method getHandle;
		private static Field playerConnection;
		private static Method sendPacket;
		private static Method setLocation;
		private static Method setUUID;
		private static Method setID;

		private static Object fakeSquid;
		private static Object fakeSquidWatcher;

		private static Object nmsWorld;

		public static boolean enabled = false;

		static {
			try {
				// e.g. Bukkit.getServer().getClass().getPackage().getName() -> org.bukkit.craftbukkit.v1_17_R1
				String[] versions = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1).split("_");
				version = Integer.parseInt(versions[1]); // 1.X
				if (version >= 17) {
					// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
					versions = Bukkit.getBukkitVersion().split("-R")[0].split("\\.");
					versionMinor = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);
				} else versionMinor = Integer.parseInt(versions[2].substring(1)); // 1.X.Y
				Common.log("Found server version 1." + version + "." + versionMinor);

				mappings = ProtocolMappings.getMappings(version);
				if (mappings == null) {
					mappings = ProtocolMappings.values()[ProtocolMappings.values().length - 1];
					Common.log("Loaded not matching version of the mappings for your server version (1." + version + "." + versionMinor + ")");
				}
				Common.log("Loaded mappings " + mappings.name());

				final Class<?> entityTypesClass = getNMSClass("world.entity", "EntityTypes");
				final Class<?> entityClass = getNMSClass("world.entity", "Entity");
				final Class<?> squidClass = getNMSClass("world.entity.animal", "EntitySquid");
				final Class<?> guardianClass = getNMSClass("world.entity.monster", "EntityGuardian");
				watcherObject1 = getField(entityClass, mappings.getWatcherFlags(), null);
				watcherObject2 = getField(guardianClass, mappings.getWatcherSpikes(), null);
				watcherObject3 = getField(guardianClass, mappings.getWatcherTargetEntity(), null);

				if (version >= 13) {
					squidType = entityTypesClass.getDeclaredField(mappings.getSquidTypeName()).get(null);
					guardianType = entityTypesClass.getDeclaredField(mappings.getGuardianTypeName()).get(null);
				}

				final Class<?> dataWatcherClass = getNMSClass("network.syncher", "DataWatcher");
				watcherConstructor = dataWatcherClass.getDeclaredConstructor(entityClass);
				if (version >= 18) {
					watcherSet = dataWatcherClass.getDeclaredMethod("b", watcherObject1.getClass(), Object.class);
					watcherRegister = dataWatcherClass.getDeclaredMethod("a", watcherObject1.getClass(), Object.class);
				} else {
					watcherSet = getMethod(dataWatcherClass, "set");
					watcherRegister = getMethod(dataWatcherClass, "register");
				}
				if (version >= 15) watcherDirty = getMethod(dataWatcherClass, "markDirty");
				packetSpawnNormal = getNMSClass("network.protocol.game", "PacketPlayOutSpawnEntity").getDeclaredConstructor(version < 17 ? new Class<?>[0] : new Class<?>[]{getNMSClass("world.entity", "Entity")});
				packetSpawnLiving = version >= 19 ? packetSpawnNormal : getNMSClass("network.protocol.game", "PacketPlayOutSpawnEntityLiving").getDeclaredConstructor(version < 17 ? new Class<?>[0] : new Class<?>[]{getNMSClass("world.entity", "EntityLiving")});
				packetRemove = getNMSClass("network.protocol.game", "PacketPlayOutEntityDestroy").getDeclaredConstructor(version == 17 && versionMinor == 0 ? int.class : int[].class);
				packetMetadata = getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata").getDeclaredConstructor(int.class, dataWatcherClass, boolean.class);
				packetTeleport = getNMSClass("network.protocol.game", "PacketPlayOutEntityTeleport").getDeclaredConstructor(version < 17 ? new Class<?>[0] : new Class<?>[]{entityClass});
				packetTeam = getNMSClass("network.protocol.game", "PacketPlayOutScoreboardTeam");

				blockPositionConstructor = getNMSClass("core", "BlockPosition").getConstructor(double.class, double.class, double.class);

				nmsWorld = Class.forName(cpack + "CraftWorld").getDeclaredMethod("getHandle").invoke(Bukkit.getWorlds().get(0));

				squidConstructor = squidClass.getDeclaredConstructors()[0];
				if (version >= 17) {
					guardianConstructor = guardianClass.getDeclaredConstructors()[0];
				}

				final Object[] entityConstructorParams = version < 14 ? new Object[]{nmsWorld} : new Object[]{entityTypesClass.getDeclaredField(mappings.getSquidTypeName()).get(null), nmsWorld};
				fakeSquid = squidConstructor.newInstance(entityConstructorParams);
				fakeSquidWatcher = createFakeDataWatcher();
				tryWatcherSet(fakeSquidWatcher, watcherObject1, (byte) 32);

				getHandle = Class.forName(cpack + "entity.CraftPlayer").getDeclaredMethod("getHandle");
				playerConnection = getNMSClass("server.level", "EntityPlayer").getDeclaredField(version < 17 ? "playerConnection" : "b");
				sendPacket = getNMSClass("server.network", "PlayerConnection").getMethod(version < 18 ? "sendPacket" : "a", getNMSClass("network.protocol", "Packet"));

				if (version >= 17) {
					setLocation = entityClass.getDeclaredMethod(version < 18 ? "setLocation" : "a", double.class, double.class, double.class, float.class, float.class);
					setUUID = entityClass.getDeclaredMethod("a_", UUID.class);
					setID = entityClass.getDeclaredMethod("e", int.class);

					createTeamPacket = packetTeam.getMethod("a", getNMSClass("world.scores", "ScoreboardTeam"), boolean.class);

					final Class<?> scoreboardClass = getNMSClass("world.scores", "Scoreboard");
					final Class<?> teamClass = getNMSClass("world.scores", "ScoreboardTeam");
					final Class<?> pushClass = getNMSClass("world.scores", "ScoreboardTeamBase$EnumTeamPush");
					createTeam = teamClass.getDeclaredConstructor(scoreboardClass, String.class);
					createScoreboard = scoreboardClass.getDeclaredConstructor();
					setTeamPush = teamClass.getDeclaredMethod(mappings.getTeamSetCollision(), pushClass);
					pushNever = pushClass.getDeclaredField("b").get(null);
					getTeamPlayers = teamClass.getDeclaredMethod(mappings.getTeamGetPlayers());
				}

				enabled = true;
			} catch (final Exception e) {
				e.printStackTrace();
				final String errorMsg = "Laser beam reflection failed to initialize. The util is disabled. Please ensure your version (" + Bukkit.getServer().getClass().getPackage().getName() + ") is supported.";
				Common.log(errorMsg);
			}
		}

		public static void sendPackets(final Player p, final Object... packets) throws ReflectiveOperationException {
			final Object connection = playerConnection.get(getHandle.invoke(p));
			for (final Object packet : packets) {
				if (packet == null) continue;
				sendPacket.invoke(connection, packet);
			}
		}

		public static Object createFakeDataWatcher() throws ReflectiveOperationException {
			final Object watcher = watcherConstructor.newInstance(fakeSquid);
			if (version > 13) setField(watcher, "registrationLocked", false);
			return watcher;
		}

		public static void setDirtyWatcher(final Object watcher) throws ReflectiveOperationException {
			if (version >= 15) watcherDirty.invoke(watcher, watcherObject1);
		}

		public static Object createSquid(final Location location, final UUID uuid, final int id) throws ReflectiveOperationException {
			final Object entity = squidConstructor.newInstance(squidType, nmsWorld);
			setEntityIDs(entity, uuid, id);
			moveFakeEntity(entity, location);
			return entity;
		}

		public static Object createGuardian(final Location location, final UUID uuid, final int id) throws ReflectiveOperationException {
			final Object entity = guardianConstructor.newInstance(guardianType, nmsWorld);
			setEntityIDs(entity, uuid, id);
			moveFakeEntity(entity, location);
			return entity;
		}

		public static Object createPacketEntitySpawnLiving(final Location location, final int typeID, final UUID uuid, final int id) throws ReflectiveOperationException {
			final Object packet = packetSpawnLiving.newInstance();
			setField(packet, "a", id);
			setField(packet, "b", uuid);
			setField(packet, "c", typeID);
			setField(packet, "d", location.getX());
			setField(packet, "e", location.getY());
			setField(packet, "f", location.getZ());
			setField(packet, "j", (byte) (location.getYaw() * 256.0F / 360.0F));
			setField(packet, "k", (byte) (location.getPitch() * 256.0F / 360.0F));
			if (version <= 14) setField(packet, "m", fakeSquidWatcher);
			return packet;
		}

		public static Object createPacketEntitySpawnNormal(final Location location, final int typeID, final Object type, final int id) throws ReflectiveOperationException {
			final Object packet = packetSpawnNormal.newInstance();
			setField(packet, "a", id);
			setField(packet, "b", UUID.randomUUID());
			setField(packet, "c", location.getX());
			setField(packet, "d", location.getY());
			setField(packet, "e", location.getZ());
			setField(packet, "i", (int) (location.getYaw() * 256.0F / 360.0F));
			setField(packet, "j", (int) (location.getPitch() * 256.0F / 360.0F));
			setField(packet, "k", version < 13 ? typeID : type);
			return packet;
		}

		public static Object createPacketEntitySpawnLiving(final Object entity) throws ReflectiveOperationException {
			return packetSpawnLiving.newInstance(entity);
		}

		public static Object createPacketEntitySpawnNormal(final Object entity) throws ReflectiveOperationException {
			return packetSpawnNormal.newInstance(entity);
		}

		public static void initGuardianWatcher(final Object watcher, final int targetId) throws ReflectiveOperationException {
			tryWatcherSet(watcher, watcherObject1, (byte) 32);
			tryWatcherSet(watcher, watcherObject2, Boolean.FALSE);
			tryWatcherSet(watcher, watcherObject3, targetId);
		}

		public static Object[] createPacketsRemoveEntities(final int... entitiesId) throws ReflectiveOperationException {
			final Object[] packets;
			if (version == 17 && versionMinor == 0) {
				packets = new Object[entitiesId.length];
				for (int i = 0; i < entitiesId.length; i++) {
					packets[i] = packetRemove.newInstance(entitiesId[i]);
				}
			} else {
				packets = new Object[]{packetRemove.newInstance(entitiesId)};
			}
			return packets;
		}

		public static Object createPacketMoveEntity(final Location location, final int entityId) throws ReflectiveOperationException {
			final Object packet = packetTeleport.newInstance();
			setField(packet, "a", entityId);
			setField(packet, "b", location.getX());
			setField(packet, "c", location.getY());
			setField(packet, "d", location.getZ());
			setField(packet, "e", (byte) (location.getYaw() * 256.0F / 360.0F));
			setField(packet, "f", (byte) (location.getPitch() * 256.0F / 360.0F));
			setField(packet, "g", true);
			return packet;
		}

		public static void setEntityIDs(final Object entity, final UUID uuid, final int id) throws ReflectiveOperationException {
			setUUID.invoke(entity, uuid);
			setID.invoke(entity, id);
		}

		public static void moveFakeEntity(final Object entity, final Location location) throws ReflectiveOperationException {
			setLocation.invoke(entity, location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
		}

		public static Object createPacketMoveEntity(final Object entity) throws ReflectiveOperationException {
			return packetTeleport.newInstance(entity);
		}

		public static Object createPacketTeamCreate(final String teamName, final UUID... entities) throws ReflectiveOperationException {
			final Object packet;
			if (version < 17) {
				packet = packetTeam.newInstance();
				setField(packet, "a", teamName);
				setField(packet, "i", 0);
				setField(packet, "f", "never");
				final Collection<String> players = (Collection<String>) getField(packetTeam, "h", packet);
				for (final UUID entity : entities) players.add(entity.toString());
			} else {
				final Object team = createTeam.newInstance(createScoreboard.newInstance(), teamName);
				setTeamPush.invoke(team, pushNever);
				final Collection<String> players = (Collection<String>) getTeamPlayers.invoke(team);
				for (final UUID entity : entities) players.add(entity.toString());
				packet = createTeamPacket.invoke(null, team, true);
			}
			return packet;
		}

		private static Object createPacketMetadata(final int entityId, final Object watcher) throws ReflectiveOperationException {
			return packetMetadata.newInstance(entityId, watcher, false);
		}

		private static void tryWatcherSet(final Object watcher, final Object watcherObject, final Object watcherData) throws ReflectiveOperationException {
			try {
				watcherSet.invoke(watcher, watcherObject, watcherData);
			} catch (final InvocationTargetException ex) {
				watcherRegister.invoke(watcher, watcherObject, watcherData);
				if (version >= 15) watcherDirty.invoke(watcher, watcherObject);
			}
		}

		/* Reflection utils */
		private static Method getMethod(final Class<?> clazz, final String name) throws NoSuchMethodException {
			for (final Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(name)) return m;
			}
			throw new NoSuchMethodException(name + " in " + clazz.getName());
		}

		private static void setField(final Object instance, final String name, final Object value) throws ReflectiveOperationException {
			final Field field = instance.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(instance, value);
		}

		private static Object getField(final Class<?> clazz, final String name, final Object instance) throws ReflectiveOperationException {
			final Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(instance);
		}

		private static Class<?> getNMSClass(final String package17, final String className) throws ClassNotFoundException {
			return Class.forName((version < 17 ? npack : "net.minecraft." + package17) + "." + className);
		}

		private enum ProtocolMappings {
			V1_9(9, "Z", "bA", "bB", "b", "c", 94, 68),
			V1_10(10, V1_9),
			V1_11(11, V1_10),
			V1_12(12, V1_11),
			V1_13(13, "ac", "bF", "bG", "b", "c", 70, 28),
			V1_14(14, "W", "b", "bD", "c", "d", 73, 30),
			V1_15(15, "T", "b", "bA", "c", "d", 74, 31),
			V1_16(16, null, "b", "d", "c", "d", -1, 31) {
				@Override
				public int getSquidID() {
					return Packets.versionMinor < 2 ? 74 : 81;
				}

				@Override
				public String getWatcherFlags() {
					return Packets.versionMinor < 2 ? "T" : "S";
				}
			},
			V1_17(17, "Z", "b", "e", "c", "d", 86, 35, "K", "aJ", "u", "setCollisionRule", "getPlayerNameSet"),
			V1_18(18, null, "b", "e", "c", "d", 86, 35, "K", "aJ", "u", "a", "g") {
				@Override
				public String getWatcherFlags() {
					return Packets.versionMinor < 2 ? "aa" : "Z";
				}
			},
			V1_19(19, "Z", "b", "e", "c", "d", 89, 38, "N", "aM", "w", "a", "g"),
			;

			private final int major;
			private final String watcherFlags;
			private final String watcherSpikes;
			private final String watcherTargetEntity;
			private final String watcherTargetLocation;
			private final String watcherBasePlate;
			private final int squidID;
			private final int guardianID;
			private final String guardianTypeName;
			private final String squidTypeName;
			private final String crystalTypeName;
			private final String teamSetCollision;
			private final String teamGetPlayers;

			private ProtocolMappings(final int major, final ProtocolMappings parent) {
				this(major, parent.watcherFlags, parent.watcherSpikes, parent.watcherTargetEntity, parent.watcherTargetLocation, parent.watcherBasePlate, parent.squidID, parent.guardianID, parent.guardianTypeName, parent.squidTypeName, parent.crystalTypeName, parent.teamSetCollision, parent.teamGetPlayers);
			}

			private ProtocolMappings(final int major,
									 final String watcherFlags, final String watcherSpikes, final String watcherTargetEntity, final String watcherTargetLocation, final String watcherBasePlate,
									 final int squidID, final int guardianID) {
				this(major, watcherFlags, watcherSpikes, watcherTargetEntity, watcherTargetLocation, watcherBasePlate, squidID, guardianID, null, "SQUID", "END_CRYSTAL", null, null);
			}

			private ProtocolMappings(final int major,
									 final String watcherFlags, final String watcherSpikes, final String watcherTargetEntity, final String watcherTargetLocation, final String watcherBasePlate,
									 final int squidID, final int guardianID,
									 final String guardianTypeName, final String squidTypeName, final String crystalTypeName, final String teamSetCollision, final String teamGetPlayers) {
				this.major = major;
				this.watcherFlags = watcherFlags;
				this.watcherSpikes = watcherSpikes;
				this.watcherTargetEntity = watcherTargetEntity;
				this.watcherTargetLocation = watcherTargetLocation;
				this.watcherBasePlate = watcherBasePlate;
				this.squidID = squidID;
				this.guardianID = guardianID;
				this.guardianTypeName = guardianTypeName;
				this.squidTypeName = squidTypeName;
				this.crystalTypeName = crystalTypeName;
				this.teamSetCollision = teamSetCollision;
				this.teamGetPlayers = teamGetPlayers;
			}

			public int getMajor() {
				return major;
			}

			public String getWatcherFlags() {
				return watcherFlags;
			}

			public String getWatcherSpikes() {
				return watcherSpikes;
			}

			public String getWatcherTargetEntity() {
				return watcherTargetEntity;
			}

			public String getWatcherTargetLocation() {
				return watcherTargetLocation;
			}

			public String getWatcherBasePlate() {
				return watcherBasePlate;
			}

			public int getSquidID() {
				return squidID;
			}

			public int getGuardianID() {
				return guardianID;
			}

			public String getGuardianTypeName() {
				return guardianTypeName;
			}

			public String getSquidTypeName() {
				return squidTypeName;
			}

			public String getCrystalTypeName() {
				return crystalTypeName;
			}

			public String getTeamSetCollision() {
				return teamSetCollision;
			}

			public String getTeamGetPlayers() {
				return teamGetPlayers;
			}

			public static ProtocolMappings getMappings(final int major) {
				for (final ProtocolMappings map : values()) {
					if (major == map.getMajor()) return map;
				}
				return null;
			}
		}
	}

	@FunctionalInterface
	public static interface ReflectiveConsumer<T> {
		abstract void accept(T t) throws ReflectiveOperationException;
	}

}