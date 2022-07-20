package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.Settings;
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

public class TurretShopMenu extends Menu { // TODO destroy tool after a certain amount of uses

	private final TurretData turretData;

	private final PlayerCache cache;

	@Position(10)
	private final Button purchaserArrowButton;

	@Position(12)
	private final Button purchaserFlameButton;

	@Position(14)
	private final Button purchaserLaserButton;

	public TurretShopMenu(final TurretData turretData, final Player player) {
		this.turretData = turretData;
		this.cache = PlayerCache.from(player);

		this.setTitle("Turret Shop");
		this.setSize(27);
		this.setViewer(player);

		this.purchaserArrowButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				ArrowTurretTool.giveOneUse(player);
				cache.takeCurrency(turretData.getLevel(1).getPrice(), false);
				animateTitle("&aPurchased an Arrow Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aArrow Turret",
						"Click to purchase this tool", "that will allow you to", "create an arrow turret.", "", "Price: " + turretData.getLevel(1).getPrice()).make();
			}
		};

		this.purchaserFlameButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				FlameTurretTool.giveOneUse(player);
				cache.takeCurrency(turretData.getLevel(1).getPrice(), false);
				animateTitle("&aPurchased a Flame Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aFlame Turret",
						"Click to purchase this tool", "that will allow you to", "create a flame turret.", "", "Price: " + turretData.getLevel(1).getPrice()).make();
			}
		};

		this.purchaserLaserButton = new Button() {
			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				LaserTurretTool.giveOneUse(player);
				cache.takeCurrency(turretData.getLevel(1).getPrice(), false);
				animateTitle("&aPurchased a Laser Turret tool");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.ARROW, "&aLaser Turret",
						"Click to purchase this tool", "that will allow you to", "create a laser turret.", "", "Price: " + turretData.getLevel(1).getPrice()).make();
			}
		};
	}

	@Override
	protected String[] getInfo() {
		return new String[]{
				"In this menu you can purchase",
				"a turret tool that will allow",
				"you to create a turret. You may",
				"only create one turret with a tool,",
				"so use it wisely.",
				"",
				"You currently have" + PlayerCache.from(this.getViewer()).getCurrency(false) + " " + Settings.CurrencySection.CURRENCY_NAME
		};
	}
}
