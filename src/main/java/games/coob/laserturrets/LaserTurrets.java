package games.coob.laserturrets;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.HologramHook;
import games.coob.laserturrets.hook.HookSystem;
import games.coob.laserturrets.hook.VaultHook;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.UnplacedData;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.settings.TurretType;
import games.coob.laserturrets.task.ArrowTask;
import games.coob.laserturrets.task.BeamTask;
import games.coob.laserturrets.task.FireballTask;
import games.coob.laserturrets.task.LaserPointerTask;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.Arrays;
import java.util.function.Function;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin { // TODO prevent buttons from being taken in the loot menu

    /**
     * Automatically perform login ONCE when the plugin starts.
     */
    @Override
    protected void onPluginStart() {
        Common.setLogPrefix("[LaserTurrets]");

        for (final String type : getTypes()) {
            final TurretType turretType = findEnum(TurretType.class, type, null, "No such turret type. Available: " + Arrays.toString(getTypes()) + ".");
            TurretSettings.createTurretType(type, turretType);

            final TurretSettings settings = TurretSettings.findByName(type);

            if (settings.getToolItem() == null) {
                final ItemStack itemStack = SkullCreator.itemFromBase64(settings.getHeadTexture());
                settings.setToolItem(itemStack);
            }

            if (settings.getAmmo() == null)
                settings.createAmmo(false, new ItemStack(CompMaterial.SNOWBALL.getMaterial()), 1.0);
        }

        if (!VaultHook.setupEconomy(getServer()) && Settings.CurrencySection.USE_VAULT) {
            Common.log("Disabled due to no Vault dependency found (an economy plugin is also required)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Settings.DatabaseSection.ENABLE_MYSQL)
            TurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);

        if (this.isEnabled())
            new UpdateChecker(this, 105494).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version))
                    Common.log("There is a new update available (v" + version + ").");
            });

        // Example usage
        final String message = Lang.of("Settings_Menu.Head_Texture_Prompt_Message", "{turretType}", TurretUtil.getDisplayName("arrow"));
        System.out.println("Localized Message: " + message);
    }

    @Override
    protected void onPluginReload() {
        Sequence.reload();
        //Hologram.deleteAll();
    }

    @Override
    protected void onPluginStop() {
        Sequence.reload();
        //Hologram.deleteAll();
    }

    public String[] getTypes() { // TODO get from database
        return new String[]{
                "arrow", "beam", "fireball"
        };
    }

    /**
     * Automatically perform login when the plugin starts and each time it is reloaded.
     */
    @Override
    protected void onReloadablesStart() {
        TurretData.loadTurrets();
        UnplacedData.loadTurrets();
        //
        // Add your own plugin parts to load automatically here
        // Please see @AutoRegister for parts you do not have to register manually
        //
        // Load our dependency system
        try {
            HookSystem.loadDependencies();
        } catch (final Throwable throwable) {
            Common.throwError(throwable, "Error while loading " + this.getDataFolder().getName() + " dependencies!");
        }

        Common.runTimer(20, new ArrowTask());
        Common.runTimer(25, new FireballTask());
        Common.runTimer(30, new BeamTask());
        Common.runTimer(2, new LaserPointerTask());

        Common.runLater(10, () -> {
            for (final TurretData turretData : TurretData.getTurrets()) {
                TurretUtil.updateTexture(turretData);
            }
        });

        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms") && Settings.TurretSection.DISPLAY_HOLOGRAM) {
            Common.log("Disabled do to DecentHolograms not being installed or not enabled.");
            this.setEnabled(false);
        }

        if (Settings.TurretSection.DISPLAY_HOLOGRAM) {
            for (final TurretData turretData : TurretData.getTurrets()) {
                final Hologram hologram = DHAPI.getHologram(turretData.getId());

                if (hologram == null)
                    HologramHook.createHologram(turretData);
            }
        } else {
            for (final TurretData turretData : TurretData.getTurrets()) {
                final Hologram hologram = DHAPI.getHologram(turretData.getId());

                if (hologram != null)
                    hologram.delete();

            }
        }
    }

    private <T extends Enum<T>> T findEnum(final Class<T> enumType, final String name, final Function<T, Boolean> condition, final String falseMessage) throws CommandException {
        T found = null;

        try {
            found = ReflectionUtil.lookupEnum(enumType, name);

            if (!condition.apply(found))
                found = null;

        } catch (final Throwable t) {
            // Not found, pass through below to error out
        }

        Valid.checkNotNull(found, falseMessage.replace("{enum}", name).replace("{available}", Common.join(enumType.getEnumConstants())));
        return found;
    }

    /* ------------------------------------------------------------------------------- */
    /* Static */
    /* ------------------------------------------------------------------------------- */

    /**
     * Return the instance of this plugin, which simply refers to a static
     * field already created for you in SimplePlugin but casts it to your
     * specific plugin instance for your convenience.
     *
     * @return
     */
    public static LaserTurrets getInstance() {
        return (LaserTurrets) SimplePlugin.getInstance();
    }
}
