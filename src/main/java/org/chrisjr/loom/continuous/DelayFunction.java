package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.Interval;

public class DelayFunction extends FollowerFunction {
	final BigFraction delay;

	public DelayFunction(Pattern pattern, BigFraction delay) {
		super(pattern);
		this.delay = delay;
	}

	@Override
	public double call(BigFraction t) {
		return pattern.getValueFor(makeIntervalAround(t).subtract(delay));
	}
}
