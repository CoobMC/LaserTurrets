package games.coob.laserturrets.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HologramUtil {

	private final Location location;
	private final List<String> lines;
	private final List<ArmorStand> entities;
	private boolean spawned;

	public HologramUtil(final Location location, final String... lines) {
		this.lines = Arrays.asList(lines);
		this.entities = new LinkedList<>();
		this.spawned = false;
		this.location = location;
	}

	public void spawn() {
		final Chunk cnk = this.location.getChunk();
		if (!cnk.isLoaded()) {
			cnk.load();
		}
		for (int i = 0; i < this.lines.size(); ++i) {
			final Location spawn = this.location.clone().subtract(0.0, i * 0.25, 0.0);
			final ArmorStand entity = (ArmorStand) this.location.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
			entity.setGravity(false);
			entity.setBasePlate(false);
			entity.setVisible(false);
			entity.setCustomNameVisible(true);
			entity.setCustomName((String) this.lines.get(i));
			this.entities.add(entity);
		}
		this.spawned = true;
	}

	public void update() {
		this.remove();
		this.spawn();
	}

	public void destroy() {
		this.remove();
		this.lines.clear();
	}

	public ArmorStand get(final int index) {
		return this.entities.get(index);
	}

	public HologramUtil add(final String line) {
		this.lines.add(line);
		if (this.spawned) {
			this.update();
		}
		return this;
	}

	public HologramUtil set(final int index, final String line) {
		this.lines.set(index, line);
		if (this.spawned) {
			this.update();
		}
		return this;
	}

	private boolean removeEntity(final ArmorStand entity) {
		this.lines.remove(entity.getCustomName());
		entity.remove();
		final boolean removed = this.entities.remove(entity);
		if (removed && this.spawned) {
			this.update();
		}
		return removed;
	}

	public void remove() {
		for (final ArmorStand stand : this.entities) {
			stand.remove();
		}
	}

	public boolean remove(final String line) {
		for (final ArmorStand entity : this.entities) {
			if (entity.getCustomName().equals(line)) {
				return this.removeEntity(entity);
			}
		}
		return false;
	}
}