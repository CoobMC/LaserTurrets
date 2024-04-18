package games.coob.laserturrets.command;

import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class GiveCommand extends SimpleSubCommand {

    GiveCommand() {
        super("give");

        setDescription(Lang.of("Turret_Commands.Give_Description"));
        setUsage("<turret|ammo> <turret_type> <amount> <player>");
        setPermission(Permissions.Command.GIVE);
    }

    /**
     * Perform the main command logic.
     */
    @Override
    protected void onCommand() {
        checkConsole();

        if (args.length < 2)
            returnInvalidArgs();

        final String subject = args[0];
        final String type = args[1];

        if (args.length == 2) {
            if (subject.equals("turret")) {
                giveTurret(type, getPlayer(), 1);
            } else if (subject.equals("ammo")) {
                giveAmmo(type, getPlayer(), 1);
            }
        } else if (args.length < 5) {
            final String amountString = args[2];

            if (!canParseInt(amountString))
                returnTell(Lang.of("Turret_Commands.Invalid_Number"));

            final int amount = Integer.parseInt(amountString);

            if (args.length == 4) {
                final String playerName = args[3];
                final Player targetPlayer = Bukkit.getPlayer(playerName);

                if (targetPlayer == null)
                    returnTell(Lang.of("Turret_Commands.Player_Non_Existent"));

                if (subject.equals("turret")) {
                    giveTurret(type, targetPlayer, amount);
                    Messenger.success(getPlayer(), Lang.of("Turret_Commands.Give_Turret_Message", "{playerName}", targetPlayer.getName(), "{amount}", amount));
                } else if (subject.equals("ammo")) {
                    giveAmmo(type, targetPlayer, amount);
                    Messenger.success(getPlayer(), Lang.of("Turret_Commands.Give_Ammo_Message", "{playerName}", targetPlayer.getName(), "{amount}", amount));
                }
            } else {
                if (subject.equals("turret")) {
                    giveTurret(type, getPlayer(), amount);
                    Messenger.success(getPlayer(), Lang.of("Turret_Commands.Give_Turret_Message", "{playerName}", getPlayer().getName(), "{amount}", amount));
                } else if (subject.equals("ammo")) {
                    giveAmmo(type, getPlayer(), amount);
                    Messenger.success(getPlayer(), Lang.of("Turret_Commands.Give_Ammo_Message", "{playerName}", getPlayer().getName(), "{amount}", amount));
                }
            }
        } else returnInvalidArgs();
    }

    private void giveTurret(final String type, final Player player, final int amount) {
        for (int i = 0; i < amount; i++) {
            if ("arrow".equals(type))
                ArrowTurret.getInstance().give(player);
            else if ("beam".equals(type))
                BeamTurret.getInstance().give(player);
            else if ("fireball".equals(type))
                FireballTurret.getInstance().give(player);
        }
    }

    private void giveAmmo(final String ammoType, final Player player, final int amount) {
        final String[] parts = ammoType.split("_", 2);  // Split the string into parts
        final String type = parts[0];
        final ItemStack ammo = TurretSettings.findByName(type).getAmmo().getSecondValue();

        ammo.setAmount(amount);
        PlayerUtil.addItems(player.getInventory(), ammo);
    }

    public boolean canParseInt(final String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected List<String> tabComplete() {
        if (this.args.length == 1)
            return this.completeLastWord("turret", "ammo");

        if (this.args.length == 2)
            return this.completeLastWord("arrow", "beam", "fireball");

        if (this.args.length == 3)
            return this.completeLastWord("<amount>");

        if (this.args.length == 4)
            return completeLastWordPlayerNames();

        return NO_COMPLETE;
    }
}
