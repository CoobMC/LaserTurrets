package games.coob.laserturrets.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mineacademy.fo.ReflectionUtil;

import java.lang.reflect.Constructor;

@RequiredArgsConstructor
public enum TurretType {

	ARROW(Arrow.class),
	FIREBALL(Fireball.class),
	BEAM(Beam.class);
	// TODO add particle and bullet type

	@Getter
	private final Class<? extends TurretSettings> instanceClass;

	protected <T extends TurretSettings> T instantiate(final String name) {
		final Constructor<?> constructor = ReflectionUtil.getConstructor(this.instanceClass, String.class, TurretType.class);

		return (T) ReflectionUtil.instantiate(constructor, name, this);
	}
}
