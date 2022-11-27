package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;

import java.util.ArrayList;
import java.util.List;

public class UpgradeMenu extends Menu {

	private final TurretData turretData;

	private final int level;

	private final Button upgradeButton;

	private final Button blacklistButton;

	public UpgradeMenu(final TurretData turretData, final int turretLevel, final Player player) {
		this.setSize(27);
		this.setTitle(StringUtil.capitalize(turretData.getType()) + " Turret " + "(lvl " + turretLevel + ")");
		this.setViewer(player);

		this.turretData = turretData;
		this.level = turretLevel;

		final int currentLevel = turretData.getCurrentLevel();
		final int nextLevel = currentLevel + 1;
		final boolean hasMaxTier = currentLevel == turretData.getLevels();

		this.upgradeButton = new Button() {

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				if (!hasMaxTier) {
					final PlayerCache cache = PlayerCache.from(player);

					final double funds = cache.getCurrency(false);
					final double price = turretData.getLevel(nextLevel).getPrice();

					if (funds < price)
						animateTitle("&cYou lack " + MathUtil.formatTwoDigits(price - funds) + " " + Settings.CurrencySection.CURRENCY_NAME + "!");
					else {
						cache.takeCurrency(price, false);
						TurretRegistry.getInstance().setCurrentTurretLevel(turretData, nextLevel);

						CompSound.LEVEL_UP.play(player);

						Common.runLater(() -> {
							final Menu newMenu = new UpgradeMenu(turretData, nextLevel, player);

							newMenu.displayTo(player);
							Common.runLater(() -> newMenu.animateTitle("&2Upgraded to level " + nextLevel));
						});
					}
				}
			}

			@Override
			public ItemStack getItem() {
				final List<String> lore = new ArrayList<>();

				if (!hasMaxTier) {
					final double price = turretData.getLevel(nextLevel).getPrice();

					lore.add("Current level: " + turretData.getCurrentLevel());
					lore.add("");
					lore.add("Price: " + price + " " + Settings.CurrencySection.CURRENCY_NAME);
				}

				return ItemCreator
						.of(!hasMaxTier ? CompMaterial.ENDER_EYE : CompMaterial.BARRIER)
						.name(!hasMaxTier ? "&aUpgrade" : "&cMax level")
						.lore(lore)
						.make();
			}
		};

		this.blacklistButton = new ButtonMenu(new AlliesMenu(UpgradeMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
				"Turret Allies",
				"Click this button to edit",
				"your turrets allies.");
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot == this.getCenterSlot() + 1)
			return this.upgradeButton.getItem();
		if (slot == this.getCenterSlot() - 1)
			return this.blacklistButton.getItem();

		return NO_ITEM;
	}

	@Override
	public Menu newInstance() {
		return new UpgradeMenu(this.turretData, this.level, this.getViewer());
	}

	@Override
	protected String[] getInfo() {
		return new String[]{
				"Modify your player blacklist",
				"or upgrade this turret.",
				"",
				"&eBalance: " + PlayerCache.from(getViewer()).getCurrency(false) + " " + Settings.CurrencySection.CURRENCY_NAME
		};
	}
}
