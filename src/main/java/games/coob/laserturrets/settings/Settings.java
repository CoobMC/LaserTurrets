package games.coob.laserturrets.settings;

import org.mineacademy.fo.settings.Lang;
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

	public static class CurrencySection { // TODO integrate vault economy
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

	public static class DefaultLevel1TurretSection {
		public static Double PRICE;
		public static Boolean ENABLE_LASERS;
		public static Double LASER_DAMAGE;
		public static Integer TURRET_RANGE;

		/*
		 * Automatically called method when we load settings.yml to load values in this subclass
		 */
		private static void init() {

			// A convenience method to instruct the loader to prepend all paths with Example so you
			// do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
			setPathPrefix("Default_Level_1_Turret_Settings");

			PRICE = getDouble("Price");
			ENABLE_LASERS = getBoolean("Enable_Lasers");
			LASER_DAMAGE = getDouble("Laser_Damage");
			TURRET_RANGE = getInteger("Turret_Range");
		}
	}

	public static class DefaultLevel2TurretSection {
		public static Double PRICE;
		public static Boolean ENABLE_LASERS;
		public static Double LASER_DAMAGE;
		public static Integer TURRET_RANGE;

		/*
		 * Automatically called method when we load settings.yml to load values in this subclass
		 */
		private static void init() {

			// A convenience method to instruct the loader to prepend all paths with Example so you
			// do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
			setPathPrefix("Default_Level_2_Turret_Settings");

			PRICE = getDouble("Price");
			ENABLE_LASERS = getBoolean("Enable_Lasers");
			LASER_DAMAGE = getDouble("Laser_Damage");
			TURRET_RANGE = getInteger("Turret_Range");
		}
	}

	public static class DefaultLevel3TurretSection {
		public static Double PRICE;
		public static Boolean ENABLE_LASERS;
		public static Double LASER_DAMAGE;
		public static Integer TURRET_RANGE;

		/*
		 * Automatically called method when we load settings.yml to load values in this subclass
		 */
		private static void init() {

			// A convenience method to instruct the loader to prepend all paths with Example so you
			// do not have to call "Example.Key1", "Example.Key2" all the time, only "Key1" and "Key2".
			setPathPrefix("Default_Level_3_Turret_Settings");

			PRICE = getDouble("Price");
			ENABLE_LASERS = getBoolean("Enable_Lasers");
			LASER_DAMAGE = getDouble("Laser_Damage");
			TURRET_RANGE = getInteger("Turret_Range");
		}
	}

	/**
	 * @see org.mineacademy.fo.settings.SimpleSettings#getConfigVersion()
	 */
	@Override
	protected int getConfigVersion() {
		return 1;
	}
}
