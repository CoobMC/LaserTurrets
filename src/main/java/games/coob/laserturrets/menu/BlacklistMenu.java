package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonConversation;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlacklistMenu extends Menu {

	private final TurretData turretData;

	@Position(13)
	private final Button mobBlacklistButton;

	@Position(17)
	private final Button playerBlacklistButton;

	public BlacklistMenu(final Menu parent, final TurretData turretData) {
		super(parent);

		this.turretData = turretData;

		this.setSize(3 * 27);
		this.setTitle("Turret Blacklist");

		this.mobBlacklistButton = new ButtonMenu(new BlacklistMenu.MobBlacklistMenu(turretData), CompMaterial.CREEPER_HEAD.toItem());

		this.playerBlacklistButton = new ButtonMenu(new BlacklistMenu.PlayerBlacklistMenu(turretData), CompMaterial.PLAYER_HEAD.toItem());
	}

	private class MobBlacklistMenu extends MenuPagged<EntityType> {

		private final Button addButton;

		private MobBlacklistMenu(final TurretData turretData) {
			super(27, BlacklistMenu.this, turretData.getMobBlackList());

			this.setTitle("Mob Blacklist");

			this.addButton = new ButtonMenu(new BlacklistMenu.MobBlacklistMenu.MobSelectionMenu(), CompMaterial.CREEPER_HEAD,
					"Add Mob",
					"",
					"Open this menu to add ",
					"mobs from the blacklist",
					"to prevent the turret",
					"from targeting them.");
		}

		@Override
		protected ItemStack convertToItemStack(final EntityType entityType) {
			return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType)).make();
		}

		@Override
		protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
			TurretRegistry.getInstance().removeMobFromBlacklist(turretData, entityType);
			this.animateTitle("&cRemoved " + entityType.name() + "from the mob blacklist.");
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == this.getBottomCenterSlot())
				return addButton.getItem();

			return NO_ITEM;
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit your mob blacklist by",
					"clicking the existing eggs",
					"to remove them or clicking",
					"the 'Add Mob' button to add",
					"mobs to your blacklist."
			};
		}

		private class MobSelectionMenu extends MenuPagged<EntityType> {
			private MobSelectionMenu() {
				super(9, BlacklistMenu.MobBlacklistMenu.this, Arrays.stream(EntityType.values())
						.filter(EntityType::isAlive)
						.collect(Collectors.toList()));

				this.setTitle("Select a Mob");
			}

			@Override
			protected ItemStack convertToItemStack(final EntityType entityType) {
				return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType)).make();
			}

			@Override
			protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
				TurretRegistry.getInstance().addMobToBlacklist(turretData, entityType);
				this.animateTitle("&aAdded " + entityType.name() + "to the mob blacklist.");
			}
		}
	}

	private class PlayerBlacklistMenu extends MenuPagged<Player> {

		private final Button addButton;

		private final Button addPromptButton;

		private PlayerBlacklistMenu(final TurretData turretData) {
			super(27, BlacklistMenu.this, compileBlacklistedPlayers(turretData.getPlayerBlacklist()));

			this.setTitle("Player Blacklist");

			this.addButton = new ButtonMenu(new BlacklistMenu.PlayerBlacklistMenu.PlayerSelectionMenu(this.getViewer()), CompMaterial.CREEPER_HEAD,
					"Add Players",
					"",
					"Open this menu to add ",
					"players to the blacklist",
					"to prevent the turret",
					"from targeting them.",
					"These players would be",
					"considered as allies.");

			this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
					ItemCreator.of(CompMaterial.BEACON, "Type a name",
							"",
							"Click this button if you",
							"would like to add a player",
							"to the blacklist by typing ",
							"his name, this means you can",
							"also add offline players."));
		}

		@Override
		protected ItemStack convertToItemStack(final Player player) {
			return ItemCreator.of(
							CompMaterial.PLAYER_HEAD,
							player.getName(),
							"",
							"Click to remove",
							player.getName())
					.skullOwner(player.getName()).make();
		}

		@Override
		protected void onPageClick(final Player player, final Player item, final ClickType click) {
			TurretRegistry.getInstance().removePlayerFromBlacklist(turretData, player.getUniqueId());
			this.animateTitle("&cRemoved " + player.getName() + "from the blacklist.");
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == this.getBottomCenterSlot())
				return addButton.getItem();

			return NO_ITEM;
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit your player blacklist by",
					"clicking the existing heads",
					"to remove them or clicking",
					"the 'Add Mob' button to add",
					"mobs to your blacklist."
			};
		}

		private class PlayerSelectionMenu extends MenuPagged<Player> {
			private PlayerSelectionMenu(final Player player) {
				super(9, BlacklistMenu.PlayerBlacklistMenu.this, player.getWorld().getPlayers());

				this.setTitle("Select a player");
			}

			@Override
			protected ItemStack convertToItemStack(final Player player) {
				return ItemCreator.of(
								CompMaterial.PLAYER_HEAD,
								player.getName(),
								"",
								"Click to add",
								player.getName())
						.skullOwner(player.getName()).make();
			}

			@Override
			protected void onPageClick(final Player player, final Player item, final ClickType click) {
				TurretRegistry.getInstance().addPlayerToBlacklist(turretData, player.getUniqueId());
				this.animateTitle("&aAdded " + player.getName() + "to the blacklist.");
			}
		}
	}

	private final class PlayerBlacklistPrompt extends SimplePrompt {

		@Override
		protected String getPrompt(final ConversationContext context) {
			return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
		}

		@Nullable
		@Override
		protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
			final TurretRegistry registry = TurretRegistry.getInstance();

			registry.addPlayerToBlacklist(turretData, input);
			tellSuccess("You have added " + input + " to the blacklist!");
			return END_OF_CONVERSATION;
		}
	}

	private static List<Player> compileBlacklistedPlayers(final List<UUID> uuidList) {
		final List<Player> blacklistedPlayers = new ArrayList<>();

		for (final UUID uuid : uuidList)
			blacklistedPlayers.add(Remain.getPlayerByUUID(uuid));

		return blacklistedPlayers;
	}
}

