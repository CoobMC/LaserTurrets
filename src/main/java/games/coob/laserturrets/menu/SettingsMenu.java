package games.coob.laserturrets.menu;

import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
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
import org.mineacademy.fo.remain.Remain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SettingsMenu extends Menu {

	private Player viewer;

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
		this.setTitle("Turret Settings");

		this.arrowSettingsButton = new ButtonMenu(new SettingsEditMenu("arrow"), CompMaterial.ARROW,
				"Arrow Turret Settings",
				"Edit the default settings",
				"for arrow turrets.");

		this.fireballSettingsButton = new ButtonMenu(new SettingsEditMenu("fireball"), CompMaterial.FIRE_CHARGE,
				"Fireball Turret Settings",
				"Edit the default settings",
				"for fireball turrets.");

		this.beamSettingsButton = new ButtonMenu(new SettingsEditMenu("beam"), CompMaterial.BLAZE_ROD,
				"Beam Turret Settings",
				"Edit the default settings",
				"for beam turrets.");
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

		@Position(13)
		private final Button alliesManagerButton;

		@Position(15)
		private final Button turretLimitButton;

		private SettingsEditMenu(final String typeName) {
			super(SettingsMenu.this, true);

			this.typeName = typeName;
			this.settings = TurretSettings.findTurretSettings(typeName);

			this.setSize(9 * 4);
			this.setTitle(StringUtil.capitalize(typeName) + " Turrets");

			this.turretLimitButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
					final ItemStack itemStack = this.getItem();

					if (click.isLeftClick()) {
						itemStack.setAmount(itemStack.getAmount() + 1);
						restartMenu();
					} else if (click.isRightClick()) {
						if (itemStack.getAmount() > 0) {
							itemStack.setAmount(itemStack.getAmount() - 1);
							restartMenu();
						} else animateTitle("&cLimit cannot be negative");
					}
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.CRAFTING_TABLE, "&fTurret Limit",
							"Limit the amount of " + typeName,
							"turrets that can be created.").make();
				}
			};

			this.levelEditButton = new ButtonMenu(new LevelMenu(1), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.alliesManagerButton = new ButtonMenu(new SettingsAlliesMenu(SettingsEditMenu.this, viewer), CompMaterial.KNOWLEDGE_BOOK,
					"Turret Allies Manager",
					"Click this button to edit",
					"your turrets targets.");
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit this turrets settings."
			};
		}

		@Override
		public Menu newInstance() {
			return new SettingsEditMenu(this.typeName);
		}

		private class LevelMenu extends Menu {

			private final TurretSettings.LevelData level;

			private final int turretLevel;

			@Position(10)
			private final Button rangeButton;

			@Position(12)
			private final Button laserEnabledButton;

			@Position(14)
			private final Button laserDamageButton;

			@Position(16)
			private final Button lootButton;

			private final Button healthButton; // TODO

			@Position(30)
			private final Button previousLevelButton;

			@Position(32)
			private final Button nextLevelButton;

			private final Button removeLevelButton;

			@Position(31)
			private final Button priceButton;

			public LevelMenu(final int turretLevel) {
				super(SettingsEditMenu.this, true);

				final boolean nextLevelExists = turretLevel < settings.getLevelsSize() || settings.getLevelsSize() == 0;

				this.level = getOrMakeLevel(turretLevel);
				this.turretLevel = turretLevel;

				this.setTitle("Turret Level " + turretLevel);
				this.setSize(9 * 4);

				this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name("Turret Range")
								.lore("Set the turrets range", "by clicking this button.", "", "Current: &9" + this.level.getRange()),
						"Type in an integer value between 0 and 40 (recommend value : 15-20).",
						new RangedValue(0, 40), level::getRange, (Integer input) -> settings.setSettingsRange(this.level, input));


				this.laserEnabledButton = new Button() {
					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						final boolean isEnabled = level.isLaserEnabled();

						settings.setLaserEnabled(level, !isEnabled);
						restartMenu((isEnabled ? "&cDisabled" : "&aEnabled") + " laser pointer");
					}

					@Override
					public ItemStack getItem() {
						final boolean isEnabled = level.isLaserEnabled();

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_WOOL : CompMaterial.RED_WOOL, "Enabled/Disable Laser",
								"Current: " + (isEnabled ? "&aenabled" : "&cdisabled"),
								"",
								"Click to enable or disable",
								"lasers for this turret.").make();
					}
				};

				this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.BLAZE_POWDER).name("Laser Damage")
								.lore("Set the amount of damage", "lasers deal if they're enabled", "by clicking this button.", "", "Current: &9" + level.getLaserDamage()),
						"Type in an integer value between 0.0 and 500.0.",
						new RangedValue(0.0, 500.0), this.level::getLaserDamage, (Double input) -> settings.setLaserDamage(this.level, input));

				this.lootButton = new ButtonMenu(new LevelMenu.TurretLootChancesMenu(), CompMaterial.CHEST,
						"Turret Loot",
						"Open this menu to edit",
						"the loot players get when",
						"they destroy a turret.",
						"You can also edit the drop",
						"chance.");

				this.healthButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.BLAZE_POWDER).name("Health")
								.lore("Set the amount of health", "the turrets will have", "by clicking this button.", "", "Current: &9" + level.getHealth()),
						"Type in an integer value between 0.0 and 500.0.",
						new RangedValue(0.0, 500.0), this.level::getLaserDamage, (Double input) -> settings.setLaserDamage(this.level, input));


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
										this.aboveFirstLevel ? "Edit previous level" : "This is the first level").make();
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
								nextLevelExists ? "Edit next level" : "Create a new level").make();
					}
				};

				this.removeLevelButton = new Button() {

					@Override
					public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
						settings.removeLevel(turretLevel);

						final Menu previousMenu = new LevelMenu(turretLevel - 1);

						previousMenu.displayTo(player);
						Common.runLater(() -> previousMenu.animateTitle("&cRemoved level " + turretLevel));
					}

					@Override
					public ItemStack getItem() {
						return ItemCreator.of(CompMaterial.BARRIER, "&cRemove this level").make();
					}
				};

				this.priceButton = Button.makeDecimalPrompt(ItemCreator.of(
								CompMaterial.SUNFLOWER,
								"Edit Price",
								"Current: " + this.level.getPrice() + " coins",
								"",
								"Edit the price for",
								"this level."), "Enter the price for this level. (Current: " + this.level.getPrice() + " " + Settings.CurrencySection.CURRENCY_NAME + ")",
						RangedValue.parse("0-100000"), (Double input) -> settings.setLevelPrice(this.level, input));
			}

			@Override
			public ItemStack getItemAt(final int slot) {
				final boolean nextLevelExists = this.turretLevel < settings.getLevelsSize() || settings.getLevelsSize() == 0;

				if (!nextLevelExists && slot == 34)
					return this.removeLevelButton.getItem();

				return NO_ITEM;
			}

			private TurretSettings.LevelData getOrMakeLevel(final int turretLevel) {
				TurretSettings.LevelData level = settings.getLevel(turretLevel);

				if (level == null) {
					level = settings.addLevel();
					//level.setLevel(turretLevel);
				}

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
				return new LevelMenu(this.turretLevel);
			}

			private class TurretLootChancesMenu extends MenuContainerChances {

				TurretLootChancesMenu() {
					super(LevelMenu.this, true);

					this.setSize(54);
					this.setTitle("Place turret loot here");
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

		public class SettingsAlliesMenu extends Menu {

			@Position(14)
			private final Button mobBlacklistButton;

			@Position(12)
			private final Button playerBlacklistButton;

			public SettingsAlliesMenu(final Menu parent, final Player player) {
				super(parent, true);

				this.setViewer(player);
				this.setSize(27);
				this.setTitle("Turret Allies Manager");

				this.mobBlacklistButton = new ButtonMenu(new MobAlliesMenu(), CompMaterial.CREEPER_HEAD,
						"Mob " + (settings.isEnableMobWhitelist() ? "Whitelist" : "Blacklist"), "Edit your mob " + (settings.isEnableMobWhitelist() ? "whitelist" : "blacklist"));

				this.playerBlacklistButton = new ButtonMenu(new PlayerAlliesMenu(), CompMaterial.PLAYER_HEAD,
						"Player " + (settings.isEnablePlayerWhitelist() ? "Whitelist" : "Blacklist"), "Edit your player " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist"));
			}

			@Override
			protected String[] getInfo() {
				return new String[]{
						"&fWhitelisted&7 players/mobs are",
						"the only targets of the turret.",
						"",
						"&8Blacklisted&7 players/mobs won't",
						"get targeted by the turret."
				};
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

					this.setTitle("Mob " + (settings.isEnableMobWhitelist() ? "Whitelist" : "Blacklist"));

					this.addButton = new ButtonMenu(new MobAlliesMenu.MobSelectionMenu(), CompMaterial.ENDER_CHEST,
							"Add Mob",
							"Open this menu to add",
							"mobs to the " + (settings.isEnableMobWhitelist() ? "whitelist." : "blacklist."));

					this.mobListTypeButton = new Button() {
						@Override
						public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
							final boolean isWhitelist = settings.isEnableMobWhitelist();

							settings.enableMobWhitelist(!isWhitelist);
							setTitle("&0Mob " + (settings.isEnableMobWhitelist() ? "Whitelist" : "Blacklist"));
							restartMenu("&eChanged to " + (isWhitelist ? "&fWhitelist" : "&0Blacklist"));
						}

						@Override
						public ItemStack getItem() {
							final boolean isWhitelist = settings.isEnableMobWhitelist();

							return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, (isWhitelist ? "&fWhitelist" : "&8Blacklist"),
									"Click to change to " + (!isWhitelist ? "&fwhitelist" : "&8blacklist")).make();
						}
					};
				}

				@Override
				protected ItemStack convertToItemStack(final EntityType entityType) {
					return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
							.lore("Click to remove").make();
				}

				@Override
				protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
					settings.removeMobFromBlacklist(entityType);
					this.animateTitle("&cRemoved " + entityType.name());
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
					return new String[]{
							"Edit your mob " + (settings.isEnableMobWhitelist() ? "whitelist" : "blacklist") + " by",
							"clicking the existing eggs",
							"to remove them or clicking",
							"the 'Add Mob' button to add",
							"mobs to your " + (settings.isEnableMobWhitelist() ? "whitelist" : "blacklist") + "."
					};
				}

				@Override
				public Menu newInstance() {
					return new MobAlliesMenu();
				}

				private class MobSelectionMenu extends MenuPagged<EntityType> {
					private MobSelectionMenu() {
						super(27, MobAlliesMenu.this, Arrays.stream(EntityType.values())
								.filter(EntityType::isAlive)
								.collect(Collectors.toList()), true);

						this.setTitle("Select a Mob");
					}

					@Override
					protected ItemStack convertToItemStack(final EntityType entityType) {
						return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
								.glow(settings.getMobList().contains(entityType))
								.lore(settings.getMobList().contains(entityType) ? "&aAlready " + (settings.isEnableMobWhitelist() ? "whitelisted" : "blacklisted") : "Click to add")
								.make();
					}

					@Override
					protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
						settings.addMobToBlacklist(entityType);
						this.restartMenu("&aAdded " + entityType.name());
					}
				}
			}

			private class PlayerAlliesMenu extends MenuPagged<UUID> {

				private final Button addButton;

				private final Button addPromptButton;

				private final Button playerListTypeButton;

				private PlayerAlliesMenu() {
					super(27, SettingsAlliesMenu.this, settings.getPlayerList(), true);

					this.setTitle("Player " + (settings.isEnablePlayerWhitelist() ? "Whitelist" : "Blacklist"));

					this.addButton = new ButtonMenu(new PlayerAlliesMenu.PlayerSelectionMenu(), CompMaterial.ENDER_CHEST,
							"Add Players",
							"Open this menu to add ",
							"players to the " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist"));

					this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
							ItemCreator.of(CompMaterial.WRITABLE_BOOK, "Type a name",
									"Click this button if you",
									"would like to add a player",
									"to the " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist") + " by typing ",
									"his name, this means you can",
									"also add offline players."));

					this.playerListTypeButton = new Button() {
						@Override
						public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
							final boolean isWhitelist = settings.isEnablePlayerWhitelist();

							settings.enablePlayerWhitelist(!isWhitelist);
							setTitle("&0Player " + (settings.isEnablePlayerWhitelist() ? "Whitelist" : "Blacklist"));
							restartMenu("&eChanged to " + (isWhitelist ? "&fWhitelist" : "&0Blacklist"));
						}

						@Override
						public ItemStack getItem() {
							final boolean isWhitelist = settings.isEnablePlayerWhitelist();

							return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, (isWhitelist ? "&fWhitelist" : "&8Blacklist"),
									"Click to change to " + (!isWhitelist ? "&fwhitelist" : "&8blacklist")).make();
						}
					};
				}

				@Override
				protected ItemStack convertToItemStack(final UUID uuid) {
					final Player player = Remain.getPlayerByUUID(uuid);

					return ItemCreator.of(
									CompMaterial.PLAYER_HEAD,
									player.getName(),
									"Click to remove")
							.skullOwner(player.getName()).make();
				}

				@Override
				protected void onPageClick(final Player player, final UUID item, final ClickType click) {
					final Player target = Remain.getPlayerByUUID(item);

					settings.removePlayerFromBlacklist(target.getUniqueId());
					this.restartMenu("&cRemoved " + target.getName());
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
					return new String[]{
							"Edit your player " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist") + " by",
							"clicking the existing heads",
							"to remove them or clicking",
							"the 'Add Mob' button to add",
							"players to your " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist") + "."
					};
				}

				@Override
				public Menu newInstance() {
					return new PlayerAlliesMenu();
				}

				private class PlayerSelectionMenu extends MenuPagged<Player> {
					private PlayerSelectionMenu() {
						super(18, PlayerAlliesMenu.this, viewer.getWorld().getPlayers());

						this.setTitle("Select a player");
					}

					@Override
					protected ItemStack convertToItemStack(final Player player) {
						return ItemCreator.of(
										CompMaterial.PLAYER_HEAD,
										player.getName(),
										(settings.getPlayerList().contains(player.getUniqueId()) ? "&aAlready " + (settings.isEnablePlayerWhitelist() ? "whitelisted" : "blacklisted") : "Click to add"))
								.skullOwner(player.getName()).make();
					}

					@Override
					protected void onPageClick(final Player player, final Player item, final ClickType click) {
						settings.addPlayerToBlacklist(item.getUniqueId());
						this.restartMenu("&aAdded " + player.getName());
					}
				}
			}

			private final class PlayerBlacklistPrompt extends SimplePrompt {

				@Override
				protected String getPrompt(final ConversationContext context) {
					return "&6What player shouldn't be targeted by this turret? You can add more players to the " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist") + " by using the /turret blacklist add <player> command.";
				}

				@Override
				protected boolean isInputValid(final ConversationContext context, final String input) {
					for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
						return player.getName() != null && player.getName().equals(input);
					return false;
				}

				@Override
				protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
					return "Player '" + invalidInput + "' doesn't exist.";
				}

				@org.jetbrains.annotations.Nullable
				@Override
				protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
					settings.addPlayerToBlacklist(Bukkit.getOfflinePlayer(input).getUniqueId());
					tellSuccess("You have added " + input + " to the " + (settings.isEnablePlayerWhitelist() ? "whitelist" : "blacklist") + "!");

					return END_OF_CONVERSATION;
				}
			}
		}
	}
}