package games.coob.laserturrets.util;

public final class TripleBuilder<A, B, C> {
	private A first;
	private B second;
	private C third;

	public TripleBuilder<A, B, C> setFirst(final A first) {
		this.first = first;
		return this;
	}

	public TripleBuilder<A, B, C> setSecond(final B second) {
		this.second = second;
		return this;
	}

	public TripleBuilder<A, B, C> setThird(final C third) {
		this.third = third;
		return this;
	}

	public Triple<A, B, C> build() {
		return new Triple<>(first, second, third);
	}
}
