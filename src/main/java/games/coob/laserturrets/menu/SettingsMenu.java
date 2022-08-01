package games.coob.laserturrets.menu;

import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class SettingsMenu extends Menu {

	@Position(11)
	private final Button arrowSettingsButton;

	@Position(13)
	private final Button laserSettingsButton;

	@Position(15)
	private final Button flameSettingsButton;

	SettingsMenu(final @Nullable Menu parent, final Player player) {
		super(parent);

		this.setSize(27);
		this.setTitle("DefaultSettings");
		this.setViewer(player);

		this.arrowSettingsButton = new ButtonMenu(new SettingsEditMenu("arrow"), CompMaterial.ARROW,
				"Arrow Turret Settings",
				"",
				"Edit the default settings",
				"for arrow turrets.");

		this.flameSettingsButton = new ButtonMenu(new SettingsEditMenu("flame"), CompMaterial.ARROW,
				"Flame Turret Settings",
				"",
				"Edit the default settings",
				"for flame turrets.");

		this.laserSettingsButton = new ButtonMenu(new SettingsEditMenu("laser"), CompMaterial.ARROW,
				"Laser Turret Settings",
				"",
				"Edit the default settings",
				"for laser turrets.");
	}

	private final class SettingsEditMenu extends Menu {

		private final TurretSettings settings;

		@Position(12)
		private final Button levelEditButton;

		@Position(14)
		private final Button blacklistButton;

		private SettingsEditMenu(final String typeName) {
			super(SettingsMenu.this);

			this.settings = TurretSettings.findTurretSettings(typeName);

			this.setSize(9 * 4);
			this.setTitle(StringUtil.capitalize(typeName) + " Turrets");

			this.levelEditButton = new ButtonMenu(new LevelMenu(1), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.blacklistButton = new ButtonMenu(new BlacklistMenu(SettingsEditMenu.this, this.getViewer()), CompMaterial.KNOWLEDGE_BOOK,
					"Turret Blacklist",
					"",
					"Click this button to edit",
					"your turrets blacklist.");
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit this turrets settings."
			};
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
				super(SettingsEditMenu.this);

				final boolean nextLevelExists = turretLevel < settings.getLevels().size() || settings.getLevels().size() == 0;

				this.level = getOrMakeLevel(turretLevel);

				this.setTitle("Turret Level " + turretLevel);
				this.setSize(9 * 4);

				this.rangeButton = Button.makeIntegerPrompt(ItemCreator.of(CompMaterial.BOW).name("Turret Range")
								.lore("Set the turrets range", "by clicking this button.", "", "Current: &9" + level.getRange()),
						"Type in an integer value between 1 and 40 (recommend value : 15-20)",
						new RangedValue(1, 40), level::getRange, (Integer input) -> settings.setSettingsRange(level, input));


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

						return ItemCreator.of(isEnabled ? CompMaterial.GREEN_CONCRETE : CompMaterial.RED_CONCRETE, "Enabled/Disable Laser",
								"",
								"Current: " + (isEnabled ? "&atrue" : "&cfalse"),
								"",
								"Click to enable or disable",
								"lasers for this turret.").make();
					}
				};

				this.laserDamageButton = Button.makeDecimalPrompt(ItemCreator.of(CompMaterial.END_CRYSTAL).name("Laser Damage")
								.lore("Set the amount of damage", "lasers deal if they're enabled", "by clicking this button.", "", "Current: &9" + level.getLaserDamage()),
						"Type in an integer value between 1 and 40 (recommended value: 15-20)",
						new RangedValue(1, 40), level::getLaserDamage, (Double input) -> settings.setLaserDamage(level, input));

				this.lootButton = new ButtonMenu(new LevelMenu.TurretLootChancesMenu(), CompMaterial.CHEST,
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

						if (nextLevelExists) {
							nextLevelMenu = new LevelMenu(turretLevel + 1); // TODO add level
							System.out.println("Exists");
						} else {
							settings.createSettingsLevel();
							nextLevelMenu = new LevelMenu(turretLevel + 1);
							System.out.println("Doesn't exist");
						}

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
								"",
								"Current: " + this.level.getPrice() + " coins",
								"",
								"Edit the price for",
								"this level."),
						"Enter teh price for this level. Curretnt: " + this.level.getPrice() + " coins.",
						RangedValue.parse("0-100000"), (Double input) -> settings.setLevelPrice(level, input));
			}

			private TurretSettings.LevelData getOrMakeLevel(final int turretLevel) { // TODO get level 3 too
				TurretSettings.LevelData level = settings.getLevels().get(turretLevel - 1);

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

		public class BlacklistMenu extends Menu {

			@Position(13)
			private final Button mobBlacklistButton;

			@Position(17)
			private final Button playerBlacklistButton;

			public BlacklistMenu(final Menu parent, final Player player) {
				super(parent);

				this.setViewer(player);
				this.setSize(27);
				this.setTitle("Turret Blacklist");

				this.mobBlacklistButton = new ButtonMenu(new MobBlacklistMenu(), CompMaterial.CREEPER_HEAD.toItem());

				this.playerBlacklistButton = new ButtonMenu(new PlayerBlacklistMenu(), CompMaterial.PLAYER_HEAD.toItem());
			}

			private class MobBlacklistMenu extends MenuPagged<EntityType> {

				private final Button addButton;

				private MobBlacklistMenu() {
					super(27, BlacklistMenu.this, settings.getMobBlacklist());

					this.setTitle("Mob Blacklist");

					this.addButton = new ButtonMenu(new MobBlacklistMenu.MobSelectionMenu(), CompMaterial.CREEPER_HEAD,
							"Add Mob",
							"",
							"Open this menu to add ",
							"mobs from the blacklist",
							"to prevent the turret",
							"from targeting them.");
				}

				@Override
				protected ItemStack convertToItemStack(final EntityType entityType) {
					return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType)).make();
				}

				@Override
				protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
					settings.removeMobFromBlacklist(entityType);
					this.animateTitle("&cRemoved " + entityType.name() + "from the mob blacklist.");
				}

				@Override
				public ItemStack getItemAt(final int slot) {
					if (slot == this.getBottomCenterSlot())
						return addButton.getItem();

					return NO_ITEM;
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

				private class MobSelectionMenu extends MenuPagged<EntityType> {
					private MobSelectionMenu() {
						super(9, MobBlacklistMenu.this, Arrays.stream(EntityType.values())
								.filter(EntityType::isAlive)
								.collect(Collectors.toList()));

						this.setTitle("Select a Mob");
					}

					@Override
					protected ItemStack convertToItemStack(final EntityType entityType) {
						return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType)).make();
					}

					@Override
					protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
						settings.addMobToBlacklist(entityType);
						this.animateTitle("&aAdded " + entityType.name() + "to the mob blacklist.");
					}
				}
			}

			private class PlayerBlacklistMenu extends MenuPagged<Player> {

				private final Button addButton;

				private final Button addPromptButton;

				private PlayerBlacklistMenu() {
					super(27, BlacklistMenu.this, games.coob.laserturrets.menu.BlacklistMenu.compileBlacklistedPlayers(settings.getPlayerBlacklist()));

					this.setTitle("Player Blacklist");

					this.addButton = new ButtonMenu(new PlayerBlacklistMenu.PlayerSelectionMenu(this.getViewer()), CompMaterial.CREEPER_HEAD,
							"Add Players",
							"",
							"Open this menu to add ",
							"players to the blacklist",
							"to prevent the turret",
							"from targeting them.",
							"These players would be",
							"considered as allies.");

					this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
							ItemCreator.of(CompMaterial.BEACON, "Type a name",
									"",
									"Click this button if you",
									"would like to add a player",
									"to the blacklist by typing ",
									"his name, this means you can",
									"also add offline players."));
				}

				@Override
				protected ItemStack convertToItemStack(final Player player) {
					return ItemCreator.of(
									CompMaterial.PLAYER_HEAD,
									player.getName(),
									"",
									"Click to remove",
									player.getName())
							.skullOwner(player.getName()).make();
				}

				@Override
				protected void onPageClick(final Player player, final Player item, final ClickType click) {
					settings.removePlayerFromBlacklist(item.getUniqueId());
					this.animateTitle("&cRemoved " + item.getName() + "from the blacklist.");
				}

				@Override
				public ItemStack getItemAt(final int slot) {
					if (slot == this.getBottomCenterSlot() - 1)
						return addButton.getItem();
					if (slot == this.getBottomCenterSlot() + 1)
						return addPromptButton.getItem();

					return NO_ITEM;
				}

				@Override
				protected String[] getInfo() {
					return new String[]{
							"Edit your player blacklist by",
							"clicking the existing heads",
							"to remove them or clicking",
							"the 'Add Mob' button to add",
							"mobs to your blacklist."
					};
				}

				private class PlayerSelectionMenu extends MenuPagged<Player> {
					private PlayerSelectionMenu(final Player player) {
						super(9, PlayerBlacklistMenu.this, compileWorldPlayers(player));

						this.setTitle("Select a player");
					}

					@Override
					protected ItemStack convertToItemStack(final Player player) {
						return ItemCreator.of(
										CompMaterial.PLAYER_HEAD,
										player.getName(),
										"",
										"Click to add",
										player.getName())
								.skullOwner(player.getName()).make();
					}

					@Override
					protected void onPageClick(final Player player, final Player item, final ClickType click) {
						settings.addPlayerToBlacklist(player.getUniqueId());
						this.animateTitle("&aAdded " + player.getName() + "to the blacklist.");
					}
				}
			}

			private final class PlayerBlacklistPrompt extends SimplePrompt {

				@Override
				protected String getPrompt(final ConversationContext context) {
					return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
				}

				@org.jetbrains.annotations.Nullable
				@Override
				protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
					final Player player = Bukkit.getPlayer(input);

					if (player != null) {
						settings.addPlayerToBlacklist(player.getUniqueId());
						tellSuccess("You have added " + input + " to the blacklist!");
					} else tellError("Player " + input + " does not exist!");
					return END_OF_CONVERSATION;
				}
			}
		}
	}

	private static List<Player> compileWorldPlayers(final Player player) {
		final World world = player.getWorld();
		return world.getPlayers();
	}
}