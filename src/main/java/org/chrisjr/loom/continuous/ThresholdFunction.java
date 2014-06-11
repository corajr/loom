package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;

public class ThresholdFunction extends FollowerFunction {
	private final double threshold;

	public ThresholdFunction(Pattern pattern, double threshold) {
		super(pattern);
		this.threshold = threshold;
	}

	@Override
	public double call(BigFraction t) {
		double otherValue = super.call(t);
		return (otherValue >= threshold) ? 1.0 : 0.0;
	}
}
