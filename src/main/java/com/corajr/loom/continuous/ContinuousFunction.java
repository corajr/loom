package com.corajr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

import com.corajr.loom.time.Interval;

/**
 * A ContinuousFunction must define <code>call</code> as a function of time,
 * returning a value between 0.0 and 1.0.
 * 
 * @author corajr
 * 
 */
public abstract class ContinuousFunction {
	/**
	 * Returns the value of the function at this point in the cycle. Should be
	 * defined everywhere.
	 * 
	 * @param t
	 *            a BigFraction of the cycle
	 * @return a double between 0.0 and 1.0
	 */
	public abstract double call(BigFraction t);

	/**
	 * Return the average of the function's output between the endpoints of this
	 * interval.
	 * 
	 * @param i
	 *            a query interval
	 * @return a double between 0.0 and 1.0
	 */
	public double call(Interval i) {
		double value = call(i.getStart());
		value += call(i.getEnd());
		value /= 2.0;
		return value;
	}
}
