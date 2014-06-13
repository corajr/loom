package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;

public class MatchFunction extends FollowerFunction {
	private final double matchValue;
	private final static double EPSILON = 1e-6;

	public MatchFunction(Pattern pattern, double matchValue) {
		super(pattern);
		this.matchValue = matchValue;
	}

	@Override
	public double call(BigFraction t) {
		double otherValue = super.call(t);
		return (Math.abs(otherValue - matchValue) < EPSILON) ? 1.0 : 0.0;
	}
}
