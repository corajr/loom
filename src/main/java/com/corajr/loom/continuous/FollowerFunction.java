package com.corajr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.Pattern;
import com.corajr.loom.time.Interval;

/**
 * "Follows" another pattern by returning that pattern's value when queried.
 * 
 * @author corajr
 */
public class FollowerFunction extends ContinuousFunction {
	final Pattern pattern;

	/**
	 * Creates a pattern that returns the same value as the underlying pattern.
	 * 
	 * @param pattern
	 *            the pattern to be followed
	 */
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
