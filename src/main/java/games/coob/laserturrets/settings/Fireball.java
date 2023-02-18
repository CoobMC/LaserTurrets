package games.coob.laserturrets.settings;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Fireball extends TurretSettings {

	private ItemStack toolItem;
	private String base64Texture;

	protected Fireball(final String turretName, @Nullable final TurretType type) {
		super(turretName, type);
	}

	@Override
	protected void onLoad() {
		this.toolItem = this.getItemStack("Tool_Item");
		this.base64Texture = this.getString("Head_Texture");

		super.onLoad();
	}

	@Override
	protected void onSave() {
		super.onSave();

		this.set("Tool_Item", this.toolItem);
		this.set("Head_Texture", this.base64Texture);
	}

	@Override
	public String getHeadTexture() {
		return this.base64Texture;
	}

	@Override
	public void setHeadTexture(final String texture) {
		this.base64Texture = texture;

		this.save();
	}

	@Override
	public ItemStack getToolItem() {
		return this.toolItem;
	}

	@Override
	public void setToolItem(final ItemStack itemStack) {
		this.toolItem = itemStack;

		this.save();
	}
}
