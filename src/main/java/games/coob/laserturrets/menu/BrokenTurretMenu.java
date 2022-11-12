package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainer;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.StartPosition;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.List;

public class BrokenTurretMenu extends Menu {

	private final ViewMode viewMode;

	private final Button repairButton;

	private final Button lootTurretButton;

	private final Button destroyButton;

	public BrokenTurretMenu(final TurretData turretData, final ViewMode viewMode) {
		this.viewMode = viewMode;

		this.setTitle(viewMode.menuTitle);
		this.setSize(27);

		final double price = turretData.getLevel(turretData.getCurrentLevel()).getPrice();

		this.repairButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final TurretRegistry registry = TurretRegistry.getInstance();
				final PlayerCache cache = PlayerCache.from(player);

				CompSound.ANVIL_USE.play(turretData.getLocation());
				registry.setBroken(turretData, false);
				cache.takeCurrency(price, false);
				player.closeInventory();
				Common.tell(player, "&aYou have successfully repaired this turret and it costed you " + price + " " + Settings.CurrencySection.CURRENCY_NAME + ".");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.DAMAGED_ANVIL, "&aRepair Turret",
						"Click to repair this", "turret.", "",
						"Cost: " + price).make();
			}
		};

		this.lootTurretButton = new ButtonMenu(new LootTurretMenu(turretData), ItemCreator.of(CompMaterial.CHEST, "Turret Loot",
				"Claim loot from this", "destroyed turret.").make());

		this.destroyButton = new Button() {
			final double earnings = turretData.getLevel(turretData.getCurrentLevel()).getPrice() / 2;

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final TurretRegistry registry = TurretRegistry.getInstance();
				final PlayerCache cache = PlayerCache.from(player);

				CompSound.EXPLODE.play(turretData.getLocation());
				registry.unregister(turretData.getLocation().getBlock());
				cache.giveCurrency(this.earnings, false);
				player.closeInventory();
				Common.tell(player, "&6You have successfully destroyed this turret and you have earned " + this.earnings + " " + Settings.CurrencySection.CURRENCY_NAME + ".");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.TNT, "&cDestroy Turret", "Click to destroy turret", "", "You'll earn " + this.earnings + " " + Settings.CurrencySection.CURRENCY_NAME + ".").make();
			}
		};
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

		private final TurretData turretData;

		@Position(start = StartPosition.BOTTOM_CENTER)
		private final Button lootAllButton;

		LootTurretMenu(final TurretData turretData) {
			super(BrokenTurretMenu.this);

			this.turretData = turretData;

			this.setSize(27);
			this.setTitle("Turret Loot");

			this.lootAllButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
					if (turretData.getCurrentLoot() == null)
						return;

					PlayerUtil.addItems(player.getInventory(), turretData.getCurrentLoot());
					turretData.setCurrentLoot(null);
					CompSound.LAVA_POP.play(player);
					restartMenu("&aClaimed all turret loot");
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.FEATHER, "&aClaim all", "Click this button to", "claim all the items.").make();
				}
			};
		}

		@Override
		protected ItemStack getDropAt(final int slot) {
			final List<ItemStack> items = this.turretData.getCurrentLoot();

			if (this.turretData.getCurrentLoot() == null)
				return NO_ITEM;

			return slot < items.size() ? items.get(slot) : NO_ITEM;
		}

		@Override
		protected void onMenuClose(final StrictMap<Integer, ItemStack> items) {
			final TurretRegistry registry = TurretRegistry.getInstance();

			registry.setCurrentLoot(this.turretData, new ArrayList<>(items.values()));
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Claim loot from this broken",
					"turret before anyone else does!"
			};
		}
	}

	@RequiredArgsConstructor
	private enum ViewMode {
		OWNER("Repair Turret"),
		PLAYER("Loot Turret");

		private final String menuTitle;
	}

	public static void openOwnerMenu(final TurretData turretData, final Player player) {
		new BrokenTurretMenu(turretData, ViewMode.OWNER).displayTo(player);
	}

	public static void openPlayerMenu(final TurretData turretData, final Player player) {
		new BrokenTurretMenu(turretData, ViewMode.PLAYER).displayTo(player);
	}
}
