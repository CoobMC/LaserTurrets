package games.coob.laserturrets.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TurretKillEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final LivingEntity livingEntity;

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public TurretKillEvent(final LivingEntity entity) {
		this.livingEntity = entity;
	}


	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public LivingEntity getLivingEntity() {
		return this.livingEntity;
	}

}