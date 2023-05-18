package games.coob.laserturrets.listener;

import games.coob.laserturrets.model.TurretData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TurretKillEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final LivingEntity livingEntity;

	private final String turretId;

	private final TurretData turretData;

	private boolean isCancelled;

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public TurretKillEvent(final LivingEntity entity, final String turretId) {
		this.livingEntity = entity;
		this.turretId = turretId;
		this.turretData = TurretData.findById(turretId);
		this.isCancelled = false;
	}


	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public LivingEntity getLivingEntity() {
		return this.livingEntity;
	}

	public TurretData getTurretData() {
		return this.turretData;
	}

	public String getTurretId() {
		return turretId;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(final boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
}