package games.coob.laserturrets.menu;

import games.coob.laserturrets.tools.ArrowTurretTool;
import games.coob.laserturrets.tools.BeamTurretTool;
import games.coob.laserturrets.tools.FireballTurretTool;
import games.coob.laserturrets.util.Lang;
import org.mineacademy.fo.menu.MenuTools;

public class ToolsMenu extends MenuTools {
    public ToolsMenu() {
        this.setTitle(Lang.of("Tools_Menu.Menu_Title"));
    }

    @Override
    protected Object[] compileTools() {
        return new Object[]{
                ArrowTurretTool.class, FireballTurretTool.class, BeamTurretTool.class
        };
    }

    @Override
    protected String[] getInfo() {
        return Lang.ofArray("Tools_Menu.Info_Button");
    }
}
