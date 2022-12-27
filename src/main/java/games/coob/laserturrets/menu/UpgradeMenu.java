package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
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

public class UpgradeMenu extends Menu {

	private final TurretData turretData;

	private final int level;

	private final Button upgradeButton;

	private final Button blacklistButton;

	public UpgradeMenu(final TurretData turretData, final int turretLevel, final Player player) {
		this.setSize(27);
		this.setTitle(Lang.of("Upgrade_Menu.Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType())), "{level}", turretLevel));
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
						animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", MathUtil.formatTwoDigits(price - funds), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
					else {
						cache.takeCurrency(price, false);
						TurretRegistry.getInstance().setCurrentTurretLevel(turretData, nextLevel);

						CompSound.LEVEL_UP.play(player);

						Common.runLater(() -> {
							final Menu newMenu = new UpgradeMenu(turretData, nextLevel, player);

							newMenu.displayTo(player);
							Common.runLater(() -> newMenu.animateTitle(Lang.of("Upgrade_Menu.Upgrade_Animated_Message", "{nextLevel}", nextLevel)));
						});

						turretData.getHologram().update(turretData);
					}
				}
			}

			@Override
			public ItemStack getItem() {
				String[] lore = new String[0];

				if (!hasMaxTier) {
					final double price = turretData.getLevel(nextLevel).getPrice();

					lore = Lang.ofArray("Upgrade_Menu.Upgrade_Button_Lore", "{price}", price, "{level}", turretData.getCurrentLevel(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME);
				}

				return ItemCreator
						.of(!hasMaxTier ? CompMaterial.ENDER_EYE : CompMaterial.BARRIER)
						.name(!hasMaxTier ? Lang.of("Upgrade_Menu.Upgrade_Button_Title") : Lang.of("Upgrade_Menu.Upgrade_Button_Title_Max_Level"))
						.lore(lore)
						.make();
			}
		};

		this.blacklistButton = new ButtonMenu(new AlliesMenu(UpgradeMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
				Lang.of("Upgrade_Menu.Allies_Button_Title"),
				Lang.ofArray("Upgrade_Menu.Allies_Button_Lore"));
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
		return Lang.ofArray("Upgrade_Menu.Info_Button", "{balance}", PlayerCache.from(getViewer()).getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME);
	}
}
