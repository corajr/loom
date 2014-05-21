package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

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
}
