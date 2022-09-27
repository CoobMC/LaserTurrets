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

		@Permission("Chose a tool to start creating some turrets!")
		public static final String TOOL = "turret.command.tool";

		@Permission("This is the main menu that allows you to view all turrets and edit them specifically.")
		public static final String MENU = "turret.command.menu";

		@Permission("Get, set,  give or take a specific amount of the currency from players.")
		public static final String CURRENCY = "turret.command.currency";

		@Permission("Executes a command to open the portal menu.")
		public static final String SHOP = "turret.command.shop";

		@Permission("Executes a command to open the portal menu.")
		public static final String SETTINGS = "turret.command.settings";
	}
}
