package com.corajr.loom.mappings;

/**
 * A Mapping is an interface with one method, <code>call</code>, that returns a
 * single output for a value between 0.0 and 1.0. It may also return null if
 * there is no output associated with a given value.
 * 
 * @author corajr
 * 
 * @param <T>
 *            the return type of the mapping
 */

public interface Mapping<T> {
	/**
	 * @param value
	 *            a real number in the interval [0.0, 1.0]
	 * @return the output for that value
	 */
	public T call(double value);
}
