package org.chrisjr.loom.time;

import org.apache.commons.math3.fraction.Fraction;

public class Interval {
	private final Fraction start;
	private final Fraction end;
	
	public Interval(double _start, double _end) {
		this(new Fraction(_start), new Fraction(_end));
	}

	public Interval(Fraction _start, Fraction _end) {
		start = _start;
		end = _end;
	}

	public Fraction getStart() {
		return start;
	}

	public Fraction getEnd() {
		return end;
	}
		
	public Interval add(Fraction fraction) {
		return new Interval(start.add(fraction), end.add(fraction));
	}

	public Interval multiply(Fraction fraction) {
		return new Interval(start.multiply(fraction), end.multiply(fraction));
	}

	public Interval add(int i) {
		return add(new Fraction(i));
	}

	public Interval multiply(int i) {
		return multiply(new Fraction(i));
	}
}
