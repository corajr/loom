package org.chrisjr.loom.time;

import org.apache.commons.math3.fraction.BigFraction;

public class Interval {
	private final BigFraction start;
	private final BigFraction end;
	
	public Interval(double _start, double _end) {
		this(new BigFraction(_start), new BigFraction(_end));
	}

	public Interval(BigFraction _start, BigFraction _end) {
		start = _start;
		end = _end;
	}

	public BigFraction getStart() {
		return start;
	}

	public BigFraction getEnd() {
		return end;
	}
		
	public Interval add(BigFraction fraction) {
		return new Interval(start.add(fraction), end.add(fraction));
	}

	public Interval multiply(BigFraction fraction) {
		return new Interval(start.multiply(fraction), end.multiply(fraction));
	}

	public Interval add(int i) {
		return add(new BigFraction(i));
	}

	public Interval multiply(int i) {
		return multiply(new BigFraction(i));
	}
}
