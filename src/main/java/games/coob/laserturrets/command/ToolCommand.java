package games.coob.laserturrets.command;

import games.coob.laserturrets.menu.ToolsMenu;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.BeamTurretTool;
import games.coob.laserturrets.tools.FireballTurretTool;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class ToolCommand extends SimpleSubCommand {

    ToolCommand() {
        super("tool|tools");

        setDescription(Lang.of("Turret_Commands.Tool_Description"));
        setUsage("<tool> <turret_type> <player>");
        setPermission(Permissions.Command.TOOL);
    }

    /**
     * Perform the main command logic.
     */
    @Override
    protected void onCommand() {
        checkConsole();

        if (args.length == 0)
            new ToolsMenu().displayTo(getPlayer());
        else {
            final String type = args[0];

            if (args.length == 1)
                giveTool(type, getPlayer());
            else if (args.length == 2) {
                final String playerName = args[1];
                final Player player = Bukkit.getPlayer(playerName);

                if (player == null)
                    returnTell(Lang.of("Turret_Commands.Player_Non_Existent"));

                giveTool(type, player);
            } else returnInvalidArgs();
        }
    }

    private void giveTool(final String type, final Player player) {
        if ("arrow".equals(type))
            ArrowTurretTool.getInstance().give(player);
        else if ("beam".equals(type))
            BeamTurretTool.getInstance().give(player);
        else if ("fireball".equals(type))
            FireballTurretTool.getInstance().give(player);
    }

    @Override
    protected List<String> tabComplete() {
        if (this.args.length == 1)
            return this.completeLastWord("arrow", "beam", "fireball");

        if (this.args.length == 2)
            return completeLastWordPlayerNames();

        return NO_COMPLETE;
    }
}
