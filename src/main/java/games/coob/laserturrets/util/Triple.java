package games.coob.laserturrets.util;

import lombok.Data;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.SerializeUtil.Mode;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.ConfigSerializable;

/**
 * Simple triple for 3 value pairs
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
@Data
public final class Triple<A, B, C> implements ConfigSerializable {

	private A firstValue;

	private B secondValue;

	private C thirdValue;

	public Triple(final A first, final B second, final C third) {
		this.firstValue = first;
		this.secondValue = second;
		this.thirdValue = third;
	}

	/**
	 * @see org.mineacademy.fo.model.ConfigSerializable#serialize()
	 */
	@Override
	public SerializedMap serialize() {
		return SerializedMap.ofArray("A", this.firstValue, "B", this.secondValue, "C", this.thirdValue);
	}

	/**
	 * Return this triple in X - Y - Z syntax
	 *
	 * @return
	 */
	public String toLine() {
		return this.firstValue + " - " + this.secondValue + " - " + this.thirdValue;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.toLine();
	}

	/**
	 * Transform the given config section to triple
	 *
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param map
	 * @param firstType
	 * @param secondType
	 * @param thirdType
	 * @return
	 */
	public static <A, B, C> Triple<A, B, C> deserialize(final SerializedMap map, final Class<A> firstType, final Class<B> secondType, final Class<C> thirdType) {

		final A first = map.containsKey("A") ? map.get("A", firstType) : null;
		final B second = map.containsKey("B") ? map.get("B", secondType) : null;
		final C third = map.containsKey("C") ? map.get("C", thirdType) : null;

		return new Triple<>(first, second, third);
	}

	/**
	 * Deserialize the given line (it must have the KEY - VALUE syntax) into the given tuple,
	 * suited for YAML storage not JSON.
	 *
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param line
	 * @param firstType
	 * @param secondType
	 * @param thirdType
	 * @return triple or null if line is null
	 */
	public static <A, B, C> Triple<A, B, C> deserialize(final String line, final Class<A> firstType, final Class<B> secondType, final Class<C> thirdType) {
		if (line == null)
			return null;

		final String split[] = line.split(" - ");
		Valid.checkBoolean(split.length == 2, "Line must have the syntax <" + firstType.getSimpleName() + "> - <" + secondType.getSimpleName() + "> - <" + thirdType.getSimpleName() + "> but got: " + line);

		final A first = SerializeUtil.deserialize(Mode.YAML, firstType, split[0]);
		final B second = SerializeUtil.deserialize(Mode.YAML, secondType, split[1]);
		final C third = SerializeUtil.deserialize(Mode.YAML, thirdType, split[2]);

		return new Triple<>(first, second, third);
	}

	/**
	 * Do not use
	 *
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param map
	 * @return
	 * @deprecated do not use
	 */
	@Deprecated
	public static <A, B, C> Triple<A, B, C> deserialize(final SerializedMap map) {
		throw new FoException("Triple cannot be deserialized automatically, call Triple#deserialize(map, firstType, secondType, thirdType)");
	}
}
