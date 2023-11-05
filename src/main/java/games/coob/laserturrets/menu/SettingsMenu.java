package games.coob.laserturrets.menu;

import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.SkullCreator;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainerChances;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonConversation;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class SettingsMenu extends Menu {

	private final Player viewer;

	@Position(11)
	private final Button arrowSettingsButton;

	@Position(13)
	private final Button beamSettingsButton;

	@Position(15)
	private final Button fireballSettingsButton;

	public SettingsMenu(final @Nullable Menu parent, final Player player) {
		super(parent);

		this.viewer = player;

		this.setSize(27);
		this.setTitle(Lang.of("Settings_Menu.Menu_Title"));

		this.arrowSettingsButton = new ButtonMenu(new SettingsEditMenu("arrow"), ItemCreator.of(TurretSettings.findByName("arrow").getToolItem())
				.name(Lang.of("Settings_Menu.Arrow_Settings_Button_Title"))
				.lore(Lang.ofArray("Settings_Menu.Arrow_Settings_Button_Lore")));

		this.fireballSettingsButton = new ButtonMenu(new SettingsEditMenu("fireball"), ItemCreator.of(TurretSettings.findByName("fireball").getToolItem())
				.name(Lang.of("Settings_Menu.Fireball_Settings_Button_Title"))
				.lore(Lang.ofArray("Settings_Menu.Fireball_Settings_Button_Lore")));

		this.beamSettingsButton = new ButtonMenu(new SettingsEditMenu("beam"), ItemCreator.of(TurretSettings.findByName("beam").getToolItem())
				.name(Lang.of("Settings_Menu.Beam_Settings_Button_Title"))
				.lore(Lang.ofArray("Settings_Menu.Beam_Settings_Button_Lore")));
	}

	@Override
	public Menu newInstance() {
		return new SettingsMenu(getParent(), this.viewer);
	}

	private final class SettingsEditMenu extends Menu {

		private final String typeName;

		private final TurretSettings settings;

		@Position(11)
		private final Button levelEditButton;

		// @Position(13)
		// private final Button alliesManagerButton;

		@Position(15)
		private final Button turretLimitButton;

		@Position(19)
		private final Button ammoButton;

		@Position(21)
		private final Button headTextureButton;

		@Position(23)
		private final Button toolItemButton;

		@Position(25)
		private final Button invincibleButton;

		private SettingsEditMenu(final String typeName) {
			super(SettingsMenu.this, true);

			this.typeName = typeName;
			this.settings = TurretSettings.findByName(typeName);

			this.setSize(9 * 5);
			this.setTitle(Lang.of("Settings_Menu.Edit_Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(typeName))));

			this.turretLimitButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.CRAFTING_TABLE).name(Lang.of("Settings_Menu.Turret_Limit_Button_Title"))
							.lore(Lang.ofArray("Settings_Menu.Turret_Limit_Button_Lore", "{turretType}", TurretUtil.getDisplayName(typeName), "{limit}", this.settings.getTurretLimit())),
					Lang.of("Settings_Menu.Turret_Limit_Prompt_Message", "{turretType}", TurretUtil.getDisplayName(typeName), "{limit}", this.settings.getTurretLimit()),
					new RangedValue(0, 100), this.settings::getTurretLimit, this.settings::setTurretLimit);

			this.levelEditButton = new ButtonMenu(new LevelMenu(1), CompMaterial.EXPERIENCE_BOTTLE,
					Lang.of("Settings_Menu.Level_Edit_Button_Title"),
					Lang.ofArray("Settings_Menu.Level_Edit_Button_Lore"));

			/*this.alliesManagerButton = new ButtonMenu(new SettingsAlliesMenu(SettingsEditMenu.this, viewer), CompMaterial.KNOWLEDGE_BOOK,
					Lang.of("Settings_Menu.Allies_Manager_Button_Title"),
					Lang.ofArray("Settings_Menu.Allies_Manager_Button_Lore"));*/

			this.headTextureButton = new

					ButtonConversation(new HeadTexturePrompt(),
					ItemCreator.of(SkullCreator.itemFromBase64(this.settings.getHeadTexture()))
									.

							name(Lang.of("Settings_Menu.Head_Texture_Button_Title", "{turretType}", TurretUtil.getDisplayName(typeName)))
									.

							lore(Lang.ofArray("Settings_Menu.Head_Texture_Button_Lore", "{turretType}", TurretUtil.getDisplayName(typeName))));

			this.toolItemButton = new

					ButtonMenu(new ItemToolMenu(), ItemCreator.

					of(CompMaterial.DIAMOND_PICKAXE, Lang.of("Settings_Menu.Item_Tool_Title"), Lang.

							ofArray("Settings_Menu.Item_Tool_Button_Lore")));

			this.invincibleButton = new

					Button() {
						@Override
						public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
							final boolean isInvincible = settings.isInvincible();

							settings.setInvincible(!isInvincible);
							restartMenu(Lang.of("Settings_Menu.Invincibility_Enabled_Button_Animated_Message", "{enabledOrDisabled}", isInvincible ? "&cDisabled" : "&aEnabled"));
						}

						@Override
						public ItemStack getItem() {
							final boolean isInvincible = settings.isInvincible();

							return ItemCreator.of(isInvincible ? CompMaterial.BLUE_STAINED_GLASS : CompMaterial.RED_STAINED_GLASS, Lang.of("Settings_Menu.Invincible_Button_Title"),
									Lang.ofArray("Settings_Menu.Invincibility_Enabled_Button_Lore", "{enabledOrDisabled}", isInvincible ? "&aenabled" : "&cdisabled")).make();
						}
					}

			;

			this.ammoButton = new

					ButtonMenu(new AmmoMenu(), ItemCreator.

					of(CompMaterial.SNOWBALL, Lang.of("Settings_Menu.Ammo_Button_Title"), Lang.

							ofArray("Settings_Menu.Ammo_Button_Lore", "{turretType}", TurretUtil.getDisplayName(typeName))));
		}

		@Override
		protected String[] getInfo() {
			return Lang.ofArray("Settings_Menu.Edit_Menu_Info_Button");
		}

		@Override
		public Menu newInstance() {
			return new SettingsEditMenu(this.typeName);
		}

		private final class HeadTexturePrompt extends SimplePrompt {

			private HeadTexturePrompt() {
				super(true);
			}

			@Override
			protected String getPrompt(final ConversationContext ctx) {
				return Lang.of("Settings_Menu.Head_Texture_Prompt_Message", "{turretType}", TurretUtil.getDisplayName(typeName));
			}

			@Override
			protected boolean isInputValid(final ConversationContext context, final String input) {
				return TurretUtil.isBase64ValueValid(input);
			}

			@Override
			protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
				return Lang.of("Settings_Menu.Head_Texture_Prompt_Invalid_Text", "{invalidType}", invalidInput);
			}

			@Override
			protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
				settings.setHeadTexture(input);

				return Prompt.END_OF_CONVERSATION;
			}
		}

		private final class ItemToolMenu extends Menu {

			/**
			 * Create a new menu
			 */
			private ItemToolMenu() {
				super(SettingsEditMenu.this, true);

				setSize(9 * 3);
				setTitle(Lang.of("Settings_Menu.Item_Tool_Title"));
			}

			/**
			 * @see Menu#getItemAt(int)
			 */
			@Override
			public ItemStack getItemAt(final int slot) {

				if (slot == getCenterSlot())
					return settings.getToolItem();

				return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE).name(" ").make();
			}

			/**
			 * @see Menu#onMenuClose(Player, Inventory)
			 */
			@Override
			protected void onMenuClose(final Player player, final Inventory inventory) {
				final ItemStack item = inventory.getItem(getCenterSlot());

				settings.setToolItem(item);
				CompSound.SUCCESSFUL_HIT.play(player);
			}

			/**
			 * Enable clicking outside of the menu or in the slot item
			 */
			@Override
			protected boolean isActionAllowed(final MenuClickLocation location, final int slot, @org.jetbrains.annotations.Nullable final ItemStack clicked, @org.jetbrains.annotations.Nullable final ItemStack cursor, final InventoryAction action) {
				return location != MenuClickLocation.MENU || (location == MenuClickLocation.MENU && slot == getCenterSlot());
			}

			/**
			 * @see Menu#getInfo()
			 */
			@Override
			protected String[] getInfo() {
				return Lang.ofArray("Settings_Menu.Item_Tool_Menu_Info_Button");
			}
		}

		private final class AmmoMenu extends Menu {

			private final Button enableButton;

			private final Button priceButton;

			private final Button ammoItemButton;

			private AmmoMenu() {
				super(SettingsEditMenu.this, true);

				setSize(9 * 3);
				setTitle(Lang.of("Settings_Menu.Ammo_Menu_Title"));
				this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.WATER_BUCKET).name(Lang.of("Settings_Menu.Ammo_Price_Button_Title"))
								.lore(Lang.ofArray("Settings_Menu.Ammo_Price_Button_Lore", "{turretType}", TurretUtil.getDisplayName(typeName), "{price}", settings.getAmmo().getThirdValue(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME)),
						Lang.of("Settings_Menu.Ammo_Price_Prompt_Message", "{turretType}", TurretUtil.getDisplayName(typeName), "{amount}", settings.getAmmo().getThirdValue()), new RangedValue(0, 999999), settings.getAmmo()::getThirdValue, settings::setAmmoPrice);

				this.enableButton = new Button() {
					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						final boolean isEnabled = settings.getAmmo().getFirstValue();

						settings.setAmmoEnabled(!isEnabled);
						restartMenu(Lang.of("Settings_Menu.Ammo_Enable_Button_Animated_Message", "{enabledOrDisabled}", isEnabled ? "&cDisabled" : "&aEnabled"));
					}

					@Override
					public ItemStack getItem() {
						final boolean isEnabled = settings.getAmmo().getFirstValue();

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_WOOL : CompMaterial.RED_WOOL, Lang.of("Settings_Menu.Ammo_Enable_Button_Title"),
								Lang.ofArray("Settings_Menu.Ammo_Enable_Button_Lore", "{enabledOrDisabled}", isEnabled ? "&aenabled" : "&cdisabled")).make();
					}
				};

				this.ammoItemButton = new ButtonMenu(new AmmoItemMenu(), ItemCreator.of(CompMaterial.fromMaterial(settings.getAmmo().getSecondValue().getType()), Lang.of("Settings_Menu.Ammo_Item_Button_Title"), Lang.ofArray("Settings_Menu.Ammo_Item_Button_Lore", "{turretType}", TurretUtil.getDisplayName(typeName))));
			}

			@Override
			public ItemStack getItemAt(final int slot) {

				if (slot == getCenterSlot() - 2)
					return this.enableButton.getItem();

				if (slot == getCenterSlot())
					return this.ammoItemButton.getItem();

				if (slot == getCenterSlot() + 2)
					return this.priceButton.getItem();

				return NO_ITEM;
			}

			@Override
			protected String[] getInfo() {
				return Lang.ofArray("Settings_Menu.Ammo_Menu_Info_Button");
			}

			@Override
			public Menu newInstance() {
				return new AmmoMenu();
			}

			private final class AmmoItemMenu extends Menu {

				private AmmoItemMenu() {
					super(AmmoMenu.this, true);

					setSize(9 * 3);
					setTitle(Lang.of("Settings_Menu.Ammo_Item_Button_Title"));
				}

				@Override
				public ItemStack getItemAt(final int slot) {

					if (slot == getCenterSlot())
						return settings.getAmmo().getSecondValue();

					return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE).name(" ").make();
				}

				@Override
				protected void onMenuClose(final Player player, final Inventory inventory) {
					final ItemStack item = inventory.getItem(getCenterSlot());

					if (item == null)
						return;

					settings.setAmmoItem(item);
					CompSound.SUCCESSFUL_HIT.play(player);
				}

				/**
				 * Enable clicking outside of the menu or in the slot item
				 */
				@Override
				protected boolean isActionAllowed(final MenuClickLocation location, final int slot, @org.jetbrains.annotations.Nullable final ItemStack clicked, @org.jetbrains.annotations.Nullable final ItemStack cursor, final InventoryAction action) {
					return location != MenuClickLocation.MENU || (location == MenuClickLocation.MENU && slot == getCenterSlot());
				}

				@Override
				protected String[] getInfo() {
					return Lang.ofArray("Settings_Menu.Ammo_Item_Menu_Info_Button", "{turretType}", TurretUtil.getDisplayName(typeName));
				}
			}
		}

		private class LevelMenu extends Menu {

			private final TurretSettings.LevelData level;

			private final int turretLevel;

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
				super(SettingsEditMenu.this, true);

				final boolean nextLevelExists = turretLevel < settings.getLevelsSize() || settings.getLevelsSize() == 0;

				this.level = getOrMakeLevel(turretLevel);
				this.turretLevel = turretLevel;

				this.setTitle(Lang.of("Settings_Menu.Level_Menu_Title", "{level}", turretLevel));
				this.setSize(9 * 5);

				this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name(Lang.of("Settings_Menu.Range_Button_Title"))
								.lore(Lang.ofArray("Settings_Menu.Range_Button_Lore", "{range}", this.level.getRange())),
						Lang.of("Settings_Menu.Range_Prompt_Message", "{range}", this.level.getRange()),
						new RangedValue(0, 40), level::getRange, (Integer input) -> settings.setSettingsRange(this.level, input));


				this.laserEnabledButton = new Button() {
					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						final boolean isEnabled = level.isLaserEnabled();

						settings.setLaserEnabled(level, !isEnabled);
						restartMenu(Lang.of("Settings_Menu.Laser_Enabled_Button_Animated_Message", "{enabledOrDisabled}", isEnabled ? "&cDisabled" : "&aEnabled"));
					}

					@Override
					public ItemStack getItem() {
						final boolean isEnabled = level.isLaserEnabled();

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_WOOL : CompMaterial.RED_WOOL, Lang.of("Settings_Menu.Laser_Enabled_Button_Title"),
								Lang.ofArray("Settings_Menu.Laser_Enabled_Button_Lore", "{enabledOrDisabled}", isEnabled ? "&aenabled" : "&cdisabled")).make();
					}
				};

				this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.BLAZE_POWDER).name(Lang.of("Settings_Menu.Laser_Damage_Button_Title"))
								.lore(Lang.ofArray("Settings_Menu.Laser_Damage_Button_Lore", "{damage}", this.level.getLaserDamage())),
						Lang.of("Settings_Menu.Laser_Damage_Prompt_Message", "{damage}", this.level.getLaserDamage()),
						new RangedValue(0.0, 500.0), this.level::getLaserDamage, (Double input) -> settings.setLaserDamage(this.level, input));

				this.lootButton = new ButtonMenu(new LevelMenu.TurretLootChancesMenu(), CompMaterial.CHEST,
						Lang.of("Settings_Menu.Loot_Drop_Button_Title"),
						Lang.ofArray("Settings_Menu.Loot_Drop_Button_Lore"));

				this.healthButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.DIAMOND_CHESTPLATE).name(Lang.of("Settings_Menu.Health_Button_Title"))
								.lore(Lang.ofArray("Settings_Menu.Health_Button_Lore", "{health}", this.level.getHealth())),
						Lang.of("Settings_Menu.Health_Prompt_Message", "{health}", this.level.getHealth()),
						new RangedValue(0.0, 5000.0), this.level::getHealth, (Double input) -> settings.setHealth(this.level, input));


				this.previousLevelButton = new Button() {

					final boolean aboveFirstLevel = turretLevel > 1;

					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						if (this.aboveFirstLevel)
							new LevelMenu(turretLevel - 1).displayTo(player);
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator
								.of(this.aboveFirstLevel ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE,
										this.aboveFirstLevel ? Lang.of("Settings_Menu.Previous_Level_Button_Title_2") : Lang.of("Settings_Menu.Previous_Level_Button_Title_1")).make();
					}
				};

				this.nextLevelButton = new Button() {

					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						final Menu nextLevelMenu;

						nextLevelMenu = new LevelMenu(turretLevel + 1);
						nextLevelMenu.displayTo(player);
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator.of(nextLevelExists ? CompMaterial.LIME_DYE : CompMaterial.PURPLE_DYE,
								nextLevelExists ? Lang.of("Settings_Menu.Next_Level_Button_Title_2") : Lang.of("Settings_Menu.Next_Level_Button_Title_1")).make();
					}
				};

				this.removeLevelButton = new Button() {

					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						settings.removeLevel(turretLevel);

						final Menu previousMenu = new LevelMenu(turretLevel - 1);

						previousMenu.displayTo(player);
						Common.runLater(() -> previousMenu.animateTitle(Lang.of("Settings_Menu.Remove_Level_Button_Animated_Message", "{level}", turretLevel)));
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator.of(CompMaterial.BARRIER, Lang.of("Settings_Menu.Remove_Level_Button_Title", "{level}", turretLevel)).make();
					}
				};

				this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(
								CompMaterial.SUNFLOWER,
								Lang.of("Settings_Menu.Price_Button_Title"),
								Lang.ofArray("Settings_Menu.Price_Button_Lore", "{price}", this.level.getPrice(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME)),
						Lang.of("Settings_Menu.Price_Prompt_Message", "{price}", this.level.getPrice(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME),
						RangedValue.parse("0-100000"), (Double input) -> settings.setLevelPrice(this.level, input));
			}

			@Override
			public ItemStack getItemAt(final int slot) {
				final boolean nextLevelExists = this.turretLevel < settings.getLevelsSize() || settings.getLevelsSize() == 0;

				if (!nextLevelExists && slot == this.getBottomCenterSlot() + 3)
					return this.removeLevelButton.getItem();

				return NO_ITEM;
			}

			private TurretSettings.LevelData getOrMakeLevel(final int turretLevel) {
				TurretSettings.LevelData level = settings.getLevel(turretLevel);

				if (level == null) {
					level = settings.addLevel();
				}

				return level;
			}

			@Override
			protected String[] getInfo() {
				return Lang.ofArray("Settings_Menu.Level_Menu_Info_Button");
			}

			@Override
			public Menu newInstance() {
				return new LevelMenu(this.turretLevel);
			}

			private class TurretLootChancesMenu extends MenuContainerChances {

				TurretLootChancesMenu() {
					super(LevelMenu.this, true);

					this.setSize(54);
					this.setTitle(Lang.of("Settings_Menu.Turret_Loot_Menu_Title"));
				}

				@Override
				public Menu newInstance() {
					return new TurretLootChancesMenu();
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
					final List<Tuple<ItemStack, Double>> items = level.getLootChances();

					return slot < items.size() ? items.get(slot) : null;
				}

				@Override
				protected void onMenuClose(final StrictMap<Integer, Tuple<ItemStack, Double>> items) {
					settings.setLootChances(level, new ArrayList<>(items.values()));
				}

				@Override
				public boolean allowDecimalQuantities() {
					return true;
				}
			}
		}

		/*public class SettingsAlliesMenu extends Menu {

			@Position(14)
			private final Button mobBlacklistButton;

			@Position(12)
			private final Button playerBlacklistButton;

			public SettingsAlliesMenu(final Menu parent, final Player player) {
				super(parent, true);

				this.setViewer(player);
				this.setSize(27);
				this.setTitle(Lang.of("Settings_Menu.Allies_Menu_Title"));

				this.mobBlacklistButton = new ButtonMenu(new MobAlliesMenu(), CompMaterial.CREEPER_HEAD,
						Lang.of("Settings_Menu.Mob_Allies_Button_Title", "{listType}", settings.isEnableMobWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))),
						Lang.ofArray("Settings_Menu.Mob_Allies_Button_Lore", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

				this.playerBlacklistButton = new ButtonMenu(new PlayerAlliesMenu(), CompMaterial.PLAYER_HEAD,
						Lang.of("Settings_Menu.Player_Allies_Button_Title", "{listType}", settings.isEnablePlayerWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))),
						Lang.ofArray("Settings_Menu.Player_Allies_Button_Lore", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));
			}

			@Override
			protected String[] getInfo() {
				return Lang.ofArray("Settings_Menu.Allies_Menu_Info_Button", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
			}

			@Override
			public Menu newInstance() {
				return new SettingsAlliesMenu(getParent(), this.getViewer());
			}

			private class MobAlliesMenu extends MenuPagged<EntityType> {

				private final Button addButton;

				private final Button mobListTypeButton;

				private MobAlliesMenu() {
					super(27, SettingsAlliesMenu.this, settings.getMobList());

					this.setTitle(Lang.of("Settings_Menu.Mob_Allies_Menu_Title", "{listType}", settings.isEnableMobWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));

					this.addButton = new ButtonMenu(new MobAlliesMenu.MobSelectionMenu(), CompMaterial.ENDER_CHEST,
							Lang.of("Settings_Menu.Mob_Add_Button_Title"),
							Lang.ofArray("Settings_Menu.Mob_Add_Button_Lore", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

					this.mobListTypeButton = new Button() {
						@Override
						public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
							final boolean isWhitelist = settings.isEnableMobWhitelist();

							settings.enableMobWhitelist(!isWhitelist);
							setTitle(Lang.of("Settings_Menu.Mob_List_Type_Menu_Title", "{listType}", settings.isEnableMobWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));
							restartMenu(Lang.of("Settings_Menu.Mob_List_Type_Animated_Message", "{listType}", !isWhitelist ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist_Coloured")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist_Coloured"))));
						}

						@Override
						public ItemStack getItem() {
							final boolean isWhitelist = settings.isEnableMobWhitelist();

							return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, Lang.of("Settings_Menu.Mob_List_Type_Menu_Title", "{listType}", isWhitelist ? "&fWhitelist" : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist_Coloured"))),
									Lang.ofArray("Settings_Menu.Mob_List_Type_Button_Lore", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured"))).make();
						}
					};
				}

				@Override
				protected ItemStack convertToItemStack(final EntityType entityType) {
					return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
							.lore(Lang.ofArray("Settings_Menu.Mob_Egg_Lore", "{entityName}", entityType.name())).make();
				}

				@Override
				protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
					settings.removeMobFromBlacklist(entityType);
					this.restartMenu(Lang.of("Settings_Menu.Mob_Egg_Animated_Message", "{entityName}", entityType.name()));
				}

				@Override
				public ItemStack getItemAt(final int slot) {
					if (slot == this.getBottomCenterSlot())
						return addButton.getItem();
					if (slot == this.getBottomCenterSlot() + 3)
						return mobListTypeButton.getItem();

					return super.getItemAt(slot);
				}

				@Override
				protected String[] getInfo() {
					return Lang.ofArray("Settings_Menu.Mob_Allies_Info_Button", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
				}

				@Override
				public Menu newInstance() {
					return new MobAlliesMenu();
				}

				private class MobSelectionMenu extends MenuPagged<EntityType> {
					private MobSelectionMenu() {
						super(27, MobAlliesMenu.this, Arrays.stream(EntityType.values())
								.filter(EntityType::isAlive)
								.filter(EntityType::isSpawnable)
								.collect(Collectors.toList()), true);

						this.setTitle(Lang.of("Settings_Menu.Mob_Selection_Menu_Title"));
					}

					@Override
					protected ItemStack convertToItemStack(final EntityType entityType) {
						return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
								.glow(settings.getMobList().contains(entityType))
								.lore(settings.getMobList().contains(entityType) ? Lang.of("Settings_Menu.Mob_Already_Selected_Lore", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{entityName}", entityType.name()) : Lang.of("Settings_Menu.Mob_Available_For_Selection_Lore", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{entityName}", entityType.name()))
								.make();
					}

					@Override
					protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
						settings.addMobToBlacklist(entityType);
						this.animateTitle(Lang.of("Settings_Menu.Mob_Selection_Animated_Message", "{listType}", settings.isEnableMobWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{entityName}", entityType.name()));
					}
				}
			}

			private class PlayerAlliesMenu extends MenuPagged<UUID> {

				private final Button addButton;

				private final Button addPromptButton;

				private final Button playerListTypeButton;

				private PlayerAlliesMenu() {
					super(27, SettingsAlliesMenu.this, settings.getPlayerList(), true);

					this.setTitle(Lang.of("Settings_Menu.Player_List_Type_Menu_Title", "{listType}", settings.isEnablePlayerWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));

					this.addButton = new ButtonMenu(new PlayerAlliesMenu.PlayerSelectionMenu(), CompMaterial.ENDER_CHEST,
							Lang.of("Settings_Menu.Player_Add_Button_Title", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")),
							Lang.ofArray("Settings_Menu.Player_Add_Button_Lore", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

					this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
							ItemCreator.of(CompMaterial.WRITABLE_BOOK, Lang.of("Settings_Menu.Player_Add_Prompt_Button_Title"),
									Lang.ofArray("Settings_Menu.Player_Add_Prompt_Button_Lore", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"))));

					this.playerListTypeButton = new Button() {
						@Override
						public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
							final boolean isWhitelist = settings.isEnablePlayerWhitelist();

							settings.enablePlayerWhitelist(!isWhitelist);
							setTitle(Lang.of("Settings_Menu.Player_List_Type_Menu_Title", "{listType}", settings.isEnablePlayerWhitelist() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));
							restartMenu(Lang.of("Settings_Menu.Player_List_Type_Menu_Animated_Message", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured")));
						}

						@Override
						public ItemStack getItem() {
							final boolean isWhitelist = settings.isEnablePlayerWhitelist();

							return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, Lang.of("Settings_Menu.Player_List_Type_Button_Title", "{listType}", isWhitelist ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist_Coloured")) : Lang.of("Placeholders.Blacklist_Coloured")),
									Lang.ofArray("Settings_Menu.Player_List_Type_Button_Lore", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured"))).make();
						}
					};
				}

				@Override
				protected ItemStack convertToItemStack(final UUID uuid) {
					final OfflinePlayer player = Remain.getOfflinePlayerByUUID(uuid);
					final boolean isWhitelist = settings.isEnablePlayerWhitelist();

					return ItemCreator.of(
									CompMaterial.PLAYER_HEAD,
									player.getName(),
									Lang.ofArray("Settings_Menu.Player_Head_Lore", "{playerName}", player.getName(), "{listType}", isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured")))
							.skullOwner(player.getName()).make();
				}

				@Override
				protected void onPageClick(final Player player, final UUID item, final ClickType click) {
					final OfflinePlayer target = Remain.getOfflinePlayerByUUID(item);

					settings.removePlayerFromBlacklist(target.getUniqueId());
					this.restartMenu(Lang.of("Settings_Menu.Player_Head_Animated_Message", "{playerName}", target.getName()));
				}

				@Override
				public ItemStack getItemAt(final int slot) {
					if (slot == this.getBottomCenterSlot() - 1)
						return addButton.getItem();
					if (slot == this.getBottomCenterSlot() + 1)
						return addPromptButton.getItem();
					if (slot == this.getBottomCenterSlot() + 3)
						return playerListTypeButton.getItem();

					return super.getItemAt(slot);
				}

				@Override
				protected String[] getInfo() {
					return Lang.ofArray("Settings_Menu.Player_Menu_Info_Button", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
				}

				@Override
				public Menu newInstance() {
					return new PlayerAlliesMenu();
				}

				private class PlayerSelectionMenu extends MenuPagged<Player> {
					private PlayerSelectionMenu() {
						super(18, PlayerAlliesMenu.this, viewer.getWorld().getPlayers());

						this.setTitle(Lang.of("Settings_Menu.Player_Selection_Menu_Title"));
					}

					@Override
					protected ItemStack convertToItemStack(final Player player) {
						return ItemCreator.of(
										CompMaterial.PLAYER_HEAD,
										player.getName(),
										(settings.getPlayerList().contains(player.getUniqueId()) ? Lang.of("Settings_Menu.Player_Already_Selected_Lore", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{playerName}", player.getName()) : Lang.of("Settings_Menu.Player_Available_For_Selection_Lore", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{playerName}", player.getName())))
								.skullOwner(player.getName()).make();
					}

					@Override
					protected void onPageClick(final Player player, final Player item, final ClickType click) {
						settings.addPlayerToBlacklist(item.getUniqueId());
						this.animateTitle(Lang.of("Settings_Menu.Player_Selection_Animated_Message", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{playerName}", player.getName()));
					}
				}
			}

			private final class PlayerBlacklistPrompt extends SimplePrompt {

				@Override
				protected String getPrompt(final ConversationContext context) {
					return Lang.of("Settings_Menu.Player_Prompt_Message", "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
				}

				@Override
				protected boolean isInputValid(final ConversationContext context, final String input) {
					for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
						return player.getName() != null && player.getName().equals(input);
					return false;
				}

				@Override
				protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
					return Lang.of("Settings_Menu.Player_Prompt_Invalid_Text", "{invalidPlayer}", invalidInput);
				}

				@org.jetbrains.annotations.Nullable
				@Override
				protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
					settings.addPlayerToBlacklist(Bukkit.getOfflinePlayer(input).getUniqueId());
					tellSuccess(Lang.of("Settings_Menu.Player_Prompt_Success", "{playerName}", input, "{listType}", settings.isEnablePlayerWhitelist() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

					return END_OF_CONVERSATION;
				}
			}
		}*/
	}
}