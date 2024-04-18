package games.coob.laserturrets.settings;

import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.SimpleSettings;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Arrays;
import java.util.List;

/**
 * A sample settings class, utilizing {@link YamlStaticConfig} with prebuilt settings.yml handler
 * with a bunch of preconfigured keys, see resources/settings.yml
 * <p>
 * Foundation detects if you have "settings.yml" placed in your jar (in src/main/resources in source)
 * and will load this class automatically. The same goes for the {@link Lang} class which is
 * automatically loaded when we detect the presence of at least one localization/messages_X.yml
 * file in your jar.
 */
@SuppressWarnings("unused")
public final class Settings extends SimpleSettings {

    /**
     * Place the sections where user can create new "key: value" pairs
     * here so that they are not removed while adding comments.
     * <p>
     * Example use in ChatControl: user can add new channels in "Channels.List"
     * section so we place "Channels.List" here.
     *
     * @return the ignored sections
     */
    @Override
    protected List<String> getUncommentedSections() {
        return Arrays.asList(
                "Example.Uncommented_Section");
    }

    public static class TurretSection {
        public static Boolean DISPLAY_HOLOGRAM;
        public static Boolean DISPLAY_ACTION_BAR;
        public static Integer TURRET_MIN_DISTANCE;
        public static CompParticle CREATION_PARTICLE;
        public static Integer CREATION_PARTICLE_COUNT;
        public static CompParticle PLACEMENT_PARTICLE;
        public static Integer PLACEMENT_PARTICLE_COUNT;
        public static CompSound CREATION_SOUND;
        public static CompSound PLACEMENT_SOUND;
        public static Boolean BUILD_IN_OWN_TERRITORY;
        public static Boolean ALLY_PROTECTION;
        public static List<String> BLACKLISTED_WORLDS;
        public static Boolean ENABLE_TURRET_KILL_MESSAGE;
        public static Boolean REMOVE_DROPS_ON_MOB_KILL;

        /*
         * Automatically called method when we load settings.yml to load values in this subclass
         */
        private static void init() {

            // A convenience method to instruct the loader to prepend all paths with Example so you
            // do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
            setPathPrefix("Turret");

            DISPLAY_HOLOGRAM = getBoolean("Display_Holograms");
            DISPLAY_ACTION_BAR = getBoolean("Display_Action_Bar");
            TURRET_MIN_DISTANCE = getInteger("Turret_Min_Distance");
            CREATION_PARTICLE = get("Creation_Particle", CompParticle.class);
            CREATION_PARTICLE_COUNT = getInteger("Creation_Particle_Count");
            CREATION_SOUND = get("Creation_Sound", CompSound.class);
            PLACEMENT_PARTICLE = get("Placement_Particle", CompParticle.class);
            PLACEMENT_PARTICLE_COUNT = getInteger("Placement_Particle_Count");
            PLACEMENT_SOUND = get("Placement_Sound", CompSound.class);
            ENABLE_TURRET_KILL_MESSAGE = getBoolean("Enable_Turret_Kill_Message");
            REMOVE_DROPS_ON_MOB_KILL = getBoolean("Remove_Drops_On_Mob_Kill");
            BLACKLISTED_WORLDS = getStringList("Blacklisted_Worlds");
            BUILD_IN_OWN_TERRITORY = getBoolean("Build_In_Own_Territory");
            ALLY_PROTECTION = getBoolean("Ally_Protection");
        }
    }

    public static class CurrencySection { // TODO use experience levels
        public static String CURRENCY_NAME;
        public static Double DEFAULT_CURRENCY;
        public static Boolean USE_VAULT;

        /*
         * Automatically called method when we load settings.yml to load values in this subclass
         */
        private static void init() {

            // A convenience method to instruct the loader to prepend all paths with Example so you
            // do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
            setPathPrefix("Currency_Settings");

            USE_VAULT = getBoolean("Use_Vault");
            CURRENCY_NAME = getString("Currency_Name");
            DEFAULT_CURRENCY = getDouble("Default_Currency");
        }
    }

    public static class DatabaseSection {
        public static Boolean ENABLE_MYSQL;
        public static String HOST;
        public static Integer PORT;
        public static String DATABASE;
        public static String USER;
        public static String PASSWORD;

        /*
         * Automatically called method when we load settings.yml to load values in this subclass
         */
        private static void init() {

            // A convenience method to instruct the loader to prepend all paths with Example so you
            // do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
            setPathPrefix("MySQL");

            ENABLE_MYSQL = getBoolean("Enable_MySQL");
            HOST = getString("Hostname");
            PORT = getInteger("Port");
            DATABASE = getString("Database");
            USER = getString("Username");
            PASSWORD = getString("Password");
        }
    }

    /**
     * @see org.mineacademy.fo.settings.SimpleSettings#getConfigVersion()
     */
    @Override
    protected int getConfigVersion() {
        return 4;
    }
}
