package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainer;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.StartPosition;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.List;

public class BrokenTurretMenu extends Menu {

	private final TurretData turretData;

	private final ViewMode viewMode;

	private final Button repairButton;

	private final Button lootTurretButton;

	private final Button destroyButton;

	private final String currencyName = Settings.CurrencySection.CURRENCY_NAME;

	private TurretSettings settings;

	public BrokenTurretMenu(final TurretData turretData, final ViewMode viewMode) {
		this.viewMode = viewMode;
		this.turretData = turretData;
		this.settings = TurretSettings.findByName(turretData.getType());

		this.setTitle(viewMode.menuTitle);
		this.setSize(27);

		final double price = this.settings.getLevel(turretData.getCurrentLevel()).getPrice();

		this.repairButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final PlayerCache cache = PlayerCache.from(player);

				if (cache.getCurrency(false) - price < 0) {
					animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", price - cache.getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
					return;
				}

				cache.takeCurrency(price, false);
				CompSound.ANVIL_USE.play(turretData.getLocation());
				turretData.setBroken(false);
				player.closeInventory();
				Common.tell(player, Lang.of("Broken_Turret_Menu.Repair_Button_Click_Message", "{price}", price, "{currencyName}", currencyName));

				if (Settings.TurretSection.DISPLAY_HOLOGRAM) {
					final String[] loreHologram = Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
					turretData.getHologram().updateLore(loreHologram);
				}
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.DAMAGED_ANVIL, Lang.of("Broken_Turret_Menu.Repair_Button_Title"),
						Lang.ofArray("Broken_Turret_Menu.Repair_Button_Lore", "{price}", price, "{currencyName}", currencyName)).make();
			}
		};

		this.lootTurretButton = new ButtonMenu(new LootTurretMenu(), ItemCreator.of(CompMaterial.CHEST, Lang.of("Broken_Turret_Menu.Loot_Button_Title"),
				Lang.ofArray("Broken_Turret_Menu.Loot_Button_Lore")).make());

		this.destroyButton = new Button() {
			final double earnings = settings.getLevel(turretData.getCurrentLevel()).getPrice() / 2;

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final PlayerCache cache = PlayerCache.from(player);

				CompSound.EXPLODE.play(turretData.getLocation());
				turretData.unregister();
				cache.giveCurrency(this.earnings, false);
				player.closeInventory();
				Common.tell(player, Lang.of("Broken_Turret_Menu.Destroy_Button_Click_Message", "{earnings}", this.earnings, "{currencyName}", currencyName));
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.TNT, Lang.of("Broken_Turret_Menu.Destroy_Button_Title"),
						Lang.ofArray("Broken_Turret_Menu.Destroy_Button_Lore", "{earnings}", this.earnings, "{currencyName}", currencyName)).make();
			}
		};
	}

	@Override
	protected String[] getInfo() {
		final double balance = PlayerCache.from(getViewer()).getCurrency(false);
		final String[] strings = Lang.ofArray("Broken_Turret_Menu.Info_Button", "{balance}", balance, "{currencyName}", currencyName);
		final String[] stringsOwner = Lang.ofArray("Broken_Turret_Menu.Info_Button_For_Owner", "{balance}", balance, "{currencyName}", currencyName);

		return this.viewMode == ViewMode.OWNER ? stringsOwner : strings;
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (this.viewMode == ViewMode.OWNER) {
			if (slot == this.getCenterSlot() + 2)
				return this.destroyButton.getItem();
			if (slot == this.getCenterSlot() - 2)
				return this.repairButton.getItem();
		}

		if (slot == this.getCenterSlot())
			return this.lootTurretButton.getItem();

		return NO_ITEM;
	}

	private class LootTurretMenu extends MenuContainer {

		@Position(start = StartPosition.BOTTOM_CENTER)
		private final Button lootAllButton;

		LootTurretMenu() {
			super(BrokenTurretMenu.this);

			this.setSize(27);
			this.setTitle(Lang.of("Broken_Turret_Menu.Loot_Menu_Title"));

			this.lootAllButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
					if (turretData.getCurrentLoot() == null)
						return;

					PlayerUtil.addItems(player.getInventory(), turretData.getCurrentLoot());
					turretData.setCurrentLoot(new ArrayList<>());
					CompSound.LAVA_POP.play(player);
					restartMenu(Lang.of("Broken_Turret_Menu.Loot_All_Button_Animated_Message"));
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.FEATHER, Lang.of("Broken_Turret_Menu.Loot_All_Button_Title"),
							Lang.ofArray("Broken_Turret_Menu.Loot_All_Button_Lore")).make();
				}
			};
		}

		@Override
		protected boolean canEditItem(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor, final InventoryAction action) {
			return action != InventoryAction.PLACE_ALL || location != MenuClickLocation.MENU;
		}

		@Override
		protected ItemStack onItemClick(final int slot, final ClickType clickType, @Nullable final ItemStack item) {
			if (turretData.getCurrentLoot() == null)
				return item;

			turretData.removeCurrentLoot(item);

			return item;
		}

		@Override
		protected void onMenuClose(final StrictMap<Integer, ItemStack> items) {
		}

		@Override
		protected ItemStack getDropAt(final int slot) {
			final List<ItemStack> items = turretData.getCurrentLoot();

			if (turretData.getCurrentLoot() == null)
				return NO_ITEM;

			return slot < items.size() ? items.get(slot) : NO_ITEM;
		}

		@Override
		protected String[] getInfo() {
			return Lang.ofArray("Broken_Turret_Menu.Loot_Menu_Info_Button");
		}

		@Override
		public Menu newInstance() {
			return new LootTurretMenu();
		}
	}

	@RequiredArgsConstructor
	private enum ViewMode {
		OWNER(Lang.of("Broken_Turret_Menu.Menu_Title_For_Owner")),
		PLAYER(Lang.of("Broken_Turret_Menu.Menu_Title"));

		private final String menuTitle;
	}

	public static void openOwnerMenu(final TurretData turretData, final Player player) {
		new BrokenTurretMenu(turretData, ViewMode.OWNER).displayTo(player);
	}

	public static void openPlayerMenu(final TurretData turretData, final Player player) {
		new BrokenTurretMenu(turretData, ViewMode.PLAYER).displayTo(player);
	}
}
