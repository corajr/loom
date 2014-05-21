package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

public class SineFunction extends ContinuousFunction {
	public double call(BigFraction t) {
		return Math.sin(t.multiply(2).doubleValue() * Math.PI);
	}
}
