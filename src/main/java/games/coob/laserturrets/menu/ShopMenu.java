package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.FlameTurretTool;
import games.coob.laserturrets.tools.LaserTurretTool;
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

	@Position(10)
	private final Button arrowTurretButton;

	@Position(12)
	private final Button flameTurretButton;

	@Position(14)
	private final Button laserTurretButton;

	public ShopMenu(final Player player) {
		this.cache = PlayerCache.from(player);

		this.setTitle("Turret Shop");
		this.setSize(27);
		this.setViewer(player);

		this.arrowTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("arrow-turrets").getLevels().get(0).getPrice();

				ArrowTurretTool.giveOneUse(player);
				cache.takeCurrency(buyPrice, false);
				animateTitle("&aPurchased an Arrow Turret tool");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("arrow-turrets").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.ARROW, "&aArrow Turret",
						"Click to purchase this tool", "that will allow you to", "create an arrow turret.", "", "Price: " + buyPrice).make();
			}
		};

		this.flameTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("flame-turrets").getLevels().get(0).getPrice();

				FlameTurretTool.giveOneUse(player);
				cache.takeCurrency(buyPrice, false);
				animateTitle("&aPurchased a Flame Turret tool");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("flame-turrets").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.ARROW, "&aFlame Turret",
						"Click to purchase this tool", "that will allow you to", "create a flame turret.", "", "Price: " + buyPrice).make();
			}
		};

		this.laserTurretButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				final double buyPrice = TurretSettings.findTurretSettings("laser-turrets").getLevels().get(0).getPrice();

				LaserTurretTool.giveOneUse(player);
				cache.takeCurrency(buyPrice, false);
				animateTitle("&aPurchased a Laser Turret tool");
			}

			@Override
			public ItemStack getItem() {
				final double buyPrice = TurretSettings.findTurretSettings("laser-turrets").getLevels().get(0).getPrice();

				return ItemCreator.of(CompMaterial.ARROW, "&aLaser Turret",
						"Click to purchase this tool", "that will allow you to", "create a laser turret.", "", "Price: " + buyPrice).make();
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
}



