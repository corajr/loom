package com.corajr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

public class Interval {
	private final BigFraction start;
	private final BigFraction end;

	public Interval(double start, double end) {
		this(IntervalMath.toFraction(start), IntervalMath.toFraction(end));
	}

	/**
	 * Creates a new Interval, which must have a non-zero duration and end after
	 * it begins.
	 * 
	 * @param start
	 *            the beginning of the interval
	 * @param end
	 *            the end of the interval
	 */
	public Interval(BigFraction start, BigFraction end) {
		if (end.compareTo(start) <= 0)
			throw new IllegalArgumentException("Must end after start!");
		this.start = start;
		this.end = end;
	}

	/**
	 * Creates an interval that goes from 0 to the specified number of cycles.
	 * 
	 * @param duration
	 *            the length of the interval
	 * @return a new interval
	 */
	public static Interval zeroTo(double duration) {
		return zeroTo(IntervalMath.toFraction(duration));
	}

	/**
	 * Creates an interval that goes from 0 to the specified number of cycles.
	 * 
	 * @param duration
	 *            the length of the interval
	 * @return a new interval
	 */
	public static Interval zeroTo(BigFraction duration) {
		return new Interval(BigFraction.ZERO, duration);
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
		return add(IntervalMath.toFraction(i));
	}

	public Interval subtract(double i) {
		return subtract(IntervalMath.toFraction(i));
	}

	public Interval multiply(double i) {
		return multiply(IntervalMath.toFraction(i));
	}

	/**
	 * "Modulo interval" in the sense given in
	 * <http://www.cs.tau.ac.il/~nachum/papers/Modulo.pdf>. This transforms a
	 * BigFraction to fit it into the specified interval.
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

		BigFraction x_minus_a = x.subtract(a);
		BigFraction length = b.subtract(a);

		int multiplier = (int) Math.floor((x_minus_a.divide(length))
				.doubleValue());

		return x.subtract(length.multiply(multiplier));
	}

	/**
	 * Shifts the start or end of this interval to make it fit inside another.
	 * If this interval is larger than the modulo interval, it will throw an
	 * IllegalArgumentException.
	 * 
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

	/**
	 * Multiplies this interval by some amount, then fits the product into
	 * another interval.
	 * 
	 * @param amt
	 *            the multiplier
	 * @param interval
	 *            the other interval into which the product should be fit
	 * @return the product, modulo the other interval
	 */
	public Interval multiplyMod(double amt, Interval interval) {
		return multiplyMod(IntervalMath.toFraction(amt), interval);
	}

	/**
	 * Fits the start and end of an interval inside another interval, returning
	 * the result.
	 * 
	 * @param start
	 *            the start of the original interval
	 * @param end
	 *            the end of the original interval
	 * @param other
	 *            the interval within which to fit the start and end
	 * @return the new interval
	 * @see fractionMod
	 */
	public static Interval modulo(BigFraction start, BigFraction end,
			Interval other) {
		BigFraction newStart = Interval.fractionMod(start, other);
		BigFraction newEnd = Interval.fractionMod(end, other);

		Interval i = null;

		switch (newStart.compareTo(newEnd)) {
		case -1:
			i = new Interval(newStart, newEnd);
			break;
		case 0:
			throw new IllegalArgumentException(String.format(
					"%s and %s are equal after modulo(%s)", start, end, other));
		case 1:
			i = new Interval(newEnd, newStart);
			break;
		}

		return i;
	}

	/**
	 * Multiplies this interval by some amount, then fits the product into
	 * another interval.
	 * 
	 * @param fraction
	 *            the multiplier
	 * @param interval
	 *            the other interval into which the product should be fit
	 * @return the product, modulo the other interval
	 */
	public Interval multiplyMod(BigFraction fraction, Interval interval) {
		BigFraction newStart = getStart().multiply(fraction);
		BigFraction newEnd = getEnd().multiply(fraction);

		return Interval.modulo(newStart, newEnd, interval);
	}

	/**
	 * Splits an interval into two by cutting off a portion. For example: if the
	 * original interval went from 0 to 1, and the fraction was 1/4, the new
	 * intervals would be [0, 3/4] and [3/4, 1].
	 * 
	 * @param interval
	 *            the original interval
	 * @param fraction
	 *            the amount by which to shorten
	 * @return an array of two intervals, long and short
	 */
	public static Interval[] shortenBy(Interval interval, BigFraction fraction) {
		BigFraction newEnd = interval.getEnd().subtract(fraction);

		Interval shortened = new Interval(interval.getStart(), newEnd);
		Interval after = new Interval(newEnd, interval.getEnd());

		return new Interval[] { shortened, after };
	}

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

	@Override
	public String toString() {
		String s = "[" + start.toString() + "," + end.toString() + "]";
		return s.replaceAll(" ", "");
	}
}
