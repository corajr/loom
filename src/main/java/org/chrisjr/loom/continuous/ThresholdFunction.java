package org.chrisjr.loom.continuous;

import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.Interval;

public class ThresholdFunction extends FollowerFunction {
	private final double threshold;

	public ThresholdFunction(Pattern pattern, double threshold) {
		super(pattern);
		this.threshold = threshold;
	}

	@Override
	public double call(Interval i) {
		double otherValue = super.call(i);
		return (otherValue >= threshold) ? 1.0 : 0.0;
	}
}
