package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowUseTurretTool;
import games.coob.laserturrets.tools.BeamUseTurretTool;
import games.coob.laserturrets.tools.FireballUseTurretTool;
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

		this.setTitle("Turret Shop");
		this.setSize(27);
		this.setViewer(player);

		this.arrowTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("arrow").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle("&cYou lack " + (buyPrice - cache.getCurrency(false)) + " " + Settings.CurrencySection.CURRENCY_NAME);
					return;
				}

				ArrowUseTurretTool.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu("&aPurchased Arrow Turret");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("arrow").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.ARROW, "&aArrow Turret",
						"Click to purchase this tool", "that will allow you to", "create an arrow turret.", "", "Price: " + buyPrice).make();
			}
		};

		this.fireballTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("fireball").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle("&cYou lack " + (buyPrice - cache.getCurrency(false)) + " " + Settings.CurrencySection.CURRENCY_NAME);
					return;
				}

				FireballUseTurretTool.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu("&aPurchased a Fireball Turret");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("fireball").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.FIRE_CHARGE, "&aFireball Turret",
						"Click to purchase this tool", "that will allow you to", "create a fireball turret.", "", "Price: " + buyPrice).make();
			}
		};

		this.beamTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("beam").getLevels().get(0).getPrice();

				if (cache.getCurrency(false) - buyPrice < 0) {
					animateTitle("&cYou lack " + (buyPrice - cache.getCurrency(false)) + " " + Settings.CurrencySection.CURRENCY_NAME);
					return;
				}

				BeamUseTurretTool.getInstance().give(player);
				cache.takeCurrency(buyPrice, false);
				restartMenu("&aPurchased a Beam Turret");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("beam").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.BLAZE_ROD, "&aBeam Turret",
						"Click to purchase this tool", "that will allow you to", "create a beam turret.", "", "Price: " + buyPrice).make();
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
				"&eYou currently have " + PlayerCache.from(this.getViewer()).getCurrency(false) + " " + Settings.CurrencySection.CURRENCY_NAME
		};
	}

	@Override
	public Menu newInstance() {
		return new ShopMenu(getViewer());
	}
}



