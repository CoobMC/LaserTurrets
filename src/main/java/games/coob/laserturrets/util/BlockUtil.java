package games.coob.laserturrets.util;

import org.bukkit.Material;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockUtil {

	private static final Set<CompMaterial> INTRACTABLE_BLOCKS;

	static {
		INTRACTABLE_BLOCKS = new HashSet<>(Arrays.asList(
				CompMaterial.CHEST,
				CompMaterial.TRAPPED_CHEST,
				CompMaterial.ENDER_CHEST,
				CompMaterial.FURNACE,
				CompMaterial.CRAFTING_TABLE,
				CompMaterial.DISPENSER,
				CompMaterial.DROPPER,
				CompMaterial.HOPPER,
				CompMaterial.ANVIL,
				CompMaterial.ENCHANTING_TABLE,
				CompMaterial.JUKEBOX,
				CompMaterial.NOTE_BLOCK,
				CompMaterial.CAULDRON,
				CompMaterial.BREWING_STAND,
				CompMaterial.MINECART,
				CompMaterial.BEACON,
				CompMaterial.DAYLIGHT_DETECTOR,
				CompMaterial.COMMAND_BLOCK,
				CompMaterial.DROPPER
		));
	}

	public static boolean isInteractable(final Material material) {
		return INTRACTABLE_BLOCKS.contains(CompMaterial.fromMaterial(material));
	}
}
