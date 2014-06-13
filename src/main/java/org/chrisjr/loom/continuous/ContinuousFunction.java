package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;

/**
 * @author chrisjr
 * 
 *         A ContinuousFunction must define <code>call</code> as a function of
 *         time, returning a value between 0.0 and 1.0.
 */
public abstract class ContinuousFunction {
	/**
	 * @param t
	 *            a BigFraction of the cycle
	 * @return a double between 0.0 and 1.0
	 */
	public abstract double call(BigFraction t);

	/**
	 * @param i
	 *            an Interval from the cycle
	 * @return a double between 0.0 and 1.0, the average of the values at the
	 *         start and end of the interval
	 */
	public double call(Interval i) {
		double value = call(i.getStart());
		value += call(i.getEnd());
		value /= 2.0;
		return value;
	}
}
