package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.Interval;

public class FollowerFunction extends ContinuousFunction {
	final Pattern pattern;
	final BigFraction halfResolution = new BigFraction(1, 2000);
	
	public FollowerFunction(Pattern pattern) {
		this.pattern = pattern;
	}
	
	protected Interval makeIntervalAround(BigFraction t) {
		return new Interval(t.subtract(halfResolution), t.add(halfResolution));
	}

	@Override
	public double call(BigFraction t) {
		return pattern.getValueFor(makeIntervalAround(t));
	}

}
