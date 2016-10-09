package com.corajr.loom.mappings;

/**
 * Map a value from [0.0, 1.0] to [lo, hi] (range is inclusive).
 * 
 * Note: Given this behavior, this class should *not* be used for getting an array index.
 * 
 * @author corajr
 */
public class IntMapping implements Mapping<Integer> {
	final int lo;
	final int hi;
	final int intervalSize;

	public IntMapping(int lo, int hi) {
		this.lo = lo;
		this.hi = hi;
		this.intervalSize = (hi - lo);
	}

	@Override
	public Integer call(double value) {
		return (int) Math.round(intervalSize * value) + lo;
	}
}
