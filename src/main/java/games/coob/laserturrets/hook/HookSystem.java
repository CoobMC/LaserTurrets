package games.coob.laserturrets.hook;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.booksaw.betterTeams.Team;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import konquest.api.KonquestAPI;
import konquest.api.manager.KonquestGuildManager;
import konquest.api.manager.KonquestKingdomManager;
import konquest.api.model.KonquestKingdom;
import konquest.api.model.KonquestTerritory;
import konquest.api.model.KonquestTown;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.LandWorld;
import me.angeschossen.lands.api.relations.Relation;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.interfaces.UClans;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.group.model.relationships.StandardRelationAttribute;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.player.KingdomPlayer;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.lang.reflect.Constructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HookSystem {

	// ------------------------------------------------------------------------------------------------------------
	// Store hook classes separately below, avoiding no such method/field errors
	// ------------------------------------------------------------------------------------------------------------

	private static WorldGuardHook worldguardHook;

	private static TownyHook townyHook;

	private static KingdomsHook kingdomsHook;

	private static LandsHook landsHook;

	private static SimpleClansHook simpleClansHook;

	private static BetterTeamsHook betterTeamsHook;

	private static SaberFactionsHook saberFactionsHook;

	private static FactionsUUIDHook factionsUUIDHook;

	private static UltimateClansHook ultimateClansHook;

	private static KonquestHook konquestHook;

	//private static MedievalFactionsHook medievalFactionsHook;

	private static ResidenceHook residenceHook;

	// ------------------------------------------------------------------------------------------------------------
	// Main loading method
	// ------------------------------------------------------------------------------------------------------------

	public static void loadDependencies() {
		if (Common.doesPluginExist("WorldGuard"))
			worldguardHook = new WorldGuardHook();

		if (Common.doesPluginExist("Towny"))
			townyHook = new TownyHook();

		if (Common.doesPluginExist("KingdomsX"))
			kingdomsHook = new KingdomsHook();

		if (Common.doesPluginExist("Lands"))
			landsHook = new LandsHook();

		if (Common.doesPluginExist("SimpleClans"))
			simpleClansHook = new SimpleClansHook();

		if (Common.doesPluginExist("BetterTeams"))
			betterTeamsHook = new BetterTeamsHook();

		if (Common.doesPluginExist("SaberFactions"))
			saberFactionsHook = new SaberFactionsHook();

		if (Common.doesPluginExist("Factions"))
			factionsUUIDHook = new FactionsUUIDHook();

		if (Common.doesPluginExist("UltimateClans"))
			ultimateClansHook = new UltimateClansHook();

		if (Common.doesPluginExist("Konquest"))
			konquestHook = new KonquestHook();

		/*if (Common.doesPluginExist("MedievalFactions"))
			medievalFactionsHook = new MedievalFactionsHook();*/

		if (Common.doesPluginExist("Residence"))
			residenceHook = new ResidenceHook();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Methods for determining which plugins were loaded after you call the load method
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isWorldGuardLoaded() {
		return worldguardHook != null;
	}

	public static boolean isTownyLoaded() {
		return townyHook != null;
	}

	public static boolean isKingdomsXLoaded() {
		return kingdomsHook != null;
	}

	public static boolean isLandsLoaded() {
		return landsHook != null;
	}

	public static boolean isSimpleClansLoaded() {
		return simpleClansHook != null;
	}

	public static boolean isBetterTeamsLoaded() {
		return betterTeamsHook != null;
	}

	public static boolean isSaberFactionsLoaded() {
		return saberFactionsHook != null;
	}

	public static boolean isFactionsUUIDLoaded() {
		return factionsUUIDHook != null;
	}

	public static boolean isUltimateClansLoaded() {
		return ultimateClansHook != null;
	}

	public static boolean isKonquestLoaded() {
		return konquestHook != null;
	}

	/*public static boolean isMedievalFactionsLoaded() {
		return medievalFactionsHook != null;
	}*/

	public static boolean isResidenceLoaded() {
		return residenceHook != null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Main methods
	// ------------------------------------------------------------------------------------------------------------

	public static boolean canBuild(final Location location, final Player player) {
		return canBuildInRegion(location, player) || canBuildKonquest(location, player) || canBuildInResidence(location, player) || /*canBuildInMedievalFaction(location, player) ||*/ canPlaceInTown(location.getBlock(), player) || canPlaceInSaberFaction(location, player) || canPlaceInFaction(location, player) || canPlaceInLand(location, player) || canPlaceInKingdom(location, player);
	}

	public static boolean isAlly(final Location location, final Player target, final OfflinePlayer turretOwner) {
		return isTownAlly(location, target) || isKingdomAlly(location, target) || isLandAlly(location, target) || isClanAlly(turretOwner, target) || isTeamAlly(turretOwner, target) || isSaberFactionAlly(location, target) || isFactionAlly(location, target) || isUltimateClanAlly(turretOwner, target) || /*isMedievalFactionAlly(location, target) ||*/ isKonquestAlly(location, target) || isResidenceAlly(location, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// WorldGuard
	// ------------------------------------------------------------------------------------------------------------

	public static boolean canBuildInRegion(final Location location, final Player player) {
		return isWorldGuardLoaded() && worldguardHook.canBuildInRegion(location, player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Towny
	// ------------------------------------------------------------------------------------------------------------

	public static boolean canPlaceInTown(final Block block, final Player player) {
		return isTownyLoaded() && townyHook.canPlaceInTown(block, player);
	}

	public static boolean isTownAlly(final Location location, final Player target) {
		return isTownyLoaded() && townyHook.isTownAlly(location, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// KingdomsX
	// ------------------------------------------------------------------------------------------------------------

	public static boolean canPlaceInKingdom(final Location location, final Player player) {
		return isKingdomsXLoaded() && kingdomsHook.canPlaceInKingdom(location, player);
	}

	public static boolean isKingdomAlly(final Location location, final Player target) {
		return isKingdomsXLoaded() && kingdomsHook.isKingdomAlly(location, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Lands
	// ------------------------------------------------------------------------------------------------------------

	public static boolean canPlaceInLand(final Location location, final Player player) {
		return isLandsLoaded() && landsHook.canPlaceInLand(player, location);
	}

	public static boolean isLandAlly(final Location location, final Player target) {
		return isLandsLoaded() && landsHook.isLandAlly(location, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// SimpleClans
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isClanAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		return isSimpleClansLoaded() && simpleClansHook.isClanAlly(turretOwner, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// BetterTeams
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isTeamAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		return isBetterTeamsLoaded() && betterTeamsHook.isTeamAlly(turretOwner, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// SaberFactions
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isSaberFactionAlly(final Location location, final OfflinePlayer target) {
		return isSaberFactionsLoaded() && saberFactionsHook.isFactionAlly(location, target);
	}

	public static boolean canPlaceInSaberFaction(final Location location, final Player player) {
		return isSaberFactionsLoaded() && saberFactionsHook.canPlaceInFaction(location, player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// FactionsUUID
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isFactionAlly(final Location location, final OfflinePlayer target) {
		return isFactionsUUIDLoaded() && factionsUUIDHook.isFactionAlly(location, target);
	}

	public static boolean canPlaceInFaction(final Location location, final Player player) {
		return isFactionsUUIDLoaded() && factionsUUIDHook.canPlaceInFaction(location, player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// UltimateClans
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isUltimateClanAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		return isUltimateClansLoaded() && ultimateClansHook.isClanAlly(turretOwner, target);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Medieval Factions
	// ------------------------------------------------------------------------------------------------------------

	/*public static boolean isMedievalFactionAlly(final Location location, final OfflinePlayer target) {
		return isMedievalFactionsLoaded() && medievalFactionsHook.isFactionAlly(location, target);
	}

	public static boolean canBuildInMedievalFaction(final Location location, final Player player) {
		return isMedievalFactionsLoaded() && medievalFactionsHook.canBuildInFaction(location, player);
	}*/

	// ------------------------------------------------------------------------------------------------------------
	// Konquest
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isKonquestAlly(final Location location, final OfflinePlayer target) {
		return isKonquestLoaded() && konquestHook.isKonquestAlly(location, target);
	}

	public static boolean canBuildKonquest(final Location location, final Player player) {
		return isKonquestLoaded() && konquestHook.canBuildKonquest(location, player);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Residence
	// ------------------------------------------------------------------------------------------------------------

	public static boolean isResidenceAlly(final Location location, final Player target) {
		return isResidenceLoaded() && residenceHook.isResidenceAlly(location, target);
	}

	public static boolean canBuildInResidence(final Location location, final Player player) {
		return isResidenceLoaded() && residenceHook.canPlaceInResidence(location, player);
	}
}

class WorldGuardHook {
	public boolean canBuildInRegion(final Location location, final Player player) {
		final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		final com.sk89q.worldedit.util.Location location1 = BukkitAdapter.adapt(location);
		final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		final RegionQuery query = container.createQuery();
		final World world = BukkitAdapter.adapt(player.getWorld());
		final boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, world);

		if (canBypass)
			return true;

		return query.testState(location1, localPlayer, Flags.BUILD);
	}
}

class TownyHook {
	public boolean canPlaceInTown(final Block block, final Player player) {
		return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.BUILD);
	}

	public boolean isTownAlly(final Location townLocation, final Player target) {
		final Town town = TownyAPI.getInstance().getTown(townLocation);
		final Town targetTown = TownyAPI.getInstance().getTown(target);

		if (town != null && targetTown != null)
			return town.hasAlly(targetTown);

		return false;
	}
}

class KingdomsHook {
	public boolean canPlaceInKingdom(final Location location, final Player player) {
		final Land land = Land.getLand(location);

		if (land != null) {
			if (land.isClaimed()) {
				final Kingdom kingdom = land.getKingdom();
				final KingdomPlayer damagerKp = KingdomPlayer.getKingdomPlayer(player);
				final Kingdom damagerKingdom = damagerKp.getKingdom();

				if (kingdom == null)
					return true;

				return kingdom.hasAttribute(damagerKingdom, StandardRelationAttribute.BUILD);
			}
		}

		return true;
	}

	public boolean isKingdomAlly(final Location location, final Player target) {
		final org.kingdoms.constants.land.Land land = org.kingdoms.constants.land.Land.getLand(location);

		if (land == null)
			return false;

		final Kingdom kingdom = land.getKingdom();
		final KingdomPlayer kingdomEnemy = KingdomPlayer.getKingdomPlayer(target);
		final Kingdom kingdom2 = kingdomEnemy.getKingdom();

		if (kingdom == null)
			return false;

		return kingdom.getRelationWith(kingdom2) == KingdomRelation.ALLY;
	}
}

class LandsHook {
	public boolean canPlaceInLand(final Player player, final Location location) {
		final LandsIntegration api = LandsIntegration.of(SimplePlugin.getInstance());
		final LandWorld world = api.getWorld(player.getWorld());

		if (world != null) // Lands is enabled in this world
			return world.hasRoleFlag(player.getUniqueId(), location, me.angeschossen.lands.api.flags.type.Flags.BLOCK_PLACE);

		return true;
	}

	public boolean isLandAlly(final Location landLocation, final Player target) {
		final LandsIntegration api = LandsIntegration.of(SimplePlugin.getInstance());
		final Area area = api.getArea(landLocation);

		if (area == null)
			return false;

		final me.angeschossen.lands.api.land.Land land = area.getLand();
		final Relation relation = land.getRelation(target.getUniqueId());

		return relation == Relation.ALLY;
	}
}

class SimpleClansHook {
	public boolean isClanAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		//final UUID playerUuid = player.getUniqueId();
		final ClanManager cm = SimpleClans.getInstance().getClanManager();
		// Get a ClanPlayer object
		final ClanPlayer cp = cm.getClanPlayer(turretOwner);
		final ClanPlayer clanEnemy = cm.getClanPlayer(target.getUniqueId());

		if (cp != null && clanEnemy != null) {
			final Clan clan = cp.getClan();

			if (clan != null)
				return clan.isAlly(clanEnemy.getTag());
		}

		return false;
	}
}

class BetterTeamsHook {
	public boolean isTeamAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		final Team team = Team.getTeamManager().getTeam(turretOwner);
		final Team enemyTeam = Team.getTeamManager().getTeam(target);

		if (team == null || enemyTeam == null)
			return false;

		return team.isAlly(enemyTeam.getID()) || team.getMembers().contains(target);
	}
}

class SaberFactionsHook {
	public boolean canPlaceInFaction(final Location location, final Player player) {
		try {
			final Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
			final Object fPlayersInstance = fPlayersClass.getMethod("getInstance").invoke(null);
			final Object fPlayer = fPlayersClass.getMethod("getByPlayer", Player.class).invoke(fPlayersInstance, player);

			final Class<?> fLocationClass = Class.forName("com.massivecraft.factions.FLocation");
			final Constructor<?> fLocationConstructor = fLocationClass.getConstructor(Location.class);
			final Object fLocation = fLocationConstructor.newInstance(location);

			final Class<?> boardClass = Class.forName("com.massivecraft.factions.Board");
			final Object boardInstance = boardClass.getMethod("getInstance").invoke(null);
			final Object factionAtLocation = boardClass.getMethod("getFactionAt", fLocationClass).invoke(boardInstance, fLocation);

			final Class<?> relationClass = Class.forName("com.massivecraft.factions.struct.Relation");
			final Object relation = fPlayer.getClass().getMethod("getRelationTo", factionAtLocation.getClass()).invoke(fPlayer, factionAtLocation);

			return relation == relationClass.getField("MEMBER").get(null);
		} catch (final Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	public boolean isFactionAlly(final Location location, final OfflinePlayer target) {
		try {
			final Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
			final Object fPlayersInstance = fPlayersClass.getMethod("getInstance").invoke(null);
			final Object fPlayer2 = fPlayersClass.getMethod("getByOfflinePlayer", OfflinePlayer.class).invoke(fPlayersInstance, target);

			final Class<?> fLocationClass = Class.forName("com.massivecraft.factions.FLocation");
			final Constructor<?> fLocationConstructor = fLocationClass.getConstructor(Location.class);
			final Object fLocation = fLocationConstructor.newInstance(location);

			final Class<?> boardClass = Class.forName("com.massivecraft.factions.Board");
			final Object boardInstance = boardClass.getMethod("getInstance").invoke(null);
			final Object factionAtLocation = boardClass.getMethod("getFactionAt", fLocationClass).invoke(boardInstance, fLocation);

			final Object factionTarget = fPlayer2.getClass().getMethod("getFaction").invoke(fPlayer2);

			final Class<?> relationClass = Class.forName("com.massivecraft.factions.struct.Relation");
			final Object relation = factionAtLocation.getClass().getMethod("getRelationTo", factionTarget.getClass()).invoke(factionAtLocation, factionTarget);

			return relation == relationClass.getField("ALLY").get(null) ||
					relation == relationClass.getField("MEMBER").get(null) ||
					relation == relationClass.getField("TRUCE").get(null);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

class FactionsUUIDHook {
	public boolean canPlaceInFaction(final Location location, final Player player) {
		try {
			final Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
			final Object fPlayersInstance = fPlayersClass.getMethod("getInstance").invoke(null);
			final Object fPlayer = fPlayersClass.getMethod("getByPlayer", Player.class).invoke(fPlayersInstance, player);

			final Class<?> fLocationClass = Class.forName("com.massivecraft.factions.FLocation");
			final Constructor<?> fLocationConstructor = fLocationClass.getConstructor(Location.class);
			final Object fLocation = fLocationConstructor.newInstance(location);

			final Class<?> boardClass = Class.forName("com.massivecraft.factions.Board");
			final Object boardInstance = boardClass.getMethod("getInstance").invoke(null);
			final Object factionAtLocation = boardClass.getMethod("getFactionAt", fLocationClass).invoke(boardInstance, fLocation);

			final Class<?> relationClass = Class.forName("com.massivecraft.factions.perms.Relation");
			final Object relation = fPlayer.getClass().getMethod("getRelationTo", factionAtLocation.getClass()).invoke(fPlayer, factionAtLocation);

			return relation == relationClass.getField("MEMBER").get(null);
		} catch (final Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	public boolean isFactionAlly(final Location location, final OfflinePlayer target) {
		try {
			final Class<?> fPlayersClass = Class.forName("com.massivecraft.factions.FPlayers");
			final Object fPlayersInstance = fPlayersClass.getMethod("getInstance").invoke(null);
			final Object fPlayer2 = fPlayersClass.getMethod("getByOfflinePlayer", OfflinePlayer.class).invoke(fPlayersInstance, target);

			final Class<?> fLocationClass = Class.forName("com.massivecraft.factions.FLocation");
			final Constructor<?> fLocationConstructor = fLocationClass.getConstructor(Location.class);
			final Object fLocation = fLocationConstructor.newInstance(location);

			final Class<?> boardClass = Class.forName("com.massivecraft.factions.Board");
			final Object boardInstance = boardClass.getMethod("getInstance").invoke(null);
			final Object factionAtLocation = boardClass.getMethod("getFactionAt", fLocationClass).invoke(boardInstance, fLocation);

			final Object factionTarget = fPlayer2.getClass().getMethod("getFaction").invoke(fPlayer2);

			final Class<?> relationClass = Class.forName("com.massivecraft.factions.perms.Relation");
			final Object relation = factionAtLocation.getClass().getMethod("getRelationTo", factionTarget.getClass()).invoke(factionAtLocation, factionTarget);

			return relation == relationClass.getField("ALLY").get(null) ||
					relation == relationClass.getField("MEMBER").get(null) ||
					relation == relationClass.getField("TRUCE").get(null);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

class UltimateClansHook {
	public boolean isClanAlly(final OfflinePlayer turretOwner, final OfflinePlayer target) {
		if (Bukkit.getPluginManager().isPluginEnabled("UltimateClans")) {
			final UClans clan = (UClans) Bukkit.getPluginManager().getPlugin("UltimateClans");

			if (clan != null) {
				final ClanAPIManager manager = clan.getClanAPI();

				return manager.isAlly(turretOwner.getUniqueId(), target.getUniqueId());
			}
		}

		return false;
	}
}

/*class MedievalFactionsHook {
	public boolean canBuildInFaction(final Location location, final Player player) {
		final Plugin medievalFactionsPlugin = Bukkit.getPluginManager().getPlugin("MedievalFactions");

		if (medievalFactionsPlugin instanceof MedievalFactions) {
			final MedievalFactions medievalFactions = (MedievalFactions) medievalFactionsPlugin;
			final Services services = medievalFactions.getServices();
			final MfPlayerService playerService = services.getPlayerService();
			final MfPlayer mfPlayer = playerService.getPlayerByBukkitPlayer(player);

			if (mfPlayer == null)
				return true;

			final MfClaimService claimService = services.getClaimService();
			final MfClaimedChunk claimedChunk = claimService.getClaim(player.getWorld(), (int) location.getX(), (int) location.getZ());

			return claimService.isInteractionAllowedForPlayerInChunk(mfPlayer.getId(), claimedChunk);
		}

		return true;
	}

	public boolean isFactionAlly(final Location location, final OfflinePlayer target) {
		final Plugin medievalFactionsPlugin = Bukkit.getPluginManager().getPlugin("MedievalFactions");

		if (medievalFactionsPlugin instanceof MedievalFactions) {
			final MedievalFactions medievalFactions = (MedievalFactions) medievalFactionsPlugin;
			final Services services = medievalFactions.getServices();
			final MfPlayerService playerService = services.getPlayerService();
			//final MfPlayer mfPlayer = playerService.getPlayerByBukkitPlayer(turretOwner);
			final MfPlayer mfTarget = playerService.getPlayerByBukkitPlayer(target);
			final MfClaimService claimService = services.getClaimService();
			final org.bukkit.World world = location.getWorld();

			if (world == null)
				return false;

			final String factionId = claimService.getClaim(world, (int) location.getX(), (int) location.getZ()).getFactionId();

			if (factionId == null || mfTarget == null)
				return false;

			final MfFactionService factionService = services.getFactionService();
			//final MfFaction faction = factionService.getFactionByFactionId(factionId); // getFactionByPlayerId(mfPlayer.getId());
			final MfFaction targetFaction = factionService.getFactionByPlayerId(mfTarget.getId());
			final MfFactionRelationshipService relationshipService = services.getFactionRelationshipService();

			return relationshipService.getAlliesByFactionId(factionId).contains(targetFaction.getId());
		}

		return false;
	}
}*/

class KonquestHook {

	private final KonquestAPI konquestAPI;

	KonquestHook() {
		final RegisteredServiceProvider<KonquestAPI> provider = Bukkit.getServicesManager().getRegistration(KonquestAPI.class);
		this.konquestAPI = provider.getProvider();
	}

	public boolean isKonquestAlly(final Location location, final OfflinePlayer target) {
		final KonquestGuildManager guildManager = this.konquestAPI.getGuildManager();
		final KonquestKingdomManager kingdomManager = this.konquestAPI.getKingdomManager();
		final KonquestKingdom konquestKingdom = kingdomManager.getChunkTerritory(location).getKingdom();
		final KonquestKingdom targetKingdom = guildManager.getPlayerGuild(target).getKingdom();

		return konquestKingdom.equals(targetKingdom);
	}

	public boolean canBuildKonquest(final Location location, final Player player) {
		final KonquestKingdomManager kingdomManager = this.konquestAPI.getKingdomManager();
		final KonquestGuildManager guildManager = this.konquestAPI.getGuildManager();
		final KonquestTerritory territory = kingdomManager.getChunkTerritory(location);

		if (territory == null)
			return true;

		final KonquestKingdom konquestKingdom = kingdomManager.getChunkTerritory(location).getKingdom();
		final KonquestKingdom targetKingdom = guildManager.getPlayerGuild(player).getKingdom();


		if (konquestKingdom.equals(targetKingdom)) {
			return this.konquestAPI.getPlotManager().isPlayerPlotProtectBuild(territory.getKingdom().getTown(""), location, player); // TODO get town
		} else {
			if (konquestKingdom.isOfflineProtected())
				return false;

			if (territory instanceof KonquestTown) {
				final KonquestTown town = (KonquestTown) territory;
				return !town.isTownWatchProtected();
			}
		}

		return true;
	}
}

class ResidenceHook {
	public boolean isResidenceAlly(final Location location, final Player target) {
		final ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);

		if (residence != null)
			return residence.isTrusted(target);

		return false;
	}

	public boolean canPlaceInResidence(final Location location, final Player player) {
		final ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);

		return rPlayer.canPlaceBlock(location.getBlock(), true);
	}
}




