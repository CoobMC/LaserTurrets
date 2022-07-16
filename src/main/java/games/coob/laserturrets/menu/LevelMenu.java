package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainerChances;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;

public class LevelMenu extends Menu {

	private final TurretData turretData;

	private final int turretLevel;

	private final TurretData.TurretLevel level;

	@Position(9 + 2)
	private final Button rangeButton;

	@Position(9 + 4)
	private final Button laserEnabledButton;

	@Position(9 + 6)
	private final Button laserDamageButton;

	@Position(9 + 7)
	private final Button lootButton;

	@Position(9 + 8)
	private final Button previousLevelButton;

	@Position(9 + 8)
	private final Button nextLevelButton;

	@Position(9 + 8)
	private final Button priceButton;

	public LevelMenu(final Player player, final TurretData turretData, final int turretLevel) {
		// TODO create a per level system

		Valid.checkBoolean(turretLevel < 3 + 2, "Cannot jump more than 2 levels ahead in turret level menu.");

		final boolean nextLevelExists = turretLevel <= turretData.getLevels() || turretData.getLevels() == 0;
		final TurretRegistry registry = TurretRegistry.getInstance();

		this.turretData = turretData;
		this.turretLevel = turretLevel;
		this.level = getOrMakeLevel(turretLevel);

		this.setTitle("Turret Level");
		this.setSize(9 * 4);
		this.setViewer(player);
		this.setSlotNumbersVisible();

		this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name("Turret Range")
						.lore("Set the turrets range", "by clicking this button.", "", "Current: &9" + turretData.getLevel(turretLevel).getRange()),
				"Type in an integer value between 1 and 40 (recommend value : 15-20)",
				new RangedValue(1, 40), () -> turretData.getLevel(turretLevel).getRange(), (Integer input) -> registry.setRange(turretData, turretLevel, input));


		this.laserEnabledButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				final TurretRegistry registry = TurretRegistry.getInstance();
				final boolean isEnabled = turretData.getLevel(turretLevel).isLaserEnabled();

				registry.setLaserEnabled(turretData, turretLevel, !isEnabled);
				restartMenu((isEnabled ? "&cDisabled" : "&aEnabled") + "lasers");
			}

			@Override
			public ItemStack getItem() {
				final boolean isEnabled = turretData.getLevel(turretLevel).isLaserEnabled();

				return ItemCreator.of(isEnabled ? CompMaterial.GREEN_CONCRETE : CompMaterial.RED_CONCRETE, "Enabled/Disable Laser",
						"",
						"Current: " + (isEnabled ? "&atrue" : "&cfalse"),
						"",
						"Click to enable or disable",
						"lasers for this turret.").make();
			}
		};

		this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.END_CRYSTAL).name("Laser Damage")
						.lore("Set the amount of damage", "lasers deal if they're enabled", "by clicking this button.", "", "Current: &9" + turretData.getLevel(turretLevel).getLaserDamage()),
				"Type in an integer value between 1 and 40 (recommended value: 15-20)",
				new RangedValue(1, 40), () -> turretData.getLevel(turretLevel).getLaserDamage(), (Double input) -> registry.setLaserDamage(turretData, turretLevel, input));

		this.lootButton = new ButtonMenu(new TurretLootChancesMenu(), CompMaterial.CHEST,
				"Turret Loot",
				"",
				"Open this menu to edit",
				"the loot players get when",
				"they destroy a turret.",
				"You can also edit the drop",
				"chance.");

		this.previousLevelButton = new Button() {

			final boolean aboveFirstLevel = turretLevel > 1;

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				if (aboveFirstLevel)
					new LevelMenu(player, turretData, turretLevel - 1).displayTo(player);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(aboveFirstLevel ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE,
								aboveFirstLevel ? "Edit previous level" : "This is the first level").make();
			}
		};

		this.nextLevelButton = new Button() {

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
				if (nextLevelExists) {
					final Menu nextLevelMenu = new LevelMenu(player, turretData, turretLevel + 1);

					nextLevelMenu.displayTo(player);

					if (turretLevel > 1)
						Common.runLater(() -> nextLevelMenu.animateTitle("&4Removed level " + turretLevel));
				}
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(nextLevelExists ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE,
								nextLevelExists ? "Edit next level" : "Finish this level first").make();
			}
		};

		this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(
						CompMaterial.SUNFLOWER,
						"Edit Price",
						"",
						"Current: " + this.level.getPrice() + " coins",
						"",
						"Edit the price for",
						"this level."),
				"Enter teh price for this level. Curretnt: " + this.level.getPrice() + " coins.",
				RangedValue.parse("0-100000"), (Double input) -> registry.setLevelPrice(turretData, turretLevel, input));
	}

	private TurretData.TurretLevel getOrMakeLevel(final int turretLevel) {
		TurretData.TurretLevel level = this.turretData.getLevel(turretLevel);

		if (level == null)
			level = this.turretData.addLevel();

		return level;
	}

	private class TurretLootChancesMenu extends MenuContainerChances {

		TurretLootChancesMenu() {
			super(LevelMenu.this);

			this.setTitle("Place turret loot here");
		}

		@Override
		protected boolean canEditItem(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor, final InventoryAction action) {
			final ItemStack placedItem = clicked != null && !CompMaterial.isAir(clicked) ? clicked : cursor;

			if (placedItem != null && !CompMaterial.isAir(placedItem)) {
				if (placedItem.getAmount() > 1 && action != InventoryAction.PLACE_ONE) {
					this.animateTitle("&4Amount must be 1!");

					return false;
				}
			}

			return true;
		}

		@Override
		protected ItemStack getDropAt(final int slot) {
			final Tuple<ItemStack, Double> tuple = this.getTuple(slot);

			return tuple != null ? tuple.getKey() : NO_ITEM;
		}

		@Override
		protected double getDropChance(final int slot) {
			final Tuple<ItemStack, Double> tuple = this.getTuple(slot);

			return tuple != null ? tuple.getValue() : 0;
		}

		private Tuple<ItemStack, Double> getTuple(final int slot) {
			final TurretRegistry registry = TurretRegistry.getInstance();
			final List<Tuple<ItemStack, Double>> items = registry.getTurretLootChances(turretData, turretLevel);

			return slot < items.size() ? items.get(slot) : null;
		}

		@Override
		protected void onMenuClose(final StrictMap<Integer, Tuple<ItemStack, Double>> items) {
			final TurretRegistry registry = TurretRegistry.getInstance();

			registry.setTurretLootChances(turretData, turretLevel, new ArrayList<>(items.values()));
		}

		@Override
		public boolean allowDecimalQuantities() {
			return true;
		}
	}
}
