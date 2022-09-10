package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainerChances;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonConversation;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TurretsMenu extends MenuPagged<TurretData> {

	private TurretType turretType;

	private TurretData turretData;

	private Player player;

	private final Button changeTypeButton;

	private final Button settingsButton;

	private TurretsMenu(final Player player, final TurretType turretType) {
		super(null, compileTurrets(turretType), true);

		this.turretType = turretType;
		this.player = player;

		this.setTitle(turretType.typeName + " Turrets");
		this.setSize(9 * 3);

		this.changeTypeButton = new ButtonConversation(new EditMenuTypePrompt(),
				ItemCreator.of(CompMaterial.BEACON, "Change View Type",
						"Click this button to view",
						"turrets of a different type."));

		this.settingsButton = new ButtonMenu(new SettingsMenu(this, player), CompMaterial.ANVIL, "Settings",
				"Click this button to edit",
				"your turret settings for a",
				"specific type.");
	}

	@Override
	protected String[] getInfo() {
		return new String[]{
				"In this menu, you can",
				"view all turrets of a",
				"specific type. You can",
				"also edit each individual",
				"turret by clicking them."
		};
	}

	private static List<TurretData> compileTurrets(final TurretType viewMode) {
		return new ArrayList<>(viewMode.turretTypeList);
	}

	@Override
	protected ItemStack convertToItemStack(final TurretData turretData) {
		final int level = turretData.getCurrentLevel();
		final String id = turretData.getId();
		final String type = StringUtil.capitalize(turretData.getType());
		final List<String> lore = new ArrayList<>();

		lore.add("Level: " + level);
		lore.add("Type: " + type);
		lore.add("");
		lore.add("Click to edit this turret");

		if (this.turretType.typeName.equals("All"))
			return ItemCreator.of(turretData.getMaterial()).name("&b" + type + " Turret &8" + id).lore(lore).makeMenuTool();
		else if (type.equalsIgnoreCase(this.turretType.typeName))
			return ItemCreator.of(turretData.getMaterial()).name("&f" + type + " Turret &7" + id).lore(lore).makeMenuTool();

		return NO_ITEM;
	}

	@Override
	protected void onPageClick(final Player player, final TurretData turretData, final ClickType clickType) {
		this.newInstance().displayTo(player);
		this.turretData = turretData;
		Common.runLater(() -> new TurretEditMenu(this).displayTo(player));
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot == getSize() - 1)
			return changeTypeButton.getItem();
		if (slot == getBottomCenterSlot())
			return settingsButton.getItem();

		return super.getItemAt(slot);
	}

	@Override
	public Menu newInstance() {
		return new TurretsMenu(this.player, this.turretType);
	}

	/**
	 * The prompt to give a new kit a name
	 */
	private final class EditMenuTypePrompt extends SimplePrompt {

		private EditMenuTypePrompt() {
			super(true);
		}

		@Override
		protected String getPrompt(final ConversationContext ctx) {
			return "&6Enter one of the following turret types (all, arrow, fireball, beam)";
		}

		@Override
		protected boolean isInputValid(final ConversationContext context, final String input) {
			return input.equals("all") || input.equals("arrow") || input.equals("fireball") || input.equals("beam");
		}

		@Override
		protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
			return "Type " + invalidInput + " does not exist, choose one of the following (all, arrow, fireball, beam).";
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
			turretType = TurretType.valueOf(input.toUpperCase());

			return Prompt.END_OF_CONVERSATION;
		}
	}

	private final class TurretEditMenu extends Menu {

		@Position(11)
		private final Button levelEditButton;

		@Position(13)
		private final Button blacklistButton;

		@Position(15)
		private final Button teleportButton;

		TurretEditMenu(final Menu parent) {
			super(parent, true);

			this.setSize(9 * 4);
			this.setTitle(StringUtil.capitalize(turretData.getType()) + " Turret &8" + turretData.getId());


			this.levelEditButton = new ButtonMenu(new LevelMenu(turretData.getCurrentLevel()), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.blacklistButton = new ButtonMenu(new BlacklistMenu(TurretEditMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
					"Turret Blacklist",
					"Click this button to edit",
					"your turrets blacklist.");

			this.teleportButton = Button.makeSimple(CompMaterial.ENDER_EYE, "Teleport", "Click to visit turret", player1 -> {
				player1.teleport(turretData.getLocation());

				Messenger.success(player1, "&aYou have successfully teleported to the " + turretData.getType() + " turret with the id of &2" + turretData.getId() + "&a.");
			});
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit this turrets settings."
			};
		}

		@Override
		public Menu newInstance() {
			return new TurretEditMenu(getParent());
		}

		private class LevelMenu extends Menu {

			private final int turretLevel;

			private final TurretData.TurretLevel level;

			@Position(10)
			private final Button rangeButton;

			@Position(12)
			private final Button laserEnabledButton;

			@Position(14)
			private final Button laserDamageButton;

			@Position(16)
			private final Button lootButton;

			@Position(30)
			private final Button previousLevelButton;

			@Position(32)
			private final Button nextLevelButton;

			@Position(31)
			private final Button priceButton;

			public LevelMenu(final int turretLevel) {
				super(TurretEditMenu.this, true);

				final boolean nextLevelExists = turretLevel < turretData.getLevels() || turretData.getLevels() == 0;
				final TurretRegistry registry = TurretRegistry.getInstance();

				this.turretLevel = turretLevel;
				this.level = getOrMakeLevel(turretLevel);

				this.setTitle("Turret Level " + turretLevel);
				this.setSize(9 * 4);

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
						restartMenu((isEnabled ? "&cDisabled" : "&aEnabled") + " laser pointer");
					}

					@Override
					public ItemStack getItem() {
						final boolean isEnabled = turretData.getLevel(turretLevel).isLaserEnabled();

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_WOOL : CompMaterial.RED_WOOL, "Enabled/Disable Laser",
								"Current: " + (isEnabled ? "&aenabled" : "&cdisabled"),
								"",
								"Click to enable or disable",
								"lasers for this turret.").make();
					}
				};

				this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.BLAZE_POWDER).name("Laser Damage")
								.lore("Set the amount of damage", "lasers deal if they're enabled", "by clicking this button.", "", "Current: &9" + turretData.getLevel(turretLevel).getLaserDamage()),
						"Type in an integer value between 1 and 40 (recommended value: 15-20)",
						new RangedValue(1, 40), () -> turretData.getLevel(turretLevel).getLaserDamage(), (Double input) -> registry.setLaserDamage(turretData, turretLevel, input));

				this.lootButton = new ButtonMenu(new LevelMenu.TurretLootChancesMenu(), CompMaterial.CHEST,
						"Turret Loot",
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
							new LevelMenu(turretLevel - 1).displayTo(player);
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
						final Menu nextLevelMenu;

						if (!nextLevelExists)
							TurretRegistry.getInstance().createLevel(turretData);

						nextLevelMenu = new LevelMenu(turretLevel + 1);
						nextLevelMenu.displayTo(player);
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator
								.of(nextLevelExists ? CompMaterial.LIME_DYE : CompMaterial.PURPLE_DYE,
										nextLevelExists ? "Edit next level" : "Create a new level").make();
					}
				};

				this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(
								CompMaterial.SUNFLOWER,
								"Edit Price",
								"Current: " + this.level.getPrice() + " coins",
								"",
								"Edit the price for",
								"this level."), "Enter the price for this level. (Current: " + this.level.getPrice() + " " + Settings.CurrencySection.CURRENCY_NAME + ")",
						this.getTitle(), RangedValue.parse("0-100000"), () -> turretData.getLevel(turretLevel).getPrice(), (Double input) -> registry.setLevelPrice(turretData, turretLevel, input));
			}

			private TurretData.TurretLevel getOrMakeLevel(final int turretLevel) {
				TurretData.TurretLevel level = turretData.getLevel(turretLevel);

				if (level == null)
					level = turretData.addLevel();

				return level;
			}

			@Override
			protected String[] getInfo() {
				return new String[]{
						"You can edit each individual",
						"level in this menu and set its",
						"price."
				};
			}

			@Override
			public Menu newInstance() {
				return new LevelMenu(turretLevel);
			}

			private class TurretLootChancesMenu extends MenuContainerChances {

				TurretLootChancesMenu() {
					super(LevelMenu.this);

					this.setTitle("Place turret loot here");
				}

				@Override
				protected boolean canEditItem(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor, final InventoryAction action) {
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
	}

	/*private final class ValidateMenu extends Menu {

	}*/ // TODO

	@RequiredArgsConstructor
	private enum TurretType {
		ALL("All", TurretRegistry.getInstance().getRegisteredTurrets()),
		ARROW("Arrow", TurretRegistry.getInstance().getArrowTurrets()),
		FIREBALL("Fireball", TurretRegistry.getInstance().getFireballTurrets()),
		BEAM("Beam", TurretRegistry.getInstance().getBeamTurrets());

		private final String typeName;
		private final Set<TurretData> turretTypeList;
	}

	public static void openAllTurretsSelectionMenu(final Player player) {
		new TurretsMenu(player, TurretType.ALL).displayTo(player);
	}

	public static void openArrowTurretsSelectionMenu(final Player player) {
		new TurretsMenu(player, TurretType.ARROW).displayTo(player);
	}

	public static void openFireballTurretsSelectionMenu(final Player player) {
		new TurretsMenu(player, TurretType.FIREBALL).displayTo(player);
	}

	public static void openBeamTurretsSelectionMenu(final Player player) {
		new TurretsMenu(player, TurretType.BEAM).displayTo(player);
	}
}