package com.corajr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * Returns a constant whenever queried.
 * 
 * @author corajr
 */
public class ConstantFunction extends ContinuousFunction {
	private final double value;

	public ConstantFunction(double value) {
		this.value = value;
	}

	@Override
	public double call(BigFraction t) {
		return value;
	}
}
