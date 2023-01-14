package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
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

		this.setTitle(Lang.of("Turrets_Menu.Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretType.typeName))));
		this.setSize(9 * 3);

		this.changeTypeButton = new ButtonConversation(new EditMenuTypePrompt(),
				ItemCreator.of(CompMaterial.BEACON, Lang.of("Turrets_Menu.Change_Turret_Type_Button_Title"),
						Lang.ofArray("Turrets_Menu.Change_Turret_Type_Button_Lore")));

		this.settingsButton = new ButtonMenu(new SettingsMenu(this, player), CompMaterial.ANVIL, Lang.of("Turrets_Menu.Settings_Button_Title"),
				Lang.ofArray("Turrets_Menu.Settings_Button_Lore"));
	}

	@Override
	protected String[] getInfo() {
		return Lang.ofArray("Turrets_Menu.Info_Button");
	}

	private static List<TurretData> compileTurrets(final TurretType viewMode) {
		return new ArrayList<>(viewMode.turretTypeList);
	}

	@Override
	protected ItemStack convertToItemStack(final TurretData turretData) {
		final int level = turretData.getCurrentLevel();
		final String id = turretData.getId();
		final String type = TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType()));
		final String[] lore = Lang.ofArray("Turrets_Menu.Turrets_Lore", "{level}", level, "{turretType}", type);

		if (this.turretType.typeName.equalsIgnoreCase("all"))
			return ItemCreator.of(turretData.getMaterial()).name(Lang.of("Turrets_Menu.Turrets_Title", "{turretType}", type, "{turretId}", id)).lore(lore).makeMenuTool();
		else if (type.equalsIgnoreCase(this.turretType.typeName))
			return ItemCreator.of(turretData.getMaterial()).name(Lang.of("Turrets_Menu.Turrets_Title", "{turretType}", type, "{turretId}", id)).lore(lore).makeMenuTool();

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

	private final class EditMenuTypePrompt extends SimplePrompt {

		private EditMenuTypePrompt() {
			super(true);
		}

		@Override
		protected String getPrompt(final ConversationContext ctx) {
			return Lang.of("Turrets_Menu.Edit_Turret_View_Type_Prompt_Message");
		}

		@Override
		protected boolean isInputValid(final ConversationContext context, final String input) {
			return input.equals("all") || input.equals("arrow") || input.equals("fireball") || input.equals("beam");
		}

		@Override
		protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
			return Lang.of("Turrets_Menu.Edit_Turret_View_Type_Prompt_Invalid_Text", "{invalidType}", invalidInput);
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

		@Position(31)
		private final Button removeTurret;

		TurretEditMenu(final Menu parent) {
			super(parent, true);

			this.setSize(9 * 4);
			this.setTitle(Lang.of("Turrets_Menu.Turret_Edit_Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType())), "{turretId}", turretData.getId()));

			this.levelEditButton = new ButtonMenu(new LevelMenu(turretData.getCurrentLevel()), CompMaterial.EXPERIENCE_BOTTLE,
					Lang.of("Turrets_Menu.Level_Edit_Button_Title"),
					Lang.ofArray("Turrets_Menu.Level_Edit_Button_Lore"));

			this.blacklistButton = new ButtonMenu(new AlliesMenu(TurretEditMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
					Lang.of("Turrets_Menu.Allies_Button_Title"),
					Lang.ofArray("Turrets_Menu.Allies_Button_Lore"));

			this.teleportButton = Button.makeSimple(CompMaterial.ENDER_EYE, Lang.of("Turrets_Menu.Teleport_Button_Title"),
					Lang.of("Turrets_Menu.Teleport_Button_Lore"), player1 -> {
						player1.teleport(turretData.getLocation());

						Messenger.success(player1, Lang.of("Turrets_Menu.Teleport_Success_Message", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId()));
					});

			this.removeTurret = Button.makeSimple(CompMaterial.BARRIER, Lang.of("Turrets_Menu.Remove_Turret_Button_Title"), Lang.of("Turrets_Menu.Remove_Turret_Button_Lore"), player1 -> {
				final TurretRegistry registry = TurretRegistry.getInstance();

				registry.unregister(turretData);

				final Menu previousMenu = new TurretsMenu(player1, turretType);
				
				previousMenu.displayTo(player1);
				Common.runLater(() -> previousMenu.restartMenu(Lang.of("Turrets_Menu.Remove_Turret_Animated_Message", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId())));
			});
		}

		@Override
		protected String[] getInfo() {
			return Lang.ofArray("Turrets_Menu.Turret_Edit_Menu_Info_Button", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId());
		}

		@Override
		public Menu newInstance() {
			return new TurretEditMenu(getParent());
		}

		private class LevelMenu extends Menu {

			private final int turretLevel;

			private final TurretData.TurretLevel level;

			@Position(11)
			private final Button rangeButton;

			@Position(13)
			private final Button laserEnabledButton;

			@Position(15)
			private final Button laserDamageButton;

			@Position(21)
			private final Button lootButton;

			@Position(23)
			private final Button healthButton;

			@Position(39)
			private final Button previousLevelButton;

			@Position(41)
			private final Button nextLevelButton;

			private final Button removeLevelButton;

			@Position(40)
			private final Button priceButton;

			public LevelMenu(final int turretLevel) {
				super(TurretEditMenu.this, true);

				final boolean nextLevelExists = turretLevel < turretData.getLevels() || turretData.getLevels() == 0;
				final TurretRegistry registry = TurretRegistry.getInstance();

				this.turretLevel = turretLevel;
				this.level = getOrMakeLevel(turretLevel);

				this.setTitle(Lang.of("Turrets_Menu.Level_Menu_Title", "{level}", turretLevel));
				this.setSize(9 * 5);

				this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name(Lang.of("Turrets_Menu.Range_Button_Title"))
								.lore(Lang.ofArray("Turrets_Menu.Range_Button_Lore", "{range}", turretData.getLevel(turretLevel).getRange())),
						Lang.of("Turrets_Menu.Range_Prompt_Message", "{range}", turretData.getLevel(turretLevel).getRange()),
						new RangedValue(1, 40), () -> turretData.getLevel(turretLevel).getRange(), (Integer input) -> registry.setRange(turretData, turretLevel, input));


				this.laserEnabledButton = new Button() {
					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						final TurretRegistry registry = TurretRegistry.getInstance();
						final boolean isEnabled = turretData.getLevel(turretLevel).isLaserEnabled();

						registry.setLaserEnabled(turretData, turretLevel, !isEnabled);
						restartMenu((Lang.of("Turrets_Menu.Laser_Enabled_Button_Animated_Message", "{enabledOrDisabled}", isEnabled ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Enabled")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Disabled")))));
					}

					@Override
					public ItemStack getItem() {
						final boolean isEnabled = turretData.getLevel(turretLevel).isLaserEnabled();

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_WOOL : CompMaterial.RED_WOOL, Lang.of("Turrets_Menu.Laser_Enabled_Button_Title"),
								Lang.ofArray("Turrets_Menu.Laser_Enabled_Button_Lore", "{enabledOrDisabled}", isEnabled ? Lang.of("Placeholders.Enabled") : Lang.of("Placeholders.Disabled"))).make();
					}
				};

				this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.BLAZE_POWDER).name(Lang.of("Turrets_Menu.Laser_Damage_Button_Title"))
								.lore(Lang.ofArray("Turrets_Menu.Laser_Damage_Button_Lore", "{damage}", turretData.getLevel(turretLevel).getLaserDamage())),
						Lang.of("Turrets_Menu.Laser_Damage_Prompt_Message", "{damage}", turretData.getLevel(turretLevel).getLaserDamage()),
						new RangedValue(0.0, 500.0), () -> turretData.getLevel(turretLevel).getLaserDamage(), (Double input) -> registry.setLaserDamage(turretData, turretLevel, input));

				this.lootButton = new ButtonMenu(new LevelMenu.TurretLootChancesMenu(), CompMaterial.CHEST,
						Lang.of("Turrets_Menu.Loot_Drop_Button_Title"),
						Lang.ofArray("Turrets_Menu.Loot_Drop_Button_Lore"));

				this.healthButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.DIAMOND_CHESTPLATE).name(Lang.of("Turrets_Menu.Health_Button_Title"))
								.lore(Lang.ofArray("Turrets_Menu.Health_Button_Lore", "{health}", turretData.getLevel(turretLevel).getMaxHealth())),
						Lang.of("Turrets_Menu.Health_Prompt_Message", "{health}", turretData.getLevel(turretLevel).getMaxHealth()),
						new RangedValue(0.0, 5000.0), () -> turretData.getLevel(turretLevel).getMaxHealth(), (Double input) -> turretData.getLevel(turretLevel).setMaxHealth(input));


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
										aboveFirstLevel ? Lang.of("Turrets_Menu.Previous_Level_Button_Title_2") : Lang.of("Turrets_Menu.Previous_Level_Button_Title_1")).make();
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
						return ItemCreator.of(nextLevelExists ? CompMaterial.LIME_DYE : CompMaterial.PURPLE_DYE,
								nextLevelExists ? Lang.of("Turrets_Menu.Next_Level_Button_Title_2") : Lang.of("Turrets_Menu.Next_Level_Button_Title_1")).make();
					}
				};

				this.removeLevelButton = new Button() {

					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						TurretRegistry.getInstance().removeLevel(turretData, turretLevel);

						final Menu previousMenu = new LevelMenu(turretLevel - 1);

						previousMenu.displayTo(player);
						Common.runLater(() -> previousMenu.animateTitle(Lang.of("Turrets_Menu.Remove_Level_Button_Animated_Message", "{level}", turretLevel)));
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator.of(CompMaterial.BARRIER, Lang.of("Turrets_Menu.Remove_Level_Button_Title", "{level}", turretLevel)).make();
					}
				};

				this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(
								CompMaterial.SUNFLOWER,
								Lang.of("Turrets_Menu.Price_Button_Title"),
								Lang.ofArray("Turrets_Menu.Price_Button_Lore", "{price}", this.level.getPrice(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME)),
						Lang.of("Turrets_Menu.Price_Prompt_Message", "{price}", this.level.getPrice(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME),
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
				return Lang.ofArray("Turrets_Menu.Level_Menu_Info_Button");
			}

			@Override
			public ItemStack getItemAt(final int slot) {
				final boolean nextLevelExists = this.turretLevel < turretData.getLevels() || turretData.getLevels() == 0;

				if (!nextLevelExists && slot == 34)
					return this.removeLevelButton.getItem();

				return NO_ITEM;
			}

			@Override
			public Menu newInstance() {
				return new LevelMenu(turretLevel);
			}

			private class TurretLootChancesMenu extends MenuContainerChances {

				TurretLootChancesMenu() {
					super(LevelMenu.this);

					this.setSize(54);
					this.setTitle(Lang.of("Turrets_Menu.Turret_Loot_Menu_Title"));
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
		ALL("all", TurretRegistry.getInstance().getRegisteredTurrets()),
		ARROW("arrow", TurretRegistry.getInstance().getTurretsOfType("arrow")),
		FIREBALL("fireball", TurretRegistry.getInstance().getTurretsOfType("fireball")),
		BEAM("beam", TurretRegistry.getInstance().getTurretsOfType("beam"));

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