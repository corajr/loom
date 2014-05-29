package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

public class ConstantFunction extends ContinuousFunction {
	private double value;

	public ConstantFunction(double value) {
		this.value = value;
	}

	public double call(BigFraction t) {
		return value;
	}
}
