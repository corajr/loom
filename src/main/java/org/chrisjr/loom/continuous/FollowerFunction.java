package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.Interval;

public class FollowerFunction extends ContinuousFunction {
	final Pattern pattern;

	public FollowerFunction(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public double call(BigFraction t) {
		throw new UnsupportedOperationException(
				"Use the call(Interval) function instead!");
	}

	@Override
	public double call(Interval i) {
		return pattern.getValueFor(i);
	}
}
