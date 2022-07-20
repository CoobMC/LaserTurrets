package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.Valid;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TurretSelectionMenu extends MenuPagged<TurretData> { // TODO Upgrade turret menu

	private ViewMode viewMode;

	private final Button changeTypeButton;

	private TurretSelectionMenu(final Player player, final ViewMode viewMode) {
		super(9 * 4, compileTurrets(viewMode));

		this.viewMode = viewMode;

		this.setTitle(viewMode.typeName + " Turrets");
		this.setSize(9 * 3);
		this.setViewer(player);

		this.changeTypeButton = new ButtonConversation(new EditMenuTypePrompt(),
				ItemCreator.of(CompMaterial.BEACON, "Change View Type",
						"",
						"Click this button if you",
						"would like to view turrets",
						"of a different type."));
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

	private static List<TurretData> compileTurrets(final ViewMode viewMode) {
		return new ArrayList<>(viewMode.turretTypeList);
	}

	@Override
	protected ItemStack convertToItemStack(final TurretData turretData) {
		final int level = turretData.getCurrentLevel();
		final String id = turretData.getId();
		final String type = turretData.getType();
		final List<String> lore = new ArrayList<>();

		lore.add("Level: " + level);
		lore.add("Type: " + type);
		lore.add("");
		lore.add("Click to edit this turret");

		if (this.viewMode.typeName.equals("All"))
			return ItemCreator.of(turretData.getMaterial()).name("&b" + capitalize(type) + " Turret &8" + id).lore(lore).makeMenuTool();
		else if (type.equalsIgnoreCase(this.viewMode.typeName))
			return ItemCreator.of(turretData.getMaterial()).name("&f" + capitalize(type) + " Turret &7" + id).lore(lore).makeMenuTool();

		return NO_ITEM;
	}

	public String capitalize(final String string) {
		if (string == null || string.length() <= 1) return string;
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	@Override
	protected void onPageClick(final Player player, final TurretData turretData, final ClickType clickType) {
		new TurretEditMenu(turretData);
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot == getSize() - 1)
			return changeTypeButton.getItem();

		return super.getItemAt(slot);
	}

	@Override
	public Menu newInstance() {
		return new TurretSelectionMenu(getViewer(), viewMode);
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
			return "&6Enter one of the following turret types (all, arrow, flame, laser)";
		}

		@Override
		protected boolean isInputValid(final ConversationContext context, final String input) {
			return input.equals("all") || input.equals("arrow") || input.equals("flame") || input.equals("laser");
		}

		@Override
		protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
			return "Type " + invalidInput + " does not exist, choose one of the following (all, arrow, flame, laser).";
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
			viewMode = ViewMode.valueOf(input.toUpperCase());

			return Prompt.END_OF_CONVERSATION;
		}
	}

	private final class TurretEditMenu extends Menu { // TODO make an upgrade menu

		private final TurretData turretData;

		@Position(2)
		private final Button levelEditButton;

		@Position(4)
		private final Button playerBlacklistButton;

		@Position(5)
		private final Button mobBlacklistButton;

		@Position(6)
		private final Button teleportButton;

		TurretEditMenu(final TurretData turretData) {
			super(TurretSelectionMenu.this);

			this.turretData = turretData;

			this.setSize(9 * 4);
			this.setTitle(viewMode.typeName + " Turrets");

			this.levelEditButton = new ButtonMenu(new LevelMenu(turretData, turretData.getCurrentLevel()), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.playerBlacklistButton = new ButtonConversation(new PlayerBlacklistPrompt(), CompMaterial.KNOWLEDGE_BOOK,
					"Player Blacklist",
					"",
					"Click to add or remove",
					"players from the blacklist",
					"to prevent the turret from",
					"targeting specific players.");

			this.mobBlacklistButton = new ButtonMenu(new MobBlackListMenu(), CompMaterial.CREEPER_HEAD,
					"Mob Blacklist",
					"",
					"Open this menu to add or",
					"remove mobs from the",
					"blacklist to prevent the",
					"turret from targeting them.");

			this.teleportButton = Button.makeSimple(CompMaterial.ENDER_EYE, "Teleport", "Teleport to the turret/nto visit it.", player1 -> {
				player1.teleport(this.turretData.getLocation());

				Messenger.success(player1, "You have successfully teleported to the " + this.turretData.getType() + " turret with the id of &2" + this.turretData.getId());
			});
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit this turrets settings."
			};
		}

		private class MobBlackListMenu extends MenuPagged<EntityType> {

			private final Button addButton;

			private MobBlackListMenu() {
				super(27, TurretSelectionMenu.this, turretData.getMobBlackList());

				this.setTitle("Mob Blacklist");

				this.addButton = new ButtonMenu(new MobSelectionMenu(), CompMaterial.CREEPER_HEAD,
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
				TurretRegistry.getInstance().removeMobFromBlacklist(turretData, entityType);
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
					super(9, MobBlackListMenu.this, Arrays.stream(EntityType.values())
							.filter(EntityType::isAlive)
							.collect(Collectors.toList()));

					this.setTitle("Select a Mob");
				}

				@Override
				protected ItemStack convertToItemStack(final EntityType entityType) {
					return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType)).make();
				}

				@Override
				protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
					TurretRegistry.getInstance().addMobToBlacklist(turretData, entityType);
					this.animateTitle("&aAdded " + entityType.name() + "to the mob blacklist.");
				}
			}
		}

		private class LevelMenu extends Menu {

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

			public LevelMenu(final TurretData turretData, final int turretLevel) {
				super(TurretEditMenu.this);

				Valid.checkBoolean(turretLevel < 3 + 2, "Cannot jump more than 2 levels ahead in turret level menu.");

				final boolean nextLevelExists = turretLevel <= turretData.getLevels() || turretData.getLevels() == 0;
				final TurretRegistry registry = TurretRegistry.getInstance();

				this.turretData = turretData;
				this.turretLevel = turretLevel;
				this.level = getOrMakeLevel(turretLevel);

				this.setTitle("Turret Level");
				this.setSize(9 * 4);
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
							new LevelMenu(turretData, turretLevel - 1).displayTo(player);
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
							final Menu nextLevelMenu = new LevelMenu(turretData, turretLevel + 1);

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

		private final class PlayerBlacklistPrompt extends SimplePrompt {

			@Override
			protected String getPrompt(final ConversationContext context) {
				return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
			}

			@Nullable
			@Override
			protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
				final TurretRegistry registry = TurretRegistry.getInstance();

				registry.addPlayerToBlacklist(turretData, input);
				tellSuccess("You have added " + input + " to the blacklist!");
				return END_OF_CONVERSATION;
			}
		}
	}

	@RequiredArgsConstructor
	private enum ViewMode {
		ALL("All", TurretRegistry.getInstance().getRegisteredTurrets()),
		ARROW("Arrow", TurretRegistry.getInstance().getArrowTurrets()),
		FLAME("Flame", TurretRegistry.getInstance().getFlameTurrets()),
		LASER("Laser", TurretRegistry.getInstance().getLaserTurrets());

		private final String typeName;
		private final Set<TurretData> turretTypeList;
	}

	public static void openAllTurretsSelectionMenu(final Player player) {
		new TurretSelectionMenu(player, ViewMode.ALL).displayTo(player);
	}

	public static void openArrowTurretsSelectionMenu(final Player player) {
		new TurretSelectionMenu(player, ViewMode.ARROW).displayTo(player);
	}

	public static void openFlameTurretsSelectionMenu(final Player player) {
		new TurretSelectionMenu(player, ViewMode.FLAME).displayTo(player);
	}

	public static void openLaserTurretsSelectionMenu(final Player player) {
		new TurretSelectionMenu(player, ViewMode.LASER).displayTo(player);
	}
}