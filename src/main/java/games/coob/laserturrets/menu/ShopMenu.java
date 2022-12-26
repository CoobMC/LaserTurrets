package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowTurret;
import games.coob.laserturrets.tools.BeamTurret;
import games.coob.laserturrets.tools.FireballTurret;
import games.coob.laserturrets.util.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

public class ShopMenu extends Menu {

	private final PlayerCache cache;

	@Position(11)
	private final Button arrowTurretButton;

	@Position(13)
	private final Button beamTurretButton;

	@Position(15)
	private final Button fireballTurretButton;

	public ShopMenu(final Player player) {
		this.cache = PlayerCache.from(player);

		this.setTitle(Lang.of("Shop_Menu.Menu_Title"));
		this.setSize(27);
		this.setViewer(player);

		this.arrowTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("arrow").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", buyPrice - cache.getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
					return;
				}

				ArrowTurret.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu(Lang.of("Shop_Menu.Arrow_Turret_Purchase_Animated_Message", "{price}", buyPrice));
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("arrow").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.ARROW, Lang.of("Shop_Menu.Arrow_Turret_Button_Title"),
						Lang.ofArray("Shop_Menu.Arrow_Turret_Button_Lore", "{price}", buyPrice)).make();
			}
		};

		this.fireballTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("fireball").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", buyPrice - cache.getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
					return;
				}

				FireballTurret.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu(Lang.of("Shop_Menu.Fireball_Turret_Purchase_Animated_Message", "{price}", buyPrice));
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("fireball").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.FIRE_CHARGE, Lang.of("Shop_Menu.Fireball_Turret_Button_Title"),
						Lang.ofArray("Shop_Menu.Fireball_Turret_Button_Lore", "{price}", buyPrice)).make();
			}
		};

		this.beamTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("beam").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", buyPrice - cache.getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
					return;
				}

				BeamTurret.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu(Lang.of("Shop_Menu.Fireball_Turret_Purchase_Animated_Message", "{price}", buyPrice));
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("beam").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.BLAZE_ROD, Lang.of("Shop_Menu.Beam_Turret_Button_Title"),
						Lang.ofArray("Shop_Menu.Beam_Turret_Button_Lore", "{price}", buyPrice)).make();
			}
		};
	}

	@Override
	protected String[] getInfo() {
		return Lang.ofArray("Shop_Menu.Info_Button", "{balance}", PlayerCache.from(this.getViewer()).getCurrency(false), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME);
	}

	@Override
	public Menu newInstance() {
		return new ShopMenu(getViewer());
	}
}



