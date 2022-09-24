package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.DebugCommand;
import org.mineacademy.fo.command.PermsCommand;
import org.mineacademy.fo.command.ReloadCommand;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.model.SimpleComponent;

import java.util.List;

/**
 * A sample command group. A command group is a collection of commands,
 * such as for the Boss plugin, the command group is /boss, where
 * subcommands can be "spawn", "remove" etc. Example:
 * <p>
 * /boss spawn
 * /boss remove
 * (etc..)
 */
@AutoRegister
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TurretCommandGroup extends SimpleCommandGroup {

	/**
	 * The instance of this class, hidden because the only call to this class is from
	 * our auto registration class.
	 */
	@Getter(value = AccessLevel.PRIVATE)
	private static final TurretCommandGroup instance = new TurretCommandGroup();

	/**
	 * @see org.mineacademy.fo.command.SimpleCommandGroup#getHeaderPrefix()
	 */
	@Override
	protected String getHeaderPrefix() {
		return "" + ChatColor.DARK_RED + ChatColor.BOLD;
	}

	// Change this to edit the default message shown when you type the main
	// command group label, here: /plugintemplate
	@Override
	protected List<SimpleComponent> getNoParamsHeader() {
		return super.getNoParamsHeader();
	}

	// Change this to remove "Visit MineAcademy" link we by default have for our plugins
	@Override
	protected String getCredits() {
		return "&7Join our discord server for help.";
	}

	// Change this to edit the messages at the top of our help command, defaults to
	// typing "/plugintemplate ?" or "/plugintemplate help" (you can change ?/help by
	// overriding "getHelpLabel()")
	@Override
	protected String[] getHelpHeader() {
		return super.getHelpHeader();
	}

	/**
	 * @see org.mineacademy.fo.command.SimpleCommandGroup#registerSubcommands()
	 */
	@Override
	protected void registerSubcommands() {

		// Register a sample command for this group
		registerSubcommand(new SettingsCommand());
		registerSubcommand(new ShopCommand());
		registerSubcommand(new ToolsCommand());
		registerSubcommand(new MenuCommand());
		registerSubcommand(new CurrencyCommand());

		// Register the premade commands from Foundation
		registerSubcommand(new DebugCommand("laserturrets.command.debug"));
		registerSubcommand(new ReloadCommand("laserturrets.command.reload"));
		registerSubcommand(new PermsCommand(Permissions.class, "laserturrets.command.perms"));
	}
}