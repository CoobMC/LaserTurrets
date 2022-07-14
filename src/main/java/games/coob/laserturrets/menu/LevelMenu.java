package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.remain.CompMaterial;

public class LevelMenu extends Menu {

	private final int turretLevel;

	@Position(9 + 2)
	private final Button rangeButton;

	@Position(9 + 4)
	private final Button laserEnabledButton;

	@Position(9 + 6)
	private final Button laserDamageButton;

	@Position(9 + 8)
	private final Button previousLevelButton;

	@Position(9 + 8)
	private final Button nextLevelButton;

	public LevelMenu(final Player player, final TurretData turretData, final int turretLevel) {
		// TODO create a per level system

		Valid.checkBoolean(turretLevel < 3 + 2, "Cannot jump more than 2 levels ahead in turret level menu.");

		final boolean nextLevelExists = turretLevel <= turretData.getLevels() || turretData.getLevels() == 0;
		final TurretRegistry registry = TurretRegistry.getInstance();

		this.turretLevel = turretLevel;

		this.setTitle("Turret Level");
		this.setSize(9 * 4);
		this.setViewer(player);
		this.setSlotNumbersVisible();

		this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name("Turret Range")
						.lore("Set the turrets range", "by clicking this button.", "", "Current: &9" + turretData.getRange()),
				"Type in an integer value between 1 and 40 (recommend value : 15-20)",
				new RangedValue(1, 40), turretData::getRange, (Integer input) -> registry.setRange(turretData, input));


		this.laserEnabledButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				final TurretRegistry registry = TurretRegistry.getInstance();
				final boolean isEnabled = turretData.isLaserEnabled();

				registry.setLaserEnabled(turretData, !isEnabled);
				restartMenu((isEnabled ? "&cDisabled" : "&aEnabled") + "lasers");
			}

			@Override
			public ItemStack getItem() {
				final boolean isEnabled = turretData.isLaserEnabled();

				return ItemCreator.of(isEnabled ? CompMaterial.GREEN_CONCRETE : CompMaterial.RED_CONCRETE, "Enabled/Disable Laser",
						"",
						"Current: " + (isEnabled ? "&atrue" : "&cfalse"),
						"",
						"Click to enable or disable",
						"lasers for this turret.").make();
			}
		};

		this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.END_CRYSTAL).name("Laser Damage")
						.lore("Set the amount of damage", "lasers deal if they're enabled", "by clicking this button.", "", "Current: &9" + turretData.getLaserDamage()),
				"Type in an integer value between 1 and 40 (recommended value: 15-20)",
				new RangedValue(1, 40), turretData::getLaserDamage, (Double input) -> registry.setLaserDamage(turretData, input));

		this.previousLevelButton = new Button() {

			final boolean aboveFirstLevel = turretLevel > 1;

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				if (aboveFirstLevel)
					new LevelMenu(player, turretData, turretLevel - 1, nextLevelButton).displayTo(player);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(aboveFirstLevel ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE,
								"Previous level").make();
			}
		};

		this.nextLevelButton = new Button() {

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				if (aboveFirstLevel)
					new LevelMenu(player, turretData, turretLevel - 1, nextLevelButton).displayTo(player);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(aboveFirstLevel ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE,
								"Previous level").make();
			}
		};
	}
}
