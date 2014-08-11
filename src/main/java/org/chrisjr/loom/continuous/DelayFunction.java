package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.Pattern;
import org.chrisjr.loom.time.Interval;

/**
 * Returns the value of the original function after a delay.
 * 
 * @author chrisjr
 * 
 */
public class DelayFunction extends FollowerFunction {
	final BigFraction delay;

	public DelayFunction(Pattern pattern, BigFraction delay) {
		super(pattern);
		this.delay = delay;
	}

	@Override
	public double call(Interval i) {
		return pattern.getValueFor(i.subtract(delay));
	}
}
