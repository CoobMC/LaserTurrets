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

		@Position(12)
		private final Button levelEditButton;

		@Position(14)
		private final Button blacklistButton;

		private SettingsEditMenu(final String typeName) {
			super(SettingsMenu.this, true);

			this.typeName = typeName;
			this.settings = TurretSettings.findTurretSettings(typeName);

			this.setSize(9 * 4);
			this.setTitle(StringUtil.capitalize(typeName) + " Turrets");

			this.levelEditButton = new ButtonMenu(new LevelMenu(1), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.blacklistButton = new ButtonMenu(new SettingsBlacklistMenu(SettingsEditMenu.this, viewer), CompMaterial.KNOWLEDGE_BOOK,
					"Turret Blacklist",
					"Click this button to edit",
					"your turrets blacklist.");
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
				super(SettingsEditMenu.this, true);

				final boolean nextLevelExists = turretLevel < settings.getLevelsSize() || settings.getLevelsSize() == 0;

				this.level = getOrMakeLevel(turretLevel);

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
							settings.createSettingsLevel();

						nextLevelMenu = new LevelMenu(turretLevel + 1); // TODO add level
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
						RangedValue.parse("0-100000"), (Double input) -> settings.setLevelPrice(this.level, input));
			}

			private TurretSettings.LevelData getOrMakeLevel(final int turretLevel) { // TODO get level 3 too
				TurretSettings.LevelData level = settings.getLevel(turretLevel);

				if (level == null)
					level = settings.addLevel();

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
				return new LevelMenu(this.level.getLevel());
			}

			private class TurretLootChancesMenu extends MenuContainerChances {

				TurretLootChancesMenu() {
					super(LevelMenu.this, true);

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

		public class SettingsBlacklistMenu extends Menu {

			@Position(14)
			private final Button mobBlacklistButton;

			@Position(12)
			private final Button playerBlacklistButton;

			public SettingsBlacklistMenu(final Menu parent, final Player player) {
				super(parent, true);

				this.setViewer(player);
				this.setSize(27);
				this.setTitle("Turret Blacklist");

				this.mobBlacklistButton = new ButtonMenu(new MobBlacklistMenu(), CompMaterial.CREEPER_HEAD,
						"Mob Blacklist", "Edit your mob blacklist");

				this.playerBlacklistButton = new ButtonMenu(new PlayerBlacklistMenu(), CompMaterial.PLAYER_HEAD,
						"Player Blacklist", "Edit your player blacklist");
			}

			@Override
			public Menu newInstance() {
				return new SettingsBlacklistMenu(getParent(), this.getViewer());
			}

			private class MobBlacklistMenu extends MenuPagged<EntityType> {

				private final Button addButton;

				private MobBlacklistMenu() {
					super(27, SettingsBlacklistMenu.this, settings.getMobBlacklist());

					this.setTitle("Mob Blacklist");

					this.addButton = new ButtonMenu(new MobBlacklistMenu.MobSelectionMenu(), CompMaterial.ENDER_CHEST,
							"Add Mob",
							"Open this menu to add ",
							"mobs from the blacklist",
							"to prevent the turret",
							"from targeting them.");
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

					return super.getItemAt(slot);
				}

				@Override
				protected String[] getInfo() {
					return new String[]{
							"Edit your mob blacklist by",
							"clicking the existing eggs",
							"to remove them or clicking",
							"the 'Add Mob' button to add",
							"mobs to your blacklist."
					};
				}

				@Override
				public Menu newInstance() {
					return new MobBlacklistMenu();
				}

				private class MobSelectionMenu extends MenuPagged<EntityType> {
					private MobSelectionMenu() {
						super(27, MobBlacklistMenu.this, Arrays.stream(EntityType.values())
								.filter(EntityType::isAlive)
								.collect(Collectors.toList()), true);

						this.setTitle("Select a Mob");
					}

					@Override
					protected ItemStack convertToItemStack(final EntityType entityType) {
						return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
								.glow(settings.getMobBlacklist().contains(entityType))
								.lore(settings.getMobBlacklist().contains(entityType) ? "&aAlready blacklisted" : "Click to add")
								.make();
					}

					@Override
					protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
						settings.addMobToBlacklist(entityType);
						this.restartMenu("&aAdded " + entityType.name());
					}
				}
			}

			private class PlayerBlacklistMenu extends MenuPagged<UUID> {

				private final Button addButton;

				private final Button addPromptButton;

				private PlayerBlacklistMenu() {
					super(27, SettingsBlacklistMenu.this, settings.getPlayerBlacklist(), true);

					this.setTitle("Player Blacklist");

					this.addButton = new ButtonMenu(new PlayerBlacklistMenu.PlayerSelectionMenu(), CompMaterial.ENDER_CHEST,
							"Add Players",
							"Open this menu to add ",
							"players to the blacklist",
							"to prevent the turret",
							"from targeting them.");

					this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
							ItemCreator.of(CompMaterial.WRITABLE_BOOK, "Type a name",
									"Click this button if you",
									"would like to add a player",
									"to the blacklist by typing ",
									"his name, this means you can",
									"also add offline players."));
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

					return super.getItemAt(slot);
				}

				@Override
				protected String[] getInfo() {
					return new String[]{
							"Edit your player blacklist by",
							"clicking the existing heads",
							"to remove them or clicking",
							"the 'Add Mob' button to add",
							"players to your blacklist."
					};
				}

				@Override
				public Menu newInstance() {
					return new PlayerBlacklistMenu();
				}

				private class PlayerSelectionMenu extends MenuPagged<Player> {
					private PlayerSelectionMenu() {
						super(18, PlayerBlacklistMenu.this, viewer.getWorld().getPlayers());

						this.setTitle("Select a player");
					}

					@Override
					protected ItemStack convertToItemStack(final Player player) {
						return ItemCreator.of(
										CompMaterial.PLAYER_HEAD,
										player.getName(),
										(settings.getPlayerBlacklist().contains(player.getUniqueId()) ? "&aAlready blacklisted" : "Click to add"))
								.skullOwner(player.getName()).make();
					}

					@Override
					protected void onPageClick(final Player player, final Player item, final ClickType click) {
						settings.addPlayerToBlacklist(item.getUniqueId());
						System.out.println("Blacklist : " + settings.getPlayerBlacklist());
						this.restartMenu("&aAdded " + player.getName());
					}
				}
			}

			private final class PlayerBlacklistPrompt extends SimplePrompt {

				@Override
				protected String getPrompt(final ConversationContext context) {
					return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
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
					tellSuccess("You have added " + input + " to the blacklist!");

					return END_OF_CONVERSATION;
				}
			}
		}
	}
}