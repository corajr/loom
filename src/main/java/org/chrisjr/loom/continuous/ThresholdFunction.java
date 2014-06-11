package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;

public class ThresholdFunction extends FollowerFunction {
	private final double threshold;

	public ThresholdFunction(Pattern pattern, double threshold) {
		super(pattern);
		this.threshold = threshold;
	}

	public double call(BigFraction t) {
		double otherVal = super.call(t);
		if (otherVal >= threshold)
			return 1.0;
		else
			return 0.0;
	}
}
