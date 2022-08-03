package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu extends Menu {

	@Position(15)
	private final Button upgradeButton;

	@Position(17)
	private final Button blacklistButton;

	public UpgradeMenu(final TurretData turretData, final int turretLevel, final Player player) { // TODO allow owner to upgrade, set blacklist
		this.setSize(27);
		this.setTitle("Upgrade Turret");
		this.setViewer(player);

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

		this.blacklistButton = new ButtonMenu(new BlacklistMenu(UpgradeMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
				"Turret Blacklist",
				"",
				"Click this button to edit",
				"your turrets blacklist.");
	}
}
