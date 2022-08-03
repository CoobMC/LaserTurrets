package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.TurretRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
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

import java.util.*;
import java.util.stream.Collectors;

public class BlacklistMenu extends Menu {

	private final TurretData turretData;

	@Position(14)
	private final Button mobBlacklistButton;

	@Position(12)
	private final Button playerBlacklistButton;

	public BlacklistMenu(final Menu parent, final TurretData turretData, final Player player) {
		super(parent);

		this.turretData = turretData;

		this.setViewer(player);
		this.setSize(27);
		this.setTitle("Turret Blacklist");

		this.mobBlacklistButton = new ButtonMenu(new MobBlacklistMenu(), CompMaterial.CREEPER_HEAD,
				"Mob Blacklist", "", "Edit your mob blacklist");

		this.playerBlacklistButton = new ButtonMenu(new PlayerBlacklistMenu(), CompMaterial.PLAYER_HEAD,
				"Player Blacklist", "", "Edit your player blacklist");
	}

	private class MobBlacklistMenu extends MenuPagged<EntityType> {

		private final Button addButton;

		private MobBlacklistMenu() {
			super(27, BlacklistMenu.this, turretData.getMobBlackList());

			this.setTitle("Mob Blacklist");

			this.addButton = new ButtonMenu(new MobBlacklistMenu.MobSelectionMenu(), CompMaterial.ENDER_CHEST,
					"Add Mob",
					"",
					"Open this menu to add ",
					"mobs from the blacklist",
					"to prevent the turret",
					"from targeting them.");
		}

		@Override
		protected ItemStack convertToItemStack(final EntityType entityType) {
			return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
					.lore("Click to remove").make();
		}

		@Override
		protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
			TurretRegistry.getInstance().removeMobFromBlacklist(turretData, entityType);
			this.restartMenu("&cRemoved " + entityType.name());
			newInstance().displayTo(player);
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == this.getBottomCenterSlot())
				return addButton.getItem();

			return super.getItemAt(slot);
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

		@Override
		public Menu newInstance() {
			return new MobBlacklistMenu();
		}

		private class MobSelectionMenu extends MenuPagged<EntityType> {
			private MobSelectionMenu() {
				super(27, BlacklistMenu.MobBlacklistMenu.this, Arrays.stream(EntityType.values())
						.filter(EntityType::isAlive)
						.collect(Collectors.toList()));

				this.setTitle("Select a Mob");
			}

			@Override
			protected ItemStack convertToItemStack(final EntityType entityType) {
				return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
						.glow(turretData.getMobBlackList().contains(entityType))
						.lore(turretData.getMobBlackList().contains(entityType) ? "&aAlready blacklisted" : "Click to add")
						.make();
			}

			@Override
			protected void onPageClick(final org.bukkit.entity.Player player, final EntityType entityType, final ClickType clickType) {
				TurretRegistry.getInstance().addMobToBlacklist(turretData, entityType);
				this.restartMenu("&aAdded " + entityType.name());
			}

			@Override
			protected void onMenuClose(final Player player, final Inventory inventory) {
				MobBlacklistMenu.this.newInstance().displayTo(player);
			}
		}
	}

	private class PlayerBlacklistMenu extends MenuPagged<UUID> {

		private final Button addButton;

		private final Button addPromptButton;

		private PlayerBlacklistMenu() {
			super(27, BlacklistMenu.this, turretData.getPlayerBlacklist());

			this.setTitle("Player Blacklist");

			this.addButton = new ButtonMenu(new PlayerSelectionMenu(), CompMaterial.ENDER_CHEST,
					"Add Players",
					"",
					"Open this menu to add ",
					"players to the blacklist",
					"to prevent the turret",
					"from targeting them.");

			this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
					ItemCreator.of(CompMaterial.WRITABLE_BOOK, "Type a name",
							"",
							"Click this button if you",
							"would like to add a player",
							"to the blacklist by typing ",
							"his name, this means you can",
							"also add offline players."));
		}

		@Override
		protected ItemStack convertToItemStack(final UUID uuid) {
			final Player player = Remain.getPlayerByUUID(uuid);

			return ItemCreator.of(
							CompMaterial.PLAYER_HEAD,
							player.getName(),
							"Click to remove")
					.skullOwner(player.getName()).make();
		}

		@Override
		protected void onPageClick(final Player player, final UUID item, final ClickType click) {
			final Player target = Remain.getPlayerByUUID(item);

			TurretRegistry.getInstance().removePlayerFromBlacklist(turretData, target.getUniqueId());
			this.restartMenu("&cRemoved " + target.getName());
		}

		@Override
		public ItemStack getItemAt(final int slot) {
			if (slot == this.getBottomCenterSlot() - 1)
				return addButton.getItem();
			if (slot == this.getBottomCenterSlot() + 1)
				return addPromptButton.getItem();

			return super.getItemAt(slot);
		}

		@Override
		protected String[] getInfo() {
			return new String[]{
					"Edit your player blacklist by",
					"clicking the existing heads",
					"to remove them or clicking",
					"the 'Add Mob' button to add",
					"players to your blacklist."
			};
		}

		@Override
		public Menu newInstance() {
			return new PlayerBlacklistMenu();
		}

		private class PlayerSelectionMenu extends MenuPagged<Player> {
			private PlayerSelectionMenu() {
				super(18, BlacklistMenu.PlayerBlacklistMenu.this, compileWorldPlayers(turretData));

				this.setTitle("Select a player");
			}

			@Override
			protected ItemStack convertToItemStack(final Player player) {
				return ItemCreator.of(
								CompMaterial.PLAYER_HEAD,
								player.getName(),
								(turretData.getPlayerBlacklist().contains(player.getUniqueId()) ? "&aAlready blacklisted" : "Click to add"))
						.skullOwner(player.getName()).make();
			}

			@Override
			protected void onPageClick(final Player player, final Player item, final ClickType click) {
				TurretRegistry.getInstance().addPlayerToBlacklist(turretData, item.getUniqueId());
				this.restartMenu("&aAdded " + player.getName());
			}

			@Override
			protected void onMenuClose(final Player player, final Inventory inventory) {
				PlayerBlacklistMenu.this.newInstance().displayTo(player);
			}
		}
	}

	private final class PlayerBlacklistPrompt extends SimplePrompt {

		@Override
		protected String getPrompt(final ConversationContext context) {
			return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
		}

		@Override
		protected boolean isInputValid(final ConversationContext context, final String input) {
			for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
				return player.getName() != null && player.getName().equals(input);
			return false;
		}

		@Override
		protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
			return "Player '" + invalidInput + "' doesn't exist.";
		}

		@Nullable
		@Override
		protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
			final TurretRegistry registry = TurretRegistry.getInstance();

			registry.addPlayerToBlacklist(turretData, Bukkit.getOfflinePlayer(input).getUniqueId());
			tellSuccess("You have added " + input + " to the blacklist!");

			return END_OF_CONVERSATION;
		}
	}

	public static List<Player> compileBlacklistedPlayers(final Set<UUID> uuidList) {
		final List<Player> blacklistedPlayers = new ArrayList<>();

		for (final UUID uuid : uuidList)
			blacklistedPlayers.add(Remain.getPlayerByUUID(uuid));

		return blacklistedPlayers;
	}

	private static List<Player> compileWorldPlayers(final TurretData turretData) {
		final World world = turretData.getLocation().getWorld();

		if (world != null) {
			return world.getPlayers();
		}

		return null;
	}
}

