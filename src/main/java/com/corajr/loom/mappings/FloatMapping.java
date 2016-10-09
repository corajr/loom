package com.corajr.loom.mappings;

/**
 * Map a value from [0.0, 1.0] to [lo, hi] inclusive, as a floating-point value.
 * 
 * @author corajr
 */

public class FloatMapping implements Mapping<Float> {
	final float lo;
	final float hi;
	final float intervalSize;

	public FloatMapping(float lo, float hi) {
		this.lo = lo;
		this.hi = hi;
		this.intervalSize = (hi - lo);
	}

	@Override
	public Float call(double value) {
		return (float) (intervalSize * value) + lo;
	}
}
