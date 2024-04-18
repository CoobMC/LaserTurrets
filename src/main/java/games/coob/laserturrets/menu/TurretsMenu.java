package games.coob.laserturrets.menu;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.util.Lang;
import games.coob.laserturrets.util.TurretUtil;
import lombok.NonNull;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonConversation;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.List;

public class TurretsMenu extends MenuPagged<TurretData> {

    private String typeName;

    private TurretData turretData;

    private final Player player;

    private final Button changeTypeButton;

    private final Button settingsButton;

    private TurretsMenu(final Player player, final String typeName) {
        super(9 * 4, null, compileTurrets(typeName), true);

        this.typeName = typeName;
        this.player = player;

        this.setTitle(Lang.of("Turrets_Menu.Menu_Title", "{turretType}", ChatUtil.capitalize(TurretUtil.getDisplayName(typeName))));

        this.changeTypeButton = new ButtonConversation(new EditMenuTypePrompt(),
                ItemCreator.of(CompMaterial.BEACON, Lang.of("Turrets_Menu.Change_Turret_Type_Button_Title"),
                        Lang.ofArray("Turrets_Menu.Change_Turret_Type_Button_Lore")));

        this.settingsButton = new ButtonMenu(new SettingsMenu(this, player), CompMaterial.ANVIL, Lang.of("Turrets_Menu.Settings_Button_Title"),
                Lang.ofArray("Turrets_Menu.Settings_Button_Lore"));
    }

    @Override
    protected String[] getInfo() {
        return Lang.ofArray("Turrets_Menu.Info_Button");
    }

    private static List<TurretData> compileTurrets(final String typeName) {
        if (!typeName.equals("all"))
            return TurretData.getTurretsOfType(typeName);

        return (List<TurretData>) TurretData.getTurrets();
    }

    @Override
    protected ItemStack convertToItemStack(final TurretData turretData) {
        final int level = turretData.getCurrentLevel();
        final String id = turretData.getId();
        final String type = TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType()));
        final String[] lore = Lang.ofArray("Turrets_Menu.Turrets_Lore", "{level}", level, "{turretType}", type);

        if (this.typeName.equalsIgnoreCase("all"))
            return ItemCreator.of(turretData.getMaterial()).name(Lang.of("Turrets_Menu.Turrets_Title", "{turretType}", type, "{turretId}", id)).lore(lore).makeMenuTool();
        else if (type.equalsIgnoreCase(this.typeName))
            return ItemCreator.of(turretData.getMaterial()).name(Lang.of("Turrets_Menu.Turrets_Title", "{turretType}", type, "{turretId}", id)).lore(lore).makeMenuTool();

        return NO_ITEM;
    }

    @Override
    protected void onPageClick(final Player player, final TurretData turretData, final ClickType clickType) {
        this.newInstance().displayTo(player);
        this.turretData = turretData;
        Common.runLater(() -> new TurretEditMenu(this).displayTo(player));
    }

    @Override
    public ItemStack getItemAt(final int slot) {
        if (slot == getSize() - 1)
            return changeTypeButton.getItem();
        if (slot == getBottomCenterSlot())
            return settingsButton.getItem();

        return super.getItemAt(slot);
    }

    @Override
    public Menu newInstance() {
        return new TurretsMenu(this.player, this.typeName);
    }

    private final class EditMenuTypePrompt extends SimplePrompt {

        private EditMenuTypePrompt() {
            super(true);
        }

        @Override
        protected String getPrompt(final ConversationContext ctx) {
            return Lang.of("Turrets_Menu.Edit_Turret_View_Type_Prompt_Message");
        }

        @Override
        protected boolean isInputValid(final ConversationContext context, final String input) {
            return input.equals("all") || input.equals("arrow") || input.equals("fireball") || input.equals("beam");
        }

        @Override
        protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
            return Lang.of("Turrets_Menu.Edit_Turret_View_Type_Prompt_Invalid_Text", "{invalidType}", invalidInput);
        }

        @Override
        protected Prompt acceptValidatedInput(@NonNull final ConversationContext context, @NonNull final String input) {
            typeName = input.toLowerCase();

            return Prompt.END_OF_CONVERSATION;
        }
    }

    private final class TurretEditMenu extends Menu {

        @Position(12)
        private final Button alliesButton;

        @Position(14)
        private final Button teleportButton;

        @Position(31)
        private final Button removeTurret;

        TurretEditMenu(final Menu parent) {
            super(parent, true);

            this.setSize(9 * 4);
            this.setTitle(Lang.of("Turrets_Menu.Turret_Edit_Menu_Title", "{turretType}", TurretUtil.capitalizeWord(TurretUtil.getDisplayName(turretData.getType())), "{turretId}", turretData.getId()));

            this.alliesButton = new ButtonMenu(new TurretAlliesMenu(TurretEditMenu.this, turretData, player), CompMaterial.KNOWLEDGE_BOOK,
                    Lang.of("Turrets_Menu.Allies_Button_Title"),
                    Lang.ofArray("Turrets_Menu.Allies_Button_Lore"));

            this.teleportButton = Button.makeSimple(CompMaterial.ENDER_EYE, Lang.of("Turrets_Menu.Teleport_Button_Title"),
                    Lang.of("Turrets_Menu.Teleport_Button_Lore"), player1 -> {
                        player1.teleport(turretData.getLocation());

                        Messenger.success(player1, Lang.of("Turrets_Menu.Teleport_Success_Message", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId()));
                    });

            this.removeTurret = Button.makeSimple(CompMaterial.BARRIER, Lang.of("Turrets_Menu.Remove_Turret_Button_Title"), Lang.of("Turrets_Menu.Remove_Turret_Button_Lore"), player1 -> {
                turretData.unregister();

                final Menu previousMenu = new TurretsMenu(player1, typeName);

                previousMenu.displayTo(player1);
                Common.runLater(() -> previousMenu.restartMenu(Lang.of("Turrets_Menu.Remove_Turret_Animated_Message", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId())));
            });
        }

        @Override
        protected String[] getInfo() {
            return Lang.ofArray("Turrets_Menu.Turret_Edit_Menu_Info_Button", "{turretType}", TurretUtil.getDisplayName(turretData.getType()), "{turretId}", turretData.getId());
        }

        @Override
        public Menu newInstance() {
            return new TurretEditMenu(getParent());
        }
    }

    public static void openAllTurretsSelectionMenu(final Player player) {
        new TurretsMenu(player, "all").displayTo(player);
    }

    public static void openArrowTurretsSelectionMenu(final Player player) {
        new TurretsMenu(player, "arrow").displayTo(player);
    }

    public static void openFireballTurretsSelectionMenu(final Player player) {
        new TurretsMenu(player, "fireball").displayTo(player);
    }

    public static void openBeamTurretsSelectionMenu(final Player player) {
        new TurretsMenu(player, "beam").displayTo(player);
    }
}