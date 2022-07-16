package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.PlayerBlacklistPrompt;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonConversation;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;

public class TurretSelectionMenu extends Menu {

	@Position(9 + 2)
	private final Button arrowTurretButton;

	@Position(9 + 4)
	private final Button flameTurretButton;

	@Position(9 + 6)
	private final Button laserTurretButton;

	public TurretSelectionMenu(final Player player) {

		this.setTitle("Turret Menu");
		this.setSize(9 * 3);
		this.setSlotNumbersVisible();
		this.setViewer(player);

		this.arrowTurretButton = new ButtonMenu(new TurretTypeMenu(ViewMode.ARROW, player), CompMaterial.ARROW,
				"Arrow Turret Menu",
				"",
				"Open this menu to edit",
				"arrow turret settings.");

		this.flameTurretButton = new ButtonMenu(new TurretTypeMenu(ViewMode.FLAME, player), CompMaterial.LAVA_BUCKET,
				"Flame Turret Menu",
				"",
				"Open this menu to edit",
				"flame turret settings.");

		this.laserTurretButton = new ButtonMenu(new TurretTypeMenu(ViewMode.LASER, player), CompMaterial.BLAZE_ROD,
				"Laser Turret Menu",
				"",
				"Open this menu to edit",
				"laser turret settings.");
	}

	@Override
	protected String[] getInfo() {
		return new String[]{
				"In this menu, you can select",
				"a turret type to view them",
				"and you can also edit each",
				"individual turret."
		};
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		return NO_ITEM;
	}

	@Override
	public Menu newInstance() {
		return new TurretSelectionMenu(this.getViewer());
	}

	private class TurretTypeMenu extends MenuPagged<TurretData> {

		private TurretData turretData;

		private final ViewMode viewMode;

		@Position(2)
		private final Button levelEditButton;

		@Position(4)
		private final Button playerBlacklistButton;

		@Position(8)
		private final Button teleportButton;

		TurretTypeMenu(final ViewMode viewMode, final Player player) {
			super(9 * 4, new ArrayList<>(viewMode.turretTypeList));

			this.viewMode = viewMode;

			this.setTitle(viewMode.typeName + " Turrets");

			this.levelEditButton = new ButtonMenu(new LevelMenu(player, turretData, 1), CompMaterial.EXPERIENCE_BOTTLE,
					"Level Menu",
					"",
					"Open this menu to upgrade",
					"or downgrade the turret.");

			this.playerBlacklistButton = new ButtonConversation(new PlayerBlacklistPrompt(), CompMaterial.KNOWLEDGE_BOOK,
					"Player Blacklist",
					"",
					"Click to add or remove",
					"players from the blacklist",
					"to prevent the turret from",
					"targeting specific players.");

			this.teleportButton = Button.makeSimple(CompMaterial.ENDER_EYE, "Teleport", "Teleport to the turret/nto visit it.", player1 -> {
				player1.teleport(this.turretData.getLocation());

				Messenger.success(player1, "You have successfully teleported to the " + viewMode.typeName.toLowerCase() + " turret with the id of &2" + this.turretData.getId());
			});
		}

		@Override
		protected ItemStack convertToItemStack(final TurretData turretData) {
			return ItemCreator.of(turretData.getMaterial()).name("&0" + this.viewMode.typeName + " Turret &7" + turretData.getId()).make();
		}

		@Override
		protected void onPageClick(final Player player, final TurretData turretData, final ClickType clickType) {
			// cache.setSelectedTurret(turretData);
			this.turretData = turretData;
		}

		@Override
		protected void onMenuClose(final Player player, final Inventory inventory) {
			this.turretData = null; // TODO
			System.out.println("closed menu turret");
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Select an " + this.viewMode.typeName.toLowerCase() + " turret",
					"to edit its settings."
			};
		}

		/*private class TurretLevelMenu extends Menu implements MenuQuantitable {

			@Getter
			@Setter
			private MenuQuantity quantity = MenuQuantity.ONE;

			@Position(start = StartPosition.CENTER)
			private final Button levelButton;

			TurretLevelMenu(final Player player) {
				super(TurretTypeMenu.getMenu(player));

				this.setTitle("Turret Level");
				this.setSize(27);
				this.setViewer(player);

				this.levelButton = Button.makeDummy(ItemCreator.of(CompMaterial.EXPERIENCE_BOTTLE)
						.amount(turretData.getLevel() > 0 ? turretData.getLevel() : 1));
			}

			@Override
			protected void onButtonClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final Button button) {
				if (button.equals(levelButton)) {
					final int nextLevel = (int) MathUtil.range(turretData.getLevel() + this.getNextQuantityPercent(click), 1, 3);

					turretData.setLevel(nextLevel);
				}
			}
		}*/
	}

	@RequiredArgsConstructor
	private enum ViewMode {
		ARROW("Arrow", TurretRegistry.getInstance().getArrowTurrets()),
		FLAME("Flame", TurretRegistry.getInstance().getFlameTurrets()),
		LASER("Laser", TurretRegistry.getInstance().getLaserTurrets());

		private final String typeName;
		private final List<TurretData> turretTypeList;
	}
}