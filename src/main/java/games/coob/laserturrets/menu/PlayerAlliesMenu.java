package games.coob.laserturrets.menu;

import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerAlliesMenu extends Menu {

    private final PlayerCache cache;

    @Position(14)
    private final Button mobBlacklistButton;

    @Position(12)
    private final Button playerBlacklistButton;

    @Position(26)
    private final Button syncAlliesButton;

    public PlayerAlliesMenu(final Player player) {
        this.cache = PlayerCache.from(player);

        this.setViewer(player);
        this.setSize(27);
        this.setTitle(Lang.of("Manage_Player_Allies_Menu.Main_Title"));

        this.mobBlacklistButton = new ButtonMenu(new MobBlacklistMenu(), CompMaterial.CREEPER_HEAD,
                Lang.of("Manage_Player_Allies_Menu.Mob_Allies_Title", "{listType}", cache.isMobWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))), Lang.ofArray("Manage_Player_Allies_Menu.Mob_Allies_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

        this.playerBlacklistButton = new ButtonMenu(new PlayerBlacklistMenu(), CompMaterial.PLAYER_HEAD,
                Lang.of("Manage_Player_Allies_Menu.Player_Allies_Title", "{listType}", cache.isPlayerWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))), Lang.ofArray("Manage_Player_Allies_Menu.Player_Allies_Lore", "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

        this.syncAlliesButton = new Button() {
            @Override
            public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
                for (final TurretData turretData : TurretData.getOwningTurrets(player.getUniqueId())) {
                    turretData.addPlayersToAllies(cache.getPlayerAllies());
                    turretData.addMobsToAllies(cache.getMobAllies());
                }

                animateTitle(Lang.of("Manage_Player_Allies_Menu.Sync_Allies_Animated_Message"));
            }

            @Override
            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.COMPARATOR, Lang.of("Manage_Player_Allies_Menu.Sync_Allies_Button_Title"),
                        Lang.ofArray("Manage_Player_Allies_Menu.Sync_Allies_Button_Lore")).make();
            }
        };
    }

    @Override
    protected String[] getInfo() {
        return Lang.ofArray("Manage_Player_Allies_Menu.Main_Info");
    }

    @Override
    public Menu newInstance() {
        return new PlayerAlliesMenu(this.getViewer());
    }

    private class MobBlacklistMenu extends MenuPagged<EntityType> {

        private final Button addButton;

        private final Button mobListTypeButton;

        private MobBlacklistMenu() {
            super(27, PlayerAlliesMenu.this, cache.getMobAllies(), true);

            this.setTitle(Replacer.replaceArray(Lang.of("Manage_Player_Allies_Menu.Mob_Allies_Title"), "{listType}", cache.isMobWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));

            this.addButton = new ButtonMenu(new MobSelectionMenu(), CompMaterial.ENDER_CHEST,
                    Lang.of("Manage_Player_Allies_Menu.Mob_Add_Button_Title"),
                    Lang.ofArray("Manage_Player_Allies_Menu.Mob_Add_Button_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

            this.mobListTypeButton = new Button() {
                @Override
                public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
                    final boolean isWhitelist = cache.isMobWhitelistEnabled();

                    cache.enableMobWhitelist(!isWhitelist);
                    setTitle(Lang.of("Manage_Player_Allies_Menu.Mob_List_Type_Menu_Title", "{listType}", cache.isMobWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));
                    restartMenu(Lang.of("Manage_Player_Allies_Menu.Mob_List_Type_Menu_Animated_Message", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured")));
                }

                @Override
                public ItemStack getItem() {
                    final boolean isWhitelist = cache.isMobWhitelistEnabled();

                    return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, Lang.of("Manage_Player_Allies_Menu.Mob_List_Type_Button_Title", "{listType}", isWhitelist ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist_Coloured")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist_Coloured"))),
                            Lang.ofArray("Manage_Player_Allies_Menu.Mob_List_Type_Button_Lore", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured"))).make();
                }
            };
        }

        @Override
        protected ItemStack convertToItemStack(final EntityType entityType) {
            return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
                    .lore(Lang.ofArray("Manage_Player_Allies_Menu.Mob_Egg_Lore", "{entityName}", entityType.name())).make();
        }

        @Override
        protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
            cache.removeMobFromAllies(entityType);
            this.restartMenu(Lang.of("Manage_Player_Allies_Menu.Mob_Egg_Animated_Message", "{entityName}", entityType.name()));
        }

        @Override
        public ItemStack getItemAt(final int slot) {
            if (slot == this.getBottomCenterSlot())
                return addButton.getItem();
            if (slot == this.getBottomCenterSlot() + 3)
                return mobListTypeButton.getItem();

            return super.getItemAt(slot);
        }

        @Override
        protected String[] getInfo() {
            return Lang.ofArray("Manage_Player_Allies_Menu.Mob_Menu_Info_Button", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
        }

        @Override
        public Menu newInstance() {
            return new MobBlacklistMenu();
        }

        private class MobSelectionMenu extends MenuPagged<EntityType> {
            private MobSelectionMenu() {
                super(27, PlayerAlliesMenu.MobBlacklistMenu.this, Arrays.stream(EntityType.values())
                        .filter(EntityType::isAlive)
                        .filter(EntityType::isSpawnable)
                        .collect(Collectors.toList()), true);

                this.setTitle(Lang.of("Manage_Player_Allies_Menu.Mob_Selection_Menu_Title"));
            }

            @Override
            protected ItemStack convertToItemStack(final EntityType entityType) {
                return ItemCreator.ofEgg(entityType, ItemUtil.bountifyCapitalized(entityType))
                        .glow(cache.getMobAllies().contains(entityType))
                        .lore(cache.getMobAllies().contains(entityType) ? Lang.ofArray("Manage_Player_Allies_Menu.Mob_Already_Selected_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{entityName}", entityType.name()) : Lang.ofArray("Manage_Player_Allies_Menu.Mob_Available_For_Selection_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{entityName}", entityType.name()))
                        .make();
            }

            @Override
            protected void onPageClick(final Player player, final EntityType entityType, final ClickType clickType) {
                cache.addMobToAllies(entityType);
                this.animateTitle(Lang.of("Manage_Player_Allies_Menu.Mob_Selection_Animated_Message", "{entityName}", entityType.name()));
            }

            @Override
            public Menu newInstance() {
                return new MobSelectionMenu();
            }
        }
    }

    private class PlayerBlacklistMenu extends MenuPagged<UUID> {

        private final Button addButton;

        private final Button addPromptButton;

        private final Button playerListTypeButton;

        private PlayerBlacklistMenu() {
            super(27, PlayerAlliesMenu.this, cache.getPlayerAllies(), true);

            this.setTitle(Lang.of("Manage_Player_Allies_Menu.Player_Allies_Title", "{listType}", (cache.isPlayerWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist")))));

            this.addButton = new ButtonMenu(new PlayerSelectionMenu(), CompMaterial.ENDER_CHEST,
                    Lang.of("Manage_Player_Allies_Menu.Player_Add_Button_Title"),
                    Lang.ofArray("Manage_Player_Allies_Menu.Player_Add_Button_Lore", "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

            this.addPromptButton = new ButtonConversation(new PlayerBlacklistPrompt(),
                    ItemCreator.of(CompMaterial.WRITABLE_BOOK, Lang.of("Manage_Player_Allies_Menu.Player_Add_Prompt_Button_Title"),
                            Lang.ofArray("Manage_Player_Allies_Menu.Player_Add_Prompt_Button_Lore", "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"))));

            this.playerListTypeButton = new Button() {
                @Override
                public void onClickedInMenu(final Player player, final Menu menu, final ClickType clickType) {
                    final boolean isWhitelist = cache.isPlayerWhitelistEnabled();

                    cache.enablePlayerWhitelist(!isWhitelist);
                    setTitle(Lang.of("Manage_Player_Allies_Menu.Player_List_Type_Menu_Title", "{listType}", cache.isPlayerWhitelistEnabled() ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist"))));
                    restartMenu(Lang.of("Manage_Player_Allies_Menu.Player_List_Type_Menu_Animated_Message", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured")));
                }

                @Override
                public ItemStack getItem() {
                    final boolean isWhitelist = cache.isPlayerWhitelistEnabled();

                    return ItemCreator.of(isWhitelist ? CompMaterial.WHITE_WOOL : CompMaterial.BLACK_WOOL, Lang.of("Manage_Player_Allies_Menu.Player_List_Type_Button_Title", "{listType}", isWhitelist ? TurretUtil.capitalizeWord(Lang.of("Placeholders.Whitelist_Coloured")) : TurretUtil.capitalizeWord(Lang.of("Placeholders.Blacklist_Coloured"))),
                            Lang.ofArray("Manage_Player_Allies_Menu.Player_List_Type_Button_Lore", "{listType}", !isWhitelist ? Lang.of("Placeholders.Whitelist_Coloured") : Lang.of("Placeholders.Blacklist_Coloured"))).make();
                }
            };
        }

        @Override
        protected ItemStack convertToItemStack(final UUID uuid) {
            final OfflinePlayer player = Remain.getOfflinePlayerByUUID(uuid);

            return ItemCreator.of(
                            CompMaterial.PLAYER_HEAD,
                            player.getName(),
                            Lang.ofArray("Manage_Player_Allies_Menu.Player_Head_Lore", "{playerName}", player.getName()))
                    .skullOwner(player.getName()).make();
        }

        @Override
        protected void onPageClick(final Player player, final UUID item, final ClickType click) {
            final OfflinePlayer target = Remain.getOfflinePlayerByUUID(item);

            cache.removePlayerFromAllies(target.getUniqueId());
            this.restartMenu(Lang.of("Manage_Player_Allies_Menu.Player_Head_Animated_Message", "{playerName}", target.getName()));
        }

        @Override
        public ItemStack getItemAt(final int slot) {
            if (slot == this.getBottomCenterSlot() - 1)
                return this.addButton.getItem();
            if (slot == this.getBottomCenterSlot() + 1)
                return this.addPromptButton.getItem();
            if (slot == this.getBottomCenterSlot() + 3)
                return playerListTypeButton.getItem();

            return super.getItemAt(slot);
        }

        @Override
        protected String[] getInfo() {
            return Lang.ofArray("Manage_Player_Allies_Menu.Player_Menu_Info_Button", "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
        }

        @Override
        public Menu newInstance() {
            return new PlayerBlacklistMenu();
        }

        private class PlayerSelectionMenu extends MenuPagged<Player> {
            private PlayerSelectionMenu() {
                super(18, PlayerAlliesMenu.PlayerBlacklistMenu.this, compileOnlinePlayers(), true);

                this.setTitle(Lang.of("Manage_Player_Allies_Menu.Player_Selection_Menu_Title"));
            }

            @Override
            protected ItemStack convertToItemStack(final Player player) {
                return ItemCreator.of(
                                CompMaterial.PLAYER_HEAD,
                                player.getName(),
                                cache.getPlayerAllies().contains(player.getUniqueId()) ? Lang.ofArray("Manage_Player_Allies_Menu.Player_Already_Selected_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{playerName}", player.getName()) : Lang.ofArray("Manage_Player_Allies_Menu.Player_Available_For_Selection_Lore", "{listType}", cache.isMobWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"), "{playerName}", player.getName()))
                        .skullOwner(player.getName()).make();
            }

            @Override
            protected void onPageClick(final Player player, final Player item, final ClickType click) {
                cache.addPlayerToAllies(item.getUniqueId());
                this.animateTitle(Lang.of("Manage_Player_Allies_Menu.Player_Selection_Animated_Message", "{playerName}", player.getName()));
            }

            @Override
            public Menu newInstance() {
                return new PlayerSelectionMenu();
            }
        }
    }

    private final class PlayerBlacklistPrompt extends SimplePrompt {

        @Override
        protected String getPrompt(final ConversationContext context) {
            return Lang.of("Manage_Player_Allies_Menu.Player_Prompt_Message", "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist"));
        }

        @Override
        protected boolean isInputValid(final ConversationContext context, final String input) {
            for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
                return player.getName() != null && player.getName().equals(input);
            return false;
        }

        @Override
        protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
            return Lang.of("Manage_Player_Allies_Menu.Player_Prompt_Invalid_Text", "{invalidPlayer}", invalidInput);
        }

        @Nullable
        @Override
        protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
            cache.addPlayerToAllies(Bukkit.getOfflinePlayer(input).getUniqueId());
            tellSuccess(Lang.of("Manage_Player_Allies_Menu.Player_Prompt_Success", "{playerName}", input, "{listType}", cache.isPlayerWhitelistEnabled() ? Lang.of("Placeholders.Whitelist") : Lang.of("Placeholders.Blacklist")));

            return END_OF_CONVERSATION;
        }
    }

    private static List<Player> compileOnlinePlayers() {
        return new ArrayList<>(Remain.getOnlinePlayers());
    }
}

