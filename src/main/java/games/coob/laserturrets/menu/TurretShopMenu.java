package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.FlameTurretTool;
import games.coob.laserturrets.tools.LaserTurretTool;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.StartPosition;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.List;

public class TurretShopMenu extends Menu { // TODO create config file for the price of each level


	private final PlayerCache cache;

	@Position(10)
	private final Button arrowTurretButton;

	@Position(12)
	private final Button flameTurretButton;

	@Position(14)
	private final Button laserTurretButton;

	public TurretShopMenu(final Player player) {
		this.cache = PlayerCache.from(player);

		this.setTitle("Turret Shop");
		this.setSize(27);
		this.setViewer(player);

		this.arrowTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				ArrowTurretTool.giveOneUse(player);
				cache.takeCurrency(Settings.DefaultLevel1TurretSection.PRICE, false);
				animateTitle("&aPurchased an Arrow Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aArrow Turret",
						"Click to purchase this tool", "that will allow you to", "create an arrow turret.", "", "Price: " + Settings.DefaultLevel1TurretSection.PRICE).make();
			}
		};

		this.flameTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				FlameTurretTool.giveOneUse(player);
				cache.takeCurrency(Settings.DefaultLevel1TurretSection.PRICE, false);
				animateTitle("&aPurchased a Flame Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aFlame Turret",
						"Click to purchase this tool", "that will allow you to", "create a flame turret.", "", "Price: " + Settings.DefaultLevel1TurretSection.PRICE).make();
			}
		};

		this.laserTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				LaserTurretTool.giveOneUse(player);
				cache.takeCurrency(Settings.DefaultLevel1TurretSection.PRICE, false);
				animateTitle("&aPurchased a Laser Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aLaser Turret",
						"Click to purchase this tool", "that will allow you to", "create a laser turret.", "", "Price: " + Settings.DefaultLevel1TurretSection.PRICE).make();
			}
		};
	}

	@Override
	protected String[] getInfo() {
		return new String[]{"In this menu you can purchase",
				"a turret tool that will allow",
				"you to create a turret. You may",
				"only create one turret with a tool,",
				"so use it wisely.",
				"",
				"You currently have" + PlayerCache.from(this.getViewer()).getCurrency(false) + " " + Settings.CurrencySection.CURRENCY_NAME
		};
	}

	public static class UpgradeMenu extends Menu {

		private final TurretData turretData;

		private final int turretLevel;

		@Position(start = StartPosition.CENTER)
		private final Button upgradeButton;

		public UpgradeMenu(final TurretData turretData, final int turretLevel) { // TODO allow owner to upgrade, set blacklist

			this.turretData = turretData;
			this.turretLevel = turretLevel;

			this.setSize(27);
			this.setTitle("Upgrade Turret");

			final int currentLevel = turretData.getCurrentLevel();
			final boolean mayBeUpgraded = currentLevel + 1 == turretLevel;
			final boolean upgraded = currentLevel < turretData.getLevels();
			final boolean hasMaxTier = currentLevel == turretData.getLevels();

			this.upgradeButton = new Button() {
				@Override
				public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
					if (mayBeUpgraded) {
						final PlayerCache cache = PlayerCache.from(player);

						final double funds = cache.getCurrency(false);
						final double price = turretData.getLevel(currentLevel + 1).getPrice();

						if (funds < price)
							animateTitle("You lack " + MathUtil.formatTwoDigits(price - funds) + " " + Settings.CurrencySection.CURRENCY_NAME + "!");

						else {
							cache.takeCurrency(price, false);
							TurretRegistry.getInstance().setCurrentTurretLevel(turretData, turretLevel);

							CompSound.LEVEL_UP.play(player);

							Common.runLater(() -> {
								final Menu newMenu = newInstance();

								newMenu.displayTo(player);
								Common.runLater(() -> newMenu.animateTitle("&2Upgraded turret to level" + turretLevel + " for " + funds + " " + Settings.CurrencySection.CURRENCY_NAME + "!"));
							});
						}
					}
				}

				@Override
				public ItemStack getItem() {
					final List<String> lore = new ArrayList<>();

					if (mayBeUpgraded) {
						lore.add("");
						lore.add("Price: " + turretData.getLevel(currentLevel + 1).getPrice() + " " + Settings.CurrencySection.CURRENCY_NAME);
					}

					return ItemCreator
							.of(mayBeUpgraded ? CompMaterial.ENDER_EYE : upgraded || hasMaxTier ? CompMaterial.ENDER_PEARL : CompMaterial.GLASS)
							.name(mayBeUpgraded ? "&6Click to upgrade" : upgraded ? "&2This turret has this level" : hasMaxTier ? "&7This turret has the max level" : "&7This is the first level")
							.lore(lore)
							.glow(mayBeUpgraded || upgraded || hasMaxTier)
							.make();
				}
			};
		}
	}
}

