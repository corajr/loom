package org.chrisjr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

public class Interval {
	private final BigFraction start;
	private final BigFraction end;

	public Interval(double _start, double _end) {
		this(new BigFraction(_start), new BigFraction(_end));
	}

	public Interval(BigFraction _start, BigFraction _end) {
		if (_end.compareTo(_start) <= 0)
			throw new IllegalArgumentException("Must end after start!");
		start = _start;
		end = _end;
	}

	public BigFraction getStart() {
		return start;
	}

	public BigFraction getEnd() {
		return end;
	}

	public BigFraction getSize() {
		return end.subtract(start);
	}

	public Interval add(BigFraction fraction) {
		return new Interval(start.add(fraction), end.add(fraction));
	}

	public Interval subtract(BigFraction fraction) {
		return new Interval(start.subtract(fraction), end.subtract(fraction));
	}

	public Interval multiply(BigFraction fraction) {
		return new Interval(start.multiply(fraction), end.multiply(fraction));
	}

	public Interval add(double i) {
		return add(new BigFraction(i));
	}

	public Interval subtract(double i) {
		return subtract(new BigFraction(i));
	}

	public Interval multiply(double i) {
		return multiply(new BigFraction(i));
	}

	private boolean contains(BigFraction fraction) {
		return fraction.compareTo(getStart()) >= 0
				&& fraction.compareTo(getEnd()) <= 0;
	}

	/**
	 * "modulo interval" in the sense given in
	 * http://www.cs.tau.ac.il/~nachum/papers/Modulo.pdf
	 * 
	 * @param fraction
	 *            the fraction to transform
	 * @param interval
	 *            the interval into which to fit the fraction
	 * @return the fraction mod [start, end]
	 */
	private static BigFraction fractionMod(BigFraction x, Interval interval) {

		BigFraction a = interval.getStart();
		BigFraction b = interval.getEnd();

		if (a.compareTo(b) == 0)
			return x;

		BigFraction x_minus_a = x.subtract(a);
		BigFraction length = b.subtract(a);

		int multiplier = (int) Math.floor((x_minus_a.divide(length))
				.doubleValue());

		return x.subtract(length.multiply(multiplier));
	}

	/**
	 * @param other
	 *            the other interval
	 * @return this interval shifted into the range of the other
	 * @throws IllegalArgumentException
	 */
	public Interval modulo(Interval other) throws IllegalArgumentException {

		BigFraction otherSize = other.getSize();
		if (this.getSize().compareTo(otherSize) > 0)
			throw new IllegalArgumentException(
					"This interval is larger than modulo interval; will be aliased!");

		Interval i = this;

		while (i.getStart().compareTo(other.getStart()) < 0)
			i = i.add(otherSize);
		
		while (i.getEnd().compareTo(other.getEnd()) > 0)
			i = i.subtract(otherSize);

		return i;
	}

	public Interval multiplyMod(int i, Interval interval) {
		return multiplyMod(new BigFraction(i), interval);
	}

	public static Interval modulo(BigFraction newStart, BigFraction newEnd,
			Interval other) {
		newStart = Interval.fractionMod(newStart, other);
		newEnd = Interval.fractionMod(newEnd, other);

		Interval i = null;
		if (newStart.compareTo(newEnd) <= 0) {
			i = new Interval(newStart, newEnd);
		} else {
			i = new Interval(newEnd, newStart);
		}

		return i;
	}

	public Interval multiplyMod(BigFraction fraction, Interval interval) {
		BigFraction newStart = getStart().multiply(fraction);
		BigFraction newEnd = getEnd().multiply(fraction);

		return Interval.modulo(newStart, newEnd, interval);
	}
	
	public static Interval[] shortenBy(Interval interval, BigFraction fraction) {
		BigFraction newEnd = interval.getEnd().subtract(fraction);

		Interval shortened = new Interval(interval.getStart(), newEnd);
		Interval after = new Interval(newEnd, interval.getEnd());

		return new Interval[] { shortened, after };
	}

	// Auto-generated hashCode and equals

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Interval))
			return false;
		Interval other = (Interval) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	public String toString() {
		String s = "[" + start.toString() + "," + end.toString() + "]";
		return s.replaceAll(" ", "");
	}
}
