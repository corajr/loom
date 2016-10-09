package com.corajr.loom.continuous;

import com.corajr.loom.Pattern;
import com.corajr.loom.time.Interval;

/**
 * Follows another pattern, returning 1.0 when it matches the specified value,
 * and 0.0 otherwise.
 * 
 * @author corajr
 */
public class MatchFunction extends FollowerFunction {
	private final double matchValue;
	private final static double EPSILON = 1e-6;

	public MatchFunction(Pattern pattern, double matchValue) {
		super(pattern);
		this.matchValue = matchValue;
	}

	@Override
	public double call(Interval i) {
		double otherValue = super.call(i);
		return (Math.abs(otherValue - matchValue) < EPSILON) ? 1.0 : 0.0;
	}
}
