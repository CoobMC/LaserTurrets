package games.coob.laserturrets.model;

import org.mineacademy.fo.command.PermsCommand;
import org.mineacademy.fo.command.annotation.Permission;
import org.mineacademy.fo.command.annotation.PermissionGroup;

/**
 * A sample permissions class. This is the preferred way of keeping all permissions
 * of your plugin in one place.
 * <p>
 * You will also be able to use the {@link PermsCommand} to list them automatically
 * if you choose to this class.
 */
public final class Permissions {

	/**
	 * A sample permission group for your convenience. The {@link PermissionGroup}
	 * is used in the {@link PermsCommand} for your convenience automatically.
	 */
	@PermissionGroup("View the permission details.")
	public static final class Command {

		@Permission("Gives access to '/lt turret take <id>' to take turrets back into your inventory.")
		public static final String TAKE = "turret.command.take";

		@Permission("Gives access to '/lt turret remove <id>' to remove existing turrets.")
		public static final String REMOVE = "turret.command.remove";

		@Permission("Gives access to '/lt turret buy <turretType>' to buy turrets.")
		public static final String BUY = "turret.command.buy";

		@Permission("Gives access to '/lt turret give <turretType> <player>' to give turrets to yourself or another player.")
		public static final String GIVE = "turret.command.give";

		@Permission("Gives access to '/lt turret tool <turretType> <player>' to give a turret tool to yourself or another player.")
		public static final String TOOL = "turret.command.tool";

		@Permission("This is the main menu that allows you to view all turrets and edit them specifically.")
		public static final String MENU = "turret.command.menu";

		@Permission("Get, set,  give or take a specific amount of the currency from players.")
		public static final String CURRENCY = "turret.command.balance";

		@Permission("Executes a command to open the portal menu.")
		public static final String SHOP = "turret.command.shop";

		@Permission("Executes a command to open the players allies menu.")
		public static final String ALLIES = "turret.command.allies";

		@Permission("Executes a command to open the portal menu.")
		public static final String SETTINGS = "turret.command.settings";
	}
}
