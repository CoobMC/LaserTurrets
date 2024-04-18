package games.coob.laserturrets.sequence;

import games.coob.laserturrets.model.TurretData;
import games.coob.laserturrets.settings.Settings;
import games.coob.laserturrets.settings.TurretSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.SkullCreator;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.util.UUID;

public final class TurretCreationSequence extends Sequence {

    private final Player player;

    private final Block block;

    private final String type;

    TurretCreationSequence(final Player player, final Block block, final String type) {
        super("turret-creation");

        this.player = player;
        this.block = block;
        this.type = type;
    }

    /*
     * Start this sequence
     */
    @Override
    protected void onStart() {
        this.getLastLocation().add(0.5, 1.2, 0.5);

        final TurretSettings turretSettings = TurretSettings.findByName(this.type);
        final ItemStack item = ItemCreator.of(SkullCreator.itemFromBase64(turretSettings.getHeadTexture())).make();

        this.lightning();
        this.glowingStand(item);
        this.getLastStand().setAnimated(true);

        this.nextSequence(this::onFinish);
    }

    @Override
    public void disable() {
        this.removeLast();
    }

    private void onFinish() {
        final Location location = this.block.getLocation().add(0.5, 1, 0.5);

        Settings.TurretSection.CREATION_PARTICLE.spawn(location, 0.5, 0.5, 0.5, 0.1, Settings.TurretSection.CREATION_PARTICLE_COUNT, 0.1, null);
        Settings.TurretSection.CREATION_SOUND.play(location);

        final TurretSettings turretSettings = TurretSettings.findByName(this.type);
        final Block skullBlock = this.block.getRelative(BlockFace.UP);
        final String uniqueID = UUID.randomUUID().toString().substring(0, 6);

        SkullCreator.blockWithBase64(skullBlock, turretSettings.getHeadTexture());
        SkullCreator.rotateSkull((Skull) skullBlock.getState(), PlayerUtil.getFacing(this.player));

        final TurretData turretData = TurretData.createTurret(uniqueID, this.block);
        Common.runLater(() -> turretData.register(this.player, this.block, this.type, uniqueID));

        this.removeLast();
        this.block.removeMetadata("IsCreating", SimplePlugin.getInstance());
        this.player.removeMetadata("CreatingTurret", SimplePlugin.getInstance());
    }
}
