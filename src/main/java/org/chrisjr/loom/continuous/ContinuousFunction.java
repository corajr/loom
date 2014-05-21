package org.chrisjr.loom.continuous;

import org.apache.commons.math3.fraction.BigFraction;

public abstract class ContinuousFunction {
	public abstract double call(BigFraction t);
}
