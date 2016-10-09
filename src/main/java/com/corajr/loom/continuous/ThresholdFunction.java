package com.corajr.loom.continuous;

import com.corajr.loom.Pattern;
import com.corajr.loom.time.Interval;

/**
 * Follows another pattern, returning 1.0 when its value equals or exceeds the
 * specified threshold and 0.0 otherwise.
 * 
 * @author corajr
 */
public class ThresholdFunction extends FollowerFunction {
	private final double threshold;

	/**
	 * Create a ThresholdFunction that follows another pattern.
	 * 
	 * @param pattern
	 *            the pattern to be followed
	 * @param threshold
	 *            the >= threshold between 0.0 and 1.0
	 */
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
