package games.coob.laserturrets;

import games.coob.laserturrets.database.TurretsDatabase;
import games.coob.laserturrets.hook.HookSystem;
import games.coob.laserturrets.hook.VaultHook;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.sequence.Sequence;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.settings.TurretType;
import games.coob.laserturrets.task.*;
import games.coob.laserturrets.util.Hologram;
import games.coob.laserturrets.util.SkullCreator;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * PluginTemplate is a simple template you can use every time you make
 * a new plugin. This will save you time because you no longer have to
 * recreate the same skeleton and features each time.
 * <p>
 * It uses Foundation for fast and efficient development process.
 */
public final class LaserTurrets extends SimplePlugin {

	/**
	 * Automatically perform login ONCE when the plugin starts.
	 */
	@Override
	protected void onPluginStart() {
		Common.setLogPrefix("[LaserTurrets]");

		if (YamlConfig.fromFileFast(FileUtil.getFile("data.db")).isSet("Turrets")) {
			convert();
			TurretData.loadTurrets();
		}

		for (final String type : getTypes()) {
			final TurretType turretType = findEnum(TurretType.class, type, null, "No such such turret type. Available: " + Arrays.toString(getTypes()) + ".");
			TurretSettings.createTurretType(type, turretType);

			final TurretSettings settings = TurretSettings.findByName(type);

			if (settings.getToolItem() == null) {
				final ItemStack itemStack = SkullCreator.itemFromBase64(settings.getHeadTexture());
				settings.setToolItem(itemStack);
			}
		}

		if (!VaultHook.setupEconomy(getServer()) && Settings.CurrencySection.USE_VAULT) {
			Common.log("Disabled due to no Vault dependency found (an economy plugin is also required)!", getDescription().getName());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (Settings.DatabaseSection.ENABLE_MYSQL)
			TurretsDatabase.getInstance().connect(Settings.DatabaseSection.HOST, Settings.DatabaseSection.PORT, Settings.DatabaseSection.DATABASE, Settings.DatabaseSection.USER, Settings.DatabaseSection.PASSWORD);

		new UpdateChecker(this, 105494).getVersion(version -> {
			if (!this.getDescription().getVersion().equals(version))
				Common.log("There is a new update available (v" + version + ").");
		});
	}

	private void convert() { // TODO remove Turrets key if set in data.db
		final Path source = Paths.get(SimplePlugin.getInstance().getDataFolder().getPath(), "turrets");
		final Path target = Paths.get(SimplePlugin.getInstance().getDataFolder().getPath(), "types");

		try {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			Common.log("Folder renamed successfully from 'turrets' to 'types'.");
		} catch (final IOException e) {
			Common.log("An error occurred while renaming the folder 'turrets': " + e.getMessage());
		}

		final File dataFile = FileUtil.getFile("data.db");
		final YamlConfig datadb = YamlConfig.fromFileFast(dataFile);
		final Set<SerializedMap> turrets = datadb.getSet("Turrets", SerializedMap.class);

		for (final SerializedMap turret : turrets) {
			final File turretFile = FileUtil.getOrMakeFile("turrets/" + turret.getString("Id") + ".yml"); // TODO get id
			final YamlConfig turretConfig = YamlConfig.fromFile(turretFile);

			turretConfig.set("Block", turret.getString("Block"));
			turretConfig.set("Id", turret.getString("Id"));
			turretConfig.set("Type", turret.getString("Type"));
			turretConfig.set("Owner", turret.get("Owner", UUID.class));
			turretConfig.set("Player_Blacklist", turret.getSet("Player_Blacklist", UUID.class));
			turretConfig.set("Mob_Blacklist", turret.getSet("Mob_Blacklist", EntityType.class));
			turretConfig.set("Current_Loot", turret.getList("Current_Loot", ItemStack.class));
			turretConfig.set("Use_Player_Whitelist", turret.getBoolean("Use_Player_Whitelist"));
			turretConfig.set("Use_Mob_Whitelist", turret.getBoolean("Use_Mob_Whitelist"));
			turretConfig.set("Current_Level", turret.getInteger("Current_Level"));
			turretConfig.set("Current_Health", turret.getDouble("Current_Health"));
			turretConfig.set("Broken", turret.getBoolean("Broken"));

			turretConfig.save();
		}

		datadb.set("Turrets", null);
		datadb.save(dataFile);

		try {
			renameKey(dataFile, "Currency", "Balance");
		} catch (final IOException e) {
			e.printStackTrace();
		}

		Common.log("Successfully updated folder.");
	}

	private void renameKey(final File file, final String key, final String replacement) throws IOException {
		final Charset charset = StandardCharsets.UTF_8;
		final Path path = file.toPath();

		String content = Files.readString(path, charset);
		content = content.replaceAll(key, replacement);
		Files.writeString(path, content, charset);
	}

	@Override
	protected void onPluginReload() {
		Sequence.reload();
		Hologram.deleteAll();
	}

	@Override
	protected void onPluginStop() {
		Sequence.reload();
		Hologram.deleteAll();
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
		if (!YamlConfig.fromFileFast(FileUtil.getFile("data.db")).isSet("Turrets"))
			TurretData.loadTurrets();
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
				TurretUtil.updateHologramAndTexture(turretData);
			}
		});

		if (Settings.TurretSection.DISPLAY_HOLOGRAM)
			Common.runTimer(20, new HologramTask());
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
