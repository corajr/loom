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

	public Interval add(int i) {
		return add(new BigFraction(i));
	}

	public Interval subtract(int i) {
		return subtract(new BigFraction(i));
	}

	public Interval multiply(int i) {
		return multiply(new BigFraction(i));
	}
	
	public Interval modulo(Interval other) throws IllegalArgumentException {
		BigFraction mySize = getSize();
		
		// TODO if my entire span is bigger than the other interval, what should happen?
		if (mySize.compareTo(other.getSize()) > 0) {
			throw new IllegalArgumentException("Cannot find modulo using smaller interval.");
		}
		
		Interval i = this;
		while (i.getStart().compareTo(other.getEnd()) > 0) {
			i = i.subtract(other.getEnd());
		}

		return i;
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
