package games.coob.laserturrets.menu;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import games.coob.laserturrets.PlayerCache;
import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.model.UnplacedData;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuContainer;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgradeMenu extends Menu {

    private final TurretData turretData;

    private final int level;

    private final Button upgradeButton;

    private final Button blacklistButton;

    private final Button takeButton;

    private final Button ammoButton;

    private final TurretSettings settings;

    public UpgradeMenu(final TurretData turretData, final int turretLevel, final Player player) {
        this.setSize(27);
        this.setTitle(Lang.of("Upgrade_Menu.Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType())), "{level}", turretLevel));
        this.setViewer(player);

        this.turretData = turretData;
        this.level = turretLevel;
        this.settings = TurretSettings.findByName(turretData.getType());

        final int currentLevel = turretData.getCurrentLevel();
        final int nextLevel = currentLevel + 1;
        final boolean hasMaxTier = currentLevel == this.settings.getLevelsSize();

        this.upgradeButton = new Button() {

            @Override
            public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
                if (!hasMaxTier) {
                    final PlayerCache cache = PlayerCache.from(player);

                    final double funds = cache.getCurrency(false);
                    final double price = settings.getLevel(nextLevel).getPrice();

                    if (funds < price)
                        animateTitle(Lang.of("Menu.Not_Enough_Money_Animated_Message", "{moneyNeeded}", MathUtil.formatTwoDigits(price - funds), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME));
                    else {
                        cache.takeCurrency(price, false);
                        turretData.setCurrentTurretLevel(nextLevel);
                        turretData.setCurrentHealth(settings.getLevel(nextLevel).getHealth());

                        CompSound.LEVEL_UP.play(player);

                        Common.runLater(() -> {
                            final Menu newMenu = new UpgradeMenu(turretData, nextLevel, player);

                            newMenu.displayTo(player);
                            Common.runLater(() -> newMenu.animateTitle(Lang.of("Upgrade_Menu.Upgrade_Animated_Message", "{nextLevel}", nextLevel)));
                        });

                        if (Settings.TurretSection.DISPLAY_HOLOGRAM) {
                            final String[] lore = Lang.ofArray("Turret_Display.Hologram", "{turretType}", TurretUtil.capitalizeWord(turretData.getType()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{health}", turretData.getCurrentHealth());
                            final List<String> list = new ArrayList<>(Arrays.asList(lore));

                            if (TurretSettings.findByName(turretData.getType()).isInvincible()) {
                                list.removeIf(line -> line.contains(String.valueOf(turretData.getCurrentHealth())));
                            }

                            final Object[] objects = list.toArray();
                            final String[] lines = Arrays.copyOf(objects, objects.length, String[].class);
                            final List<String> newList = new ArrayList<>(Arrays.asList(lines));

                            // turretData.getHologram().updateLore(lines);
                            final Hologram hologram = DHAPI.getHologram(turretData.getId());

                            if (hologram != null) {
                                DHAPI.setHologramLines(hologram, newList);
                            }
                        }
                    }
                }
            }

            @Override
            public ItemStack getItem() {
                String[] lore = new String[0];

                if (!hasMaxTier) {
                    final double price = settings.getLevel(nextLevel).getPrice();
                    final TurretSettings.LevelData currentLevel = settings.getLevel(turretData.getCurrentLevel());
                    final TurretSettings.LevelData upgradedLevel = settings.getLevel(nextLevel);

                    lore = Lang.ofArray("Upgrade_Menu.Upgrade_Button_Lore", "{price}", price, "{level}", turretData.getCurrentLevel(), "{currencyName}", Settings.CurrencySection.CURRENCY_NAME, "{currentRange}", currentLevel.getRange(), "{upgradedRange}", upgradedLevel.getRange(), "{currentDamage}", currentLevel.getLaserDamage(), "{upgradedDamage}", upgradedLevel.getLaserDamage(), "{currentHealth}", currentLevel.getHealth(), "{upgradedHealth}", upgradedLevel.getHealth());
                }

                return ItemCreator
                        .of(!hasMaxTier ? CompMaterial.ENDER_EYE : CompMaterial.BARRIER)
                        .name(!hasMaxTier ? Lang.of("Upgrade_Menu.Upgrade_Button_Title") : Lang.of("Upgrade_Menu.Upgrade_Button_Title_Max_Level"))
                        .lore(lore)
                        .make();
            }
        };

        this.takeButton = new Button() {
            final TurretSettings settings = TurretSettings.findByName(turretData.getType());

            @Override
            public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
                final ItemStack skull = SkullCreator.itemFromBase64(settings.getHeadTexture());
                final ItemStack turret = ItemCreator.of(skull).name(Lang.of("Tool.Unplaced_Turret_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
                        .lore(Lang.ofArray("Tool.Unplaced_Turret_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel()), "{owner}", Remain.getOfflinePlayerByUUID(turretData.getOwner()).getName()))
                        .tag("id", turretData.getId()).make();

                player.getInventory().addItem(turret);

                final UnplacedData unplacedData = UnplacedData.createTurret(turretData.getId(), turretData.getType());
                unplacedData.registerFromTurret(turretData, turret);
                turretData.unregister();

                player.closeInventory();
                CompSound.BLOCK_ANVIL_DESTROY.play(player);
                Messenger.success(player, Lang.of("Turret_Commands.Take_Turret_Message", "{turretType}", turretData.getType(), "{turretId}", turretData.getId()));
            }

            @Override
            public ItemStack getItem() {
                final ItemStack skull = SkullCreator.itemFromBase64(settings.getHeadTexture());

                return ItemCreator.of(skull).name(Lang.of("Upgrade_Menu.Take_Turret_Button_Title", "{turretType}", ChatUtil.capitalize(turretData.getType()), "{turretId}", turretData.getId()))
                        .lore(Lang.ofArray("Upgrade_Menu.Take_Turret_Button_Lore", "{turretType}", turretData.getType(), "{turretId}", turretData.getId(), "{level}", MathUtil.toRoman(turretData.getCurrentLevel())))
                        .tag("id", turretData.getId()).make();
            }
        };

        this.blacklistButton = new ButtonMenu(new TurretAlliesMenu(UpgradeMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
                Lang.of("Upgrade_Menu.Allies_Button_Title"),
                Lang.ofArray("Upgrade_Menu.Allies_Button_Lore"));

        this.ammoButton = new ButtonMenu(new AmmoMenu(), CompMaterial.fromMaterial(settings.getAmmo().getSecondValue().getType()),
                Lang.of("Upgrade_Menu.Ammo_Button_Title"),
                Lang.ofArray("Upgrade_Menu.Ammo_Button_Lore"));
    }

    @Override
    public ItemStack getItemAt(final int slot) {
        if (slot == this.getCenterSlot() + 1)
            return this.upgradeButton.getItem();
        if (settings.getAmmo().getFirstValue() && slot == this.getCenterSlot() + 9)
            return this.ammoButton.getItem();
        if (slot == this.getCenterSlot() - 1)
            return this.blacklistButton.getItem();
        if (slot == this.getBottomCenterSlot() + 4)
            return this.takeButton.getItem();

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

    private class AmmoMenu extends MenuContainer {

        AmmoMenu() {
            super(UpgradeMenu.this);

            this.setSize(54);
            this.setTitle(Lang.of("Upgrade_Menu.Ammo_Button_Title"));
        }

        @Override
        protected boolean canEditItem(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor, final InventoryAction action) {
            return cursor.isSimilar(settings.getAmmo().getSecondValue());
        }

        @Override
        protected void onMenuClose(final StrictMap<Integer, ItemStack> items) {
            final List<ItemStack> ammo = new ArrayList<>(items.values());
            turretData.setAmmo(ammo);
        }

        @Override
        protected ItemStack getDropAt(final int slot) {
            final List<ItemStack> items = turretData.getAmmo();

            if (turretData.getAmmo() == null)
                return NO_ITEM;

            return slot < items.size() ? items.get(slot) : NO_ITEM;
        }

        @Override
        protected String[] getInfo() {
            return Lang.ofArray("Upgrade_Menu.Ammo_Menu_Info_Button");
        }
    }
}
