package com.corajr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

/**
 * A sine function with a period of 1 cycle, with a range between 0.0 and 1.0.
 * 
 * @author corajr
 */
public class SineFunction extends ContinuousFunction {
	@Override
	public double call(BigFraction t) {
		return (Math.sin(t.multiply(2).doubleValue() * Math.PI) + 1.0) / 2.0;
	}
}
