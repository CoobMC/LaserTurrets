package games.coob.laserturrets.command;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.Permissions;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.command.SimpleSubCommand;

import java.util.List;

/**
 * A sample command belonging to a command group.
 */
final class BuyCommand extends SimpleSubCommand {

    BuyCommand() {
        super("buy");

        setDescription(Lang.of("Turret_Commands.Buy_Description"));
        setUsage("<turret|ammo> <turret_type> <ammo_amount>");
        setPermission(Permissions.Command.BUY);
    }

    /**
     * Perform the main command logic.
     */
    @Override
    protected void onCommand() {
        checkConsole();

        if (args.length == 0 || args.length == 1)
            returnInvalidArgs();

        final String subject = args[0];
        final PlayerCache cache = PlayerCache.from(getPlayer());

        if (args.length >= 2) {
            final String type = args[1];
            final TurretSettings settings = TurretSettings.findByName(type);
            double price = 0;

            if (subject.equals("turret")) {
                price = settings.getLevels().get(0).getPrice();

                if (cache.getCurrency(false) - price < 0) {
                    Messenger.error(getPlayer(), Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));
                    return;
                }

                giveTurret(type, getPlayer());
                Messenger.success(getPlayer(), Lang.of("Turret_Commands.Buy_Turret_Message", "{turretType}", type, "{price}", price, "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
            } else if (subject.equals("ammo")) {
                final String amount = args[2];

                if (!canParseInt(amount))
                    returnTell(Lang.of("Turret_Commands.Invalid_Number"));

                price = settings.getAmmo().getThirdValue() * Integer.parseInt(amount);

                if (cache.getCurrency(false) - price < 0) {
                    Messenger.error(getPlayer(), Lang.of("Turret_Commands.Balance_Cannot_Be_Negative"));
                    return;
                }

                giveAmmo(type, getPlayer(), Integer.parseInt(amount));
                Messenger.success(getPlayer(), Lang.of("Turret_Commands.Buy_Ammo_Message", "{amount}", amount, "{turretType}", type, "{price}", price, "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
            }

            cache.takeCurrency(price, false);
        }
    }

    private void giveTurret(final String type, final Player player) {
        if ("arrow".equals(type))
            ArrowTurret.getInstance().give(player);
        else if ("beam".equals(type))
            BeamTurret.getInstance().give(player);
        else if ("fireball".equals(type))
            FireballTurret.getInstance().give(player);
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

        if (this.args.length == 3 && args[0].equals("ammo")) {
            return this.completeLastWord("<ammo_amount>");
        }

        return NO_COMPLETE;
    }
}
